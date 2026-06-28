import jakarta.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    static GameState state;

    public static void main(String[] args) {
        GameConfig config = parseArgs(args);
        if (config == null) return;

        if (config.reportRecent || config.reportWins || config.reportHighScores) {
            runReports(config);
            return;
        }

        state = new GameState(config.seed);
        state.targetScore = config.targetScore;
        GameView.setQuiet(config.quiet);
        state.setupPlayers(config.bots, config.human);

        if (state.playerNames.size() < 2 || state.playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }

        EntityManagerFactory emf = null;
        GameRepository repository = null;
        if (!config.noDb) {
            emf = JpaUtil.createEntityManagerFactory(config.dbUrl);
            repository = new GameRepository(emf);
        }

        try {
            GameRunner runner = new GameRunner(state);
            for (int g = 1; g <= config.games; g++) {
                GameView.showGameHeader(g);
                GameResult result = runner.playGame();
                GameView.showFinalScores(state.playerNames, state.scores);
                if (!result.getWinnerName().equals("NO_WINNER")) {
                    System.out.println("\nOverall winner: " + result.getWinnerName()
                            + " after " + result.getRoundsPlayed() + " round(s)!");
                }
                if (repository != null) {
                    long gameId = repository.saveGameResult(result);
                    GameLogger.info("GAME_PERSISTED id=" + gameId);
                }
            }
        } finally {
            if (emf != null) emf.close();
        }
    }

    private static void runReports(GameConfig config) {
        EntityManagerFactory emf = JpaUtil.createEntityManagerFactory(config.dbUrl);
        try {
            GameRepository repo = new GameRepository(emf);
            if (config.reportRecent)     ReportView.showRecentGames(repo.recentGames(config.reportLimit));
            if (config.reportWins)       ReportView.showWinCounts(repo.playerWinCounts());
            if (config.reportHighScores) ReportView.showHighestScores(repo.highestScores(config.reportLimit));
        } finally {
            emf.close();
        }
    }

    private static GameConfig parseArgs(String[] args) {
        GameConfig config = new GameConfig();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--bots"         -> config.bots = Integer.parseInt(args[++i]);
                case "--games"        -> config.games = Integer.parseInt(args[++i]);
                case "--seed"         -> config.seed = Long.parseLong(args[++i]);
                case "--db-url"       -> config.dbUrl = args[++i];
                case "--limit"        -> config.reportLimit = Integer.parseInt(args[++i]);
                case "--target"       -> config.targetScore = Integer.parseInt(args[++i]);
                case "--human"        -> config.human = true;
                case "--quiet"        -> config.quiet = true;
                case "--no-db"        -> config.noDb = true;
                case "--history"      -> config.reportRecent = true;
                case "--wins"         -> config.reportWins = true;
                case "--high-scores"  -> config.reportHighScores = true;
                case "--self-test"    -> { selfTest(); return null; }
                case "--help"         -> { printUsage(); return null; }
                default -> throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }
        return config;
    }

    private static void printUsage() {
        System.out.println("UNO Game — Usage:");
        System.out.println("  mvn exec:java -Dexec.args=\"[options]\"");
        System.out.println();
        System.out.println("Game options:");
        System.out.println("  --bots N       Number of bot players (default 3, total players 2-4)");
        System.out.println("  --human        Add a human player");
        System.out.println("  --games N      Number of games to play (default 1)");
        System.out.println("  --target N     Score target to win game (default 500)");
        System.out.println("  --seed N       RNG seed for reproducibility");
        System.out.println("  --quiet        Suppress turn-by-turn output");
        System.out.println("  --no-db        Skip database persistence");
        System.out.println();
        System.out.println("Report options:");
        System.out.println("  --history      Show recent games");
        System.out.println("  --wins         Show player win counts");
        System.out.println("  --high-scores  Show highest scores");
        System.out.println("  --limit N      Limit report rows (default 10)");
        System.out.println("  --db-url URL   Custom H2 database URL");
        System.out.println();
        System.out.println("Human turn input:");
        System.out.println("  Enter a card index (0, 1, 2...) or card code (e.g. R5, GS, W)");
        System.out.println("  Enter DRAW to draw a card");
        System.out.println("  After drawing, enter y/n to play the drawn card");
    }

    static void selfTest() {
        int p = 0;
        // CardRules tests
        if (CardRules.color("R5").equals("R"))           p++; else fail("color R5");
        if (CardRules.rank("G+2").equals("DRAW_TWO"))    p++; else fail("rank +2");
        if (CardRules.points("W4") == 50)                p++; else fail("wild points");
        if (CardRules.isLegal("R2", "R9", ""))           p++; else fail("same color");
        if (CardRules.isLegal("G9", "R9", ""))           p++; else fail("same number");
        if (CardRules.isLegal("B3", "W", "B"))           p++; else fail("called color");
        if (!CardRules.isLegal("B3", "R9", ""))          p++; else fail("illegal mismatch");
        if (CardRules.isLegal("W", "R9", ""))            p++; else fail("wild always legal");
        if (CardRules.isLegal("W4", "B7", ""))           p++; else fail("w4 always legal");
        if (CardRules.isLegal("RS", "GS", ""))           p++; else fail("skip matches skip");
        if (!CardRules.isLegal("G3", "R9", "Y"))         p++; else fail("called color blocks");
        // BotStrategy tests
        ArrayList<String> h = new ArrayList<>(Arrays.asList("B3", "R4", "W"));
        if (BotStrategy.chooseCard(h, "R9", "") == 1)    p++; else fail("bot normal before wild");
        ArrayList<String> h2 = new ArrayList<>(Arrays.asList("B1", "B2", "R3"));
        if (BotStrategy.chooseColor(h2).equals("B"))     p++; else fail("bot color");
        // Deck composition test
        GameState gs = new GameState(42);
        gs.buildDeck();
        if (gs.deck.size() == 108)                       p++; else fail("deck size 108");
        System.out.println("Passed " + p + "/15 self-test checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("FAILED: " + name);
    }
}
