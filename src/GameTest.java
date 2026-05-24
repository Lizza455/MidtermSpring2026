import java.util.ArrayList;

// Characterization tests for the UNO implementation.
public class GameTest {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        runAll();
        System.out.println("GameTest: " + passed + " passed, " + failed + " failed.");
        if (failed > 0) System.exit(1);
    }

    static void runAll() {
        testColorMatching();
        testNumberMatching();
        testActionTypeMatching();
        testWildBehavior();
        testWildDrawFourBehavior();
        testSkipRank();
        testReverseRank();
        testDrawTwo();
        testCalledColorAfterWild();
        testIllegalPlays();
        testScoring();
        testDrawFromDeck();
        testDeckReshufflesWhenEmpty();
        testBotPrefersDrawTwo();
        testBotPrefersSkipOverNumber();
        testBotPrefersNumberOverWild();
        testBotChoosesMajorityColor();
        testEdgeCaseColorlessOnWild();
        testSkipAdvancesPlayerTwice();
        testReverseFlipsDirectionInThreePlayer();
        testReverseInTwoPlayerActsAsSkip();
        testDrawTwoGivesNextPlayerTwoCards();
        testWildDrawFourGivesNextPlayerFourCards();
        testNormalCardAdvancesOnce();
    }

    // Color matching

    static void testColorMatching() {
        check("R5 on R9 (same color)", CardRules.isLegal("R5", "R9", ""));
        check("GS on G3 (same color)", CardRules.isLegal("GS", "G3", ""));
        check("B+2 on B0 (same color)", CardRules.isLegal("B+2", "B0", ""));
    }

    // Number matching

    static void testNumberMatching() {
        check("G9 on R9 (same number)", CardRules.isLegal("G9", "R9", ""));
        check("B0 on Y0 (same number)", CardRules.isLegal("B0", "Y0", ""));
        check("R3 on G7 is illegal", !CardRules.isLegal("R3", "G7", ""));
    }

    // Action type matching

    static void testActionTypeMatching() {
        check("RS on GS (skip on skip)", CardRules.isLegal("RS", "GS", ""));
        check("BR on YR (reverse on reverse)", CardRules.isLegal("BR", "YR", ""));
        check("R+2 on B+2 (draw-two on draw-two)", CardRules.isLegal("R+2", "B+2", ""));
        check("skip does not match reverse", !CardRules.isLegal("RS", "GR", ""));
        check("skip does not match draw-two", !CardRules.isLegal("RS", "G+2", ""));
    }

    //Wild

    static void testWildBehavior() {
        check("W is legal on any number card", CardRules.isLegal("W", "R9", ""));
        check("W is legal on an action card", CardRules.isLegal("W", "BS", ""));
        check("W is legal even with a called color active", CardRules.isLegal("W", "W", "G"));
        check("rank of W is WILD", CardRules.rank("W").equals("WILD"));
        check("W has no color", CardRules.color("W").isEmpty());
    }

    // Wild Draw Four

    static void testWildDrawFourBehavior() {
        check("W4 is always legal", CardRules.isLegal("W4", "B3", ""));
        check("W4 is legal on an action card", CardRules.isLegal("W4", "RS", ""));
        check("rank of W4 is WILD_DRAW_FOUR", CardRules.rank("W4").equals("WILD_DRAW_FOUR"));
        check("W4 scores 50", CardRules.points("W4") == 50);
    }

    //Skip

    static void testSkipRank() {
        check("RS rank is SKIP", CardRules.rank("RS").equals("SKIP"));
        check("GS rank is SKIP", CardRules.rank("GS").equals("SKIP"));
        check("skip scores 20", CardRules.points("RS") == 20);
    }

    //Reverse

    static void testReverseRank() {
        check("BR rank is REVERSE", CardRules.rank("BR").equals("REVERSE"));
        check("YR rank is REVERSE", CardRules.rank("YR").equals("REVERSE"));
        check("reverse scores 20", CardRules.points("YR") == 20);
    }

    //Draw two

    static void testDrawTwo() {
        check("R+2 rank is DRAW_TWO", CardRules.rank("R+2").equals("DRAW_TWO"));
        check("draw-two scores 20", CardRules.points("G+2") == 20);
        check("R+2 legal on R5 (color match)", CardRules.isLegal("R+2", "R5", ""));
        check("B+2 legal on R+2 (rank match)", CardRules.isLegal("B+2", "R+2", ""));
    }

    //Called color after wild

    static void testCalledColorAfterWild() {
        check("called B allows B3", CardRules.isLegal("B3", "W", "B"));
        check("called B allows BS", CardRules.isLegal("BS", "W", "B"));
        check("called B blocks R5", !CardRules.isLegal("R5", "W", "B"));
        check("called G blocks B card", !CardRules.isLegal("B1", "W", "G"));
    }

    //Illegal plays

    static void testIllegalPlays() {
        check("B3 on R9 is illegal (no match)", !CardRules.isLegal("B3", "R9", ""));
        check("R3 on GS is illegal (number vs action)", !CardRules.isLegal("R3", "GS", ""));
        check("RS on GR is illegal (skip vs reverse)", !CardRules.isLegal("RS", "GR", ""));
    }

    //Scoring

    static void testScoring() {
        check("R7 scores 7", CardRules.points("R7") == 7);
        check("G0 scores 0", CardRules.points("G0") == 0);
        check("B9 scores 9", CardRules.points("B9") == 9);
        check("BS scores 20", CardRules.points("BS") == 20);
        check("YR scores 20", CardRules.points("YR") == 20);
        check("R+2 scores 20", CardRules.points("R+2") == 20);
        check("W scores 50", CardRules.points("W") == 50);
        check("W4 scores 50", CardRules.points("W4") == 50);
    }

    //Drawing from deck

    static void testDrawFromDeck() {
        GameState gs = new GameState(0);
        gs.deck.add("R5");
        gs.deck.add("B3");
        String drawn = gs.draw();
        check("draw() returns top card", drawn.equals("R5"));
        check("deck shrinks by one after draw", gs.deck.size() == 1);
        check("remaining card is correct", gs.deck.get(0).equals("B3"));
    }

    // Edge case: when deck is empty, draw() reshuffles the discard pile.
    static void testDeckReshufflesWhenEmpty() {
        GameState gs = new GameState(0);
        gs.discard.add("G3");
        gs.discard.add("Y7");
        String drawn = gs.draw();
        check("draw from empty deck uses discard", !drawn.isEmpty());
        check("discard is cleared after reshuffle", gs.discard.isEmpty());
    }

    //Bot card selection

    static void testBotPrefersDrawTwo() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");   // 0 - legal by color if up is B-something
        hand.add("R+2");  // 1 - draw-two, legal by color
        hand.add("W");    // 2 - wild
        int idx = BotStrategy.chooseCard(hand, "R9", "");
        check("bot plays draw-two before number", idx == 1);
    }

    static void testBotPrefersSkipOverNumber() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("R5");   // 0 - number, legal
        hand.add("RS");   // 1 - skip, legal
        hand.add("W");    // 2 - wild
        int idx = BotStrategy.chooseCard(hand, "R9", "");
        check("bot plays skip before number", idx == 1);
    }

    static void testBotPrefersNumberOverWild() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");  // 0 - not legal on R9
        hand.add("R4");  // 1 - legal by color
        hand.add("W");   // 2 - wild
        int idx = BotStrategy.chooseCard(hand, "R9", "");
        check("bot plays number before wild", idx == 1);
    }

    //Bot color choice

    static void testBotChoosesMajorityColor() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B1"); hand.add("B2"); hand.add("R3");
        check("bot picks majority color B", BotStrategy.chooseColor(hand).equals("B"));

        ArrayList<String> hand2 = new ArrayList<>();
        hand2.add("R1"); hand2.add("R2"); hand2.add("R3"); hand2.add("G1");
        check("bot picks R when it dominates", BotStrategy.chooseColor(hand2).equals("R"));
    }

    // Edge case: a colored card played onto an unclaimed wild (calledColor = "") is illegal.
    // In normal gameplay calledColor is always set after a wild, but the rule engine
    // handles the empty-string case this way - document it here.
    static void testEdgeCaseColorlessOnWild() {
        check("colored card on wild with no called color is illegal",
                !CardRules.isLegal("B3", "W", ""));
    }

    // State-transition tests
    // These test that card effects actually change game state correctly.
    // They set up Main.state directly so applyCardEffect can be called in isolation.

    // Sets up n bot players in a fresh GameState and assigns it to Main.state.
    static void setupStateWith(int playerCount) {
        GameView.setQuiet(true);
        Main.state = new GameState(0);
        for (int i = 0; i < playerCount; i++) {
            Main.state.playerNames.add("P" + (i + 1));
            Main.state.humanPlayers.add(false);
            Main.state.hands.add(new ArrayList<>());
        }
        Main.state.direction = 1;
        Main.state.upCard = "R5";
        Main.state.calledColor = "";
        // Give the deck some cards so draw effects don't run dry.
        for (int i = 0; i < 20; i++) Main.state.deck.add("G3");
    }

    static void testSkipAdvancesPlayerTwice() {
        setupStateWith(3);
        Main.state.currentPlayer = 0;
        Main.applyCardEffect("RS");
        check("skip from P1 lands on P3 (skips P2)", Main.state.currentPlayer == 2);
    }

    static void testReverseFlipsDirectionInThreePlayer() {
        setupStateWith(3);
        Main.state.currentPlayer = 0;
        Main.applyCardEffect("BR");
        check("reverse flips direction to -1", Main.state.direction == -1);
        // With direction=-1, next() from 0 wraps to player 2.
        check("reverse in 3-player moves to last player", Main.state.currentPlayer == 2);
    }

    static void testReverseInTwoPlayerActsAsSkip() {
        setupStateWith(2);
        Main.state.currentPlayer = 0;
        Main.applyCardEffect("YR");
        // Direction flips to -1, then two next() calls wrap back to 0.
        check("reverse in 2-player acts as skip, stays at P1", Main.state.currentPlayer == 0);
    }

    static void testDrawTwoGivesNextPlayerTwoCards() {
        setupStateWith(2);
        Main.state.currentPlayer = 0;
        int before = Main.state.hands.get(1).size();
        Main.applyCardEffect("R+2");
        check("draw two gives next player exactly 2 cards", Main.state.hands.get(1).size() == before + 2);
        check("draw two skips the affected player's turn", Main.state.currentPlayer == 0);
    }

    static void testWildDrawFourGivesNextPlayerFourCards() {
        setupStateWith(2);
        Main.state.currentPlayer = 0;
        int before = Main.state.hands.get(1).size();
        Main.applyCardEffect("W4");
        check("wild draw four gives next player exactly 4 cards", Main.state.hands.get(1).size() == before + 4);
        check("wild draw four skips the affected player's turn", Main.state.currentPlayer == 0);
    }

    static void testNormalCardAdvancesOnce() {
        setupStateWith(3);
        Main.state.currentPlayer = 0;
        Main.applyCardEffect("R5");
        check("number card advances to next player", Main.state.currentPlayer == 1);
    }

    static void check(String name, boolean result) {
        if (result) {
            passed++;
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }
}