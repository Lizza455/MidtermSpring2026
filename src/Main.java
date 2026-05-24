import java.util.*;

// Orchestrates the game loop.
// Rules: CardRules  Bots: BotStrategy  Output: GameView  State: GameState
public class Main {
    static GameState state;

    public static void main(String[] args) {
        int bots = 3, games = 1;
        boolean human = false, quiet = false;
        long seed = System.currentTimeMillis();

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--bots") && i + 1 < args.length)        bots  = Integer.parseInt(args[++i]);
            else if (args[i].equals("--games") && i + 1 < args.length)  games = Integer.parseInt(args[++i]);
            else if (args[i].equals("--seed") && i + 1 < args.length)   seed  = Long.parseLong(args[++i]);
            else if (args[i].equals("--human"))    human = true;
            else if (args[i].equals("--quiet"))    quiet = true;
            else if (args[i].equals("--self-test")) { selfTest(); return; }
            else if (args[i].equals("--test"))      { GameTest.main(new String[0]); return; }
            else if (args[i].equals("--help")) {
                System.out.println("Usage: scripts/run.sh [--bots N] [--games N] [--human] [--quiet] [--seed N]");
                return;
            }
        }

        state = new GameState(seed);
        GameView.setQuiet(quiet);
        state.setupPlayers(bots, human);

        if (state.playerNames.size() < 2 || state.playerNames.size() > 4) {
            System.out.println("UNO needs 2 to 4 players.");
            return;
        }
        for (int g = 1; g <= games; g++) { GameView.showGameHeader(g); playGame(); }
        GameView.showFinalScores(state.playerNames, state.scores);
    }

    static void playGame() {
        state.buildDeck();
        Collections.shuffle(state.deck, state.random);
        state.discard.clear();
        for (ArrayList<String> hand : state.hands) hand.clear();
        for (int i = 0; i < state.playerNames.size(); i++)
            for (int j = 0; j < 7; j++) state.hands.get(i).add(state.draw());

        state.upCard = state.draw();
        while (state.upCard.startsWith("W")) { state.discard.add(state.upCard); state.upCard = state.draw(); }
        state.calledColor = "";
        state.direction = 1;
        state.currentPlayer = state.random.nextInt(state.playerNames.size());
        state.log("Game started. First player: " + state.currentPlayerName());

        int guard = 0;
        while (guard < 3000) { guard++; if (playTurn()) return; }
        state.log("Game stopped at safety limit.");
        GameView.showSafetyLimit();
    }

    // Runs one full turn. Returns true when a player wins.
    static boolean playTurn() {
        String name = state.currentPlayerName();
        ArrayList<String> hand = state.currentHand();
        GameView.showUpCard(state.upCard, state.calledColor);
        GameView.showHand(name, hand);

        int chosen = state.isHuman(state.currentPlayer)
                ? resolveHumanChoice(hand)
                : BotStrategy.chooseCard(hand, state.upCard, state.calledColor);

        // No legal card — draw one, then optionally play it.
        if (chosen == -1) {
            String drawn = state.draw();
            hand.add(drawn);
            GameView.showDraw(name, drawn);
            state.log(name + " drew " + drawn);
            if (CardRules.isLegal(drawn, state.upCard, state.calledColor)) {
                if (!state.isHuman(state.currentPlayer)) {
                    chosen = hand.size() - 1;
                } else {
                    String answer = GameView.promptDrawnCard(drawn);
                    if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes"))
                        chosen = hand.size() - 1;
                }
            }
        }

        if (chosen >= 0) {
            if (chosen >= hand.size()) {
                GameView.showPenaltyInvalidIndex(name);
                hand.add(state.draw());
                state.log(name + " penalty draw (invalid index)");
                state.next(); return false;
            }
            String card = hand.get(chosen);
            if (!CardRules.isLegal(card, state.upCard, state.calledColor)) {
                GameView.showPenaltyIllegalCard(name, card);
                hand.add(state.draw());
                state.log(name + " penalty draw (illegal card: " + card + ")");
                state.next(); return false;
            }
            hand.remove(chosen);
            state.discard.add(state.upCard);
            state.upCard = card;
            state.calledColor = "";
            GameView.showPlay(name, card);
            state.log(name + " played " + card);

            if (card.equals("W") || card.equals("W4")) {
                state.calledColor = state.isHuman(state.currentPlayer)
                        ? GameView.resolveHumanColor() : BotStrategy.chooseColor(hand);
                GameView.showCalledColor(name, state.calledColor);
                state.log(name + " called " + state.calledColor);
            }
            if (hand.size() == 1) GameView.showUno(name);
            if (hand.isEmpty()) {
                int points = state.tallyPoints(state.currentPlayer);
                state.scores[state.currentPlayer] += points;
                GameView.showWin(name, points);
                state.log(name + " won with " + points + " points");
                return true;
            }
            applyCardEffect(card);
        } else {
            state.next();
        }
        return false;
    }

    // Applies the card effect and advances the turn.
    static void applyCardEffect(String card) {
        String rank = CardRules.rank(card);
        if (rank.equals("SKIP")) {
            state.next();
            state.log(state.currentPlayerName() + " was skipped");
            state.next();
        } else if (rank.equals("REVERSE")) {
            state.direction *= -1;
            state.log("Direction reversed");
            state.next();
            if (state.playerNames.size() == 2) state.next(); // reverse = skip in 2-player
        } else if (rank.equals("DRAW_TWO")) {
            state.next();
            state.currentHand().add(state.draw()); state.currentHand().add(state.draw());
            GameView.showDrawTwo(state.currentPlayerName());
            state.log(state.currentPlayerName() + " draws two and is skipped");
            state.next();
        } else if (rank.equals("WILD_DRAW_FOUR")) {
            state.next();
            for (int i = 0; i < 4; i++) state.currentHand().add(state.draw());
            GameView.showDrawFour(state.currentPlayerName());
            state.log(state.currentPlayerName() + " draws four and is skipped");
            state.next();
        } else {
            state.next();
        }
    }

    // Reads and validates human card input. GameView reads; Main validates legality.
    static int resolveHumanChoice(ArrayList<String> hand) {
        while (true) {
            String input = GameView.promptCardChoice();
            if (input.equals("DRAW")) return -1;
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) return index;
            } catch (Exception ignored) {}
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (CardRules.isLegal(hand.get(i), state.upCard, state.calledColor)) return i;
                    GameView.showCardNotLegal();
                }
            }
            GameView.showCardNotFound();
        }
    }

    // Kept for backward compatibility with test.sh.
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

    static void fail(String name) { throw new RuntimeException("Failed: " + name); }
}