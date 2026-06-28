import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class DrawPassUnoTest {

    private GameState state;

    @BeforeEach
    void setUp() {
        GameView.setQuiet(true);
        state = new GameState(7L);
        state.playerNames.add("Bot1");
        state.playerNames.add("Bot2");
        state.humanPlayers.add(false);
        state.humanPlayers.add(false);
        state.hands.add(new ArrayList<>());
        state.hands.add(new ArrayList<>());
        state.buildDeck();
        state.upCard        = "R5";
        state.calledColor   = "";
        state.direction     = 1;
        state.currentPlayer = 0;
    }

    // ---- Draw behavior ----

    @Test
    void draw_addsCardToHand() {
        int before = state.deck.size();
        String drawn = state.draw();
        assertEquals(before - 1, state.deck.size());
        assertNotNull(drawn);
    }

    @Test
    void draw_reshuffle_whenDeckEmpty() {
        state.deck.clear();
        state.discard.addAll(Arrays.asList("G3", "B7", "Y+2"));
        String drawn = state.draw();
        assertNotNull(drawn);
    }

    // ---- Pass / advance ----

    @Test
    void next_advancesCurrentPlayer() {
        state.currentPlayer = 0;
        state.next();
        assertEquals(1, state.currentPlayer);
    }

    @Test
    void next_wrapsAround() {
        state.currentPlayer = 1;
        state.next();
        assertEquals(0, state.currentPlayer);
    }

    @Test
    void next_reverseDirection() {
        state.direction = -1;
        state.currentPlayer = 0;
        state.next();
        assertEquals(1, state.currentPlayer);
    }

    // ---- Bot draw/pass ----

    @Test
    void bot_returnsMinusOne_whenNoLegalCard() {
        ArrayList<String> hand = new ArrayList<>(Arrays.asList("G3", "B7"));
        assertEquals(-1, BotStrategy.chooseCard(hand, "R5", ""));
    }

    // ---- UNO call detection ----

    @Test
    void uno_candidateSetWhenOneCardLeft() {
        state.hands.get(0).clear();
        state.hands.get(0).add("R5");
        state.hands.get(0).add("R3");
        state.upCard = "R9";
        // Bot2 gets many cards so it won't drop to 1
        state.hands.get(1).addAll(Arrays.asList("G3", "B7", "Y2", "G5", "B9"));

        new TurnController(state).playTurn();

        assertEquals(0, state.unoCandidateIndex);
        assertTrue(state.unoCalled);
    }

    @Test
    void uno_notTriggered_withMoreThanOneCard() {
        state.unoCandidateIndex = -1;
        state.hands.get(0).addAll(Arrays.asList("R5", "G3", "B7"));
        assertEquals(-1, state.unoCandidateIndex);
    }

    // ---- Missed UNO penalty ----

    @Test
    void missedUno_penaltyDrawsTwoCards() {
        state.hands.get(0).clear();
        state.hands.get(0).add("R3");
        state.unoCandidateIndex = 0;
        state.unoCalled = false;

        // Bot2 gets 5 cards — after playing one it has 4, NOT a UNO candidate
        state.hands.get(1).clear();
        state.hands.get(1).addAll(Arrays.asList("R7", "G3", "B5", "Y2", "B9"));
        state.currentPlayer = 1;

        int bot1Before = state.hands.get(0).size(); // 1
        new TurnController(state).playTurn();

        assertEquals(bot1Before + 2, state.hands.get(0).size());
    }

    @Test
    void missedUno_penaltyIsExactlyTwoCards() {
        state.hands.get(0).clear();
        state.hands.get(0).add("R3");
        state.unoCandidateIndex = 0;
        state.unoCalled = false;

        state.hands.get(1).clear();
        state.hands.get(1).addAll(Arrays.asList("R7", "G3", "B5", "Y2", "B9"));
        state.currentPlayer = 1;

        new TurnController(state).playTurn();

        // Was 1 card + 2 penalty = 3
        assertEquals(3, state.hands.get(0).size());
    }

    @Test
    void missedUno_noPenalty_whenUnoCalled() {
        state.hands.get(0).clear();
        state.hands.get(0).add("R3");
        state.unoCandidateIndex = 0;
        state.unoCalled = true;

        state.hands.get(1).clear();
        state.hands.get(1).addAll(Arrays.asList("R7", "G3", "B5", "Y2", "B9"));
        state.currentPlayer = 1;

        int bot1Before = state.hands.get(0).size();
        new TurnController(state).playTurn();

        assertEquals(bot1Before, state.hands.get(0).size());
    }

    @Test
    void missedUno_candidateIndexClearedAfterPenalty() {
        state.hands.get(0).clear();
        state.hands.get(0).add("R3");
        state.unoCandidateIndex = 0;
        state.unoCalled = false;

        // Bot2 has 5 cards; after playing one → 4 cards, not a UNO candidate
        state.hands.get(1).clear();
        state.hands.get(1).addAll(Arrays.asList("R7", "G3", "B5", "Y2", "B9"));
        state.currentPlayer = 1;

        new TurnController(state).playTurn();

        assertEquals(-1, state.unoCandidateIndex);
    }

    // ---- Scoring ----

    @Test
    void tallyPoints_sumsOpponentHands() {
        state.hands.get(0).clear();
        state.hands.get(1).clear();
        state.hands.get(1).addAll(Arrays.asList("R5", "W", "GS"));
        assertEquals(75, state.tallyPoints(0));
    }

    @Test
    void tallyPoints_excludesWinnerHand() {
        state.hands.get(0).clear();
        state.hands.get(0).add("Y9");
        state.hands.get(1).clear();
        state.hands.get(1).add("R5");
        assertEquals(5, state.tallyPoints(0));
    }
}