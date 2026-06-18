import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class UnoRegressionTest {

    @Test
    void cardRulesCoverCoreUnoBehavior() {
        assertTrue(CardRules.isLegal("R5", "R9", ""));
        assertTrue(CardRules.isLegal("G9", "R9", ""));
        assertTrue(CardRules.isLegal("RS", "GS", ""));
        assertTrue(CardRules.isLegal("BR", "YR", ""));
        assertTrue(CardRules.isLegal("R+2", "B+2", ""));
        assertTrue(CardRules.isLegal("W", "R9", ""));
        assertTrue(CardRules.isLegal("W4", "B3", ""));
        assertTrue(CardRules.isLegal("B3", "W", "B"));

        assertFalse(CardRules.isLegal("R3", "G7", ""));
        assertFalse(CardRules.isLegal("RS", "GR", ""));
        assertFalse(CardRules.isLegal("B3", "R9", ""));
        assertFalse(CardRules.isLegal("B3", "W", ""));

        assertEquals("DRAW_TWO", CardRules.rank("R+2"));
        assertEquals("SKIP", CardRules.rank("RS"));
        assertEquals("REVERSE", CardRules.rank("BR"));
        assertEquals("WILD", CardRules.rank("W"));
        assertEquals("WILD_DRAW_FOUR", CardRules.rank("W4"));

        assertEquals(7, CardRules.points("R7"));
        assertEquals(20, CardRules.points("BS"));
        assertEquals(20, CardRules.points("R+2"));
        assertEquals(50, CardRules.points("W"));
        assertEquals(50, CardRules.points("W4"));
    }

    @Test
    void botStrategyUsesDeterministicPriorities() {
        ArrayList<String> hand = new ArrayList<>();
        hand.add("B3");
        hand.add("R+2");
        hand.add("W");
        assertEquals(1, BotStrategy.chooseCard(hand, "R9", ""));

        ArrayList<String> hand2 = new ArrayList<>();
        hand2.add("R5");
        hand2.add("RS");
        hand2.add("W");
        assertEquals(1, BotStrategy.chooseCard(hand2, "R9", ""));

        ArrayList<String> hand3 = new ArrayList<>();
        hand3.add("B1");
        hand3.add("B2");
        hand3.add("R3");
        assertEquals("B", BotStrategy.chooseColor(hand3));
    }

    @Test
    void cardEffectsUpdateGameStateCorrectly() {
        GameState skip = setupStateWith(3);
        new TurnController(skip).applyCardEffect("RS");
        assertEquals(2, skip.currentPlayer);

        GameState reverse = setupStateWith(3);
        new TurnController(reverse).applyCardEffect("BR");
        assertEquals(-1, reverse.direction);
        assertEquals(2, reverse.currentPlayer);

        GameState twoPlayerReverse = setupStateWith(2);
        new TurnController(twoPlayerReverse).applyCardEffect("YR");
        assertEquals(0, twoPlayerReverse.currentPlayer);

        GameState drawTwo = setupStateWith(2);
        int before = drawTwo.hands.get(1).size();
        new TurnController(drawTwo).applyCardEffect("R+2");
        assertEquals(before + 2, drawTwo.hands.get(1).size());
        assertEquals(0, drawTwo.currentPlayer);

        GameState wildDrawFour = setupStateWith(2);
        int beforeFour = wildDrawFour.hands.get(1).size();
        new TurnController(wildDrawFour).applyCardEffect("W4");
        assertEquals(beforeFour + 4, wildDrawFour.hands.get(1).size());
        assertEquals(0, wildDrawFour.currentPlayer);
    }

    @Test
    void deterministicBotGameCompletesWithMeaningfulScore() {
        GameView.setQuiet(true);

        GameState state = new GameState(123);
        state.setupPlayers(3, false);

        GameRunner runner = new GameRunner(state);
        runner.playGame();

        int totalScore = 0;
        for (int score : state.scores) {
            totalScore += score;
        }

        assertTrue(totalScore > 0, "A deterministic bot-only game should finish with a non-zero score");
    }

    private GameState setupStateWith(int playerCount) {
        GameView.setQuiet(true);
        GameState state = new GameState(0);
        for (int i = 0; i < playerCount; i++) {
            state.playerNames.add("P" + (i + 1));
            state.humanPlayers.add(false);
            state.hands.add(new ArrayList<>());
        }
        state.direction = 1;
        state.upCard = "R5";
        state.calledColor = "";
        for (int i = 0; i < 20; i++) state.deck.add("G3");
        return state;
    }
}
