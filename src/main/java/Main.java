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
                if (repository != null) {
                    long gameId = repository.saveGameResult(result);
                    GameLogger.info("GAME_PERSISTED id=" + gameId);
                }
            }
            GameView.showFinalScores(state.playerNames, state.scores);
        } finally {
            if (emf != null) emf.close();
        }
    }

    private static void runReports(GameConfig config) {
        EntityManagerFactory emf = JpaUtil.createEntityManagerFactory(config.dbUrl);
        try {
            GameRepository repository = new GameRepository(emf);
            if (config.reportRecent) ReportView.showRecentGames(repository.recentGames(config.reportLimit));
            if (config.reportWins) ReportView.showWinCounts(repository.playerWinCounts());
            if (config.reportHighScores) ReportView.showHighestScores(repository.highestScores(config.reportLimit));
        } finally {
            emf.close();
        }
    }

    private static GameConfig parseArgs(String[] args) {
        GameConfig config = new GameConfig();
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length) config.bots = Integer.parseInt(args[++i]);
            else if (args[i].equals("--games") && i + 1 < args.length) config.games = Integer.parseInt(args[++i]);
            else if (args[i].equals("--seed") && i + 1 < args.length) config.seed = Long.parseLong(args[++i]);
            else if (args[i].equals("--db-url") && i + 1 < args.length) config.dbUrl = args[++i];
            else if (args[i].equals("--limit") && i + 1 < args.length) config.reportLimit = Integer.parseInt(args[++i]);
            else if (args[i].equals("--human")) config.human = true;
            else if (args[i].equals("--quiet")) config.quiet = true;
            else if (args[i].equals("--no-db")) config.noDb = true;
            else if (args[i].equals("--history")) config.reportRecent = true;
            else if (args[i].equals("--wins")) config.reportWins = true;
            else if (args[i].equals("--high-scores")) config.reportHighScores = true;
            else if (args[i].equals("--self-test")) { selfTest(); return null; }
            else if (args[i].equals("--help")) { printUsage(); return null; }
            else throw new IllegalArgumentException("Unknown argument: " + args[i]);
        }
        return config;
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  mvn exec:java -Dexec.args=\"[--bots N] [--games N] [--human] [--quiet] [--seed N] [--no-db]\"");
        System.out.println("  mvn exec:java -Dexec.args=\"--history [--limit N]\"");
        System.out.println("  mvn exec:java -Dexec.args=\"--wins\"");
        System.out.println("  mvn exec:java -Dexec.args=\"--high-scores [--limit N]\"");
        System.out.println("  Optional: --db-url jdbc:h2:./data/uno");
    }

    static void selfTest() {
        int p = 0;
        if (CardRules.color("R5").equals("R")) p++; else fail("color R5");
        if (CardRules.rank("G+2").equals("DRAW_TWO")) p++; else fail("rank +2");
        if (CardRules.points("W4") == 50) p++; else fail("wild points");
        if (CardRules.isLegal("R2", "R9", "")) p++; else fail("same color");
        if (CardRules.isLegal("G9", "R9", "")) p++; else fail("same number");
        if (CardRules.isLegal("B3", "W", "B")) p++; else fail("called color");
        if (!CardRules.isLegal("B3", "R9", "")) p++; else fail("illegal mismatch");
        ArrayList<String> h = new ArrayList<>(Arrays.asList("B3", "R4", "W"));
        if (BotStrategy.chooseCard(h, "R9", "") == 1) p++; else fail("bot normal before wild");
        ArrayList<String> h2 = new ArrayList<>(Arrays.asList("B1", "B2", "R3"));
        if (BotStrategy.chooseColor(h2).equals("B")) p++; else fail("bot color");
        System.out.println("Passed " + p + " characterization checks.");
    }

    static void fail(String name) {
        throw new RuntimeException("Failed: " + name);
    }
}
