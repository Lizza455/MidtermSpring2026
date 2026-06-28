import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Skip, Reverse, and Draw Two action card effects.
 * Covers rubric sections 1.3, 1.4, 1.5.
 */
public class ActionCardTest {

    private GameState state;
    private TurnController tc;

    @BeforeEach
    void setUp() {
        state = new GameState(42);
        state.setupPlayers(3, false); // Bot1, Bot2, Bot3, Bot4
        state.buildDeck();
        for (int i = 0; i < state.playerNames.size(); i++) {
            state.hands.get(i).add("R5"); // give everyone a safe card
            state.hands.get(i).add("R3");
        }
        state.currentPlayer = 0;
        state.direction     = 1;
        state.upCard        = "R0";
        state.calledColor   = "";
        tc = new TurnController(state);
    }

    // ---- Skip ----

    @Test
    void skip_nextPlayerLosesTurn_threePlayer() {
        // Player 0 plays RS on upCard R0 (same color)
        state.upCard = "R0";
        state.hands.get(0).clear();
        state.hands.get(0).add("RS");
        state.hands.get(0).add("R5");

        tc.applyCardEffect("RS");

        // Should skip player 1, land on player 2
        assertEquals(2, state.currentPlayer, "Skip should land on player 2");
    }

    @Test
    void skip_legalOnSameColorAndSameType() {
        assertTrue(CardRules.isLegal("RS", "R5", ""), "Skip legal on same color");
        assertTrue(CardRules.isLegal("RS", "GS", ""), "Skip legal on same type");
        assertFalse(CardRules.isLegal("RS", "G7", ""), "Skip illegal different color/type");
    }

    // ---- Reverse ----

    @Test
    void reverse_changesDirection_multiPlayer() {
        assertEquals(1, state.direction);
        tc.applyCardEffect("YR");
        assertEquals(-1, state.direction, "Reverse should flip direction to -1");
    }

    @Test
    void reverse_twoPlayer_actsLikeSkip() {
        // Setup 2-player game
        GameState s2 = new GameState(99);
        s2.setupPlayers(1, false); // Bot1 only — so You + Bot1 = 2 players
        s2.setupPlayers(0, false);
        // Manually set 2 players
        s2.playerNames.clear();
        s2.humanPlayers.clear();
        s2.hands.clear();
        s2.playerNames.add("A");
        s2.playerNames.add("B");
        s2.humanPlayers.add(false);
        s2.humanPlayers.add(false);
        s2.hands.add(new ArrayList<>(Arrays.asList("R5", "R3")));
        s2.hands.add(new ArrayList<>(Arrays.asList("R5", "R3")));
        s2.currentPlayer = 0;
        s2.direction = 1;
        s2.upCard = "R0";
        s2.calledColor = "";

        TurnController tc2 = new TurnController(s2);
        tc2.applyCardEffect("YR");

        // direction flips, then 2-player Reverse acts like Skip — current player stays same
        assertEquals(0, s2.currentPlayer, "Reverse in 2P should return to same player");
    }

    @Test
    void reverse_legalOnSameColorAndType() {
        assertTrue(CardRules.isLegal("RR", "R5", ""), "Reverse legal same color");
        assertTrue(CardRules.isLegal("GR", "YR", ""), "Reverse legal same type");
    }

    // ---- Draw Two ----

    @Test
    void drawTwo_nextPlayerDrawsTwoCards() {
        int before = state.hands.get(1).size();
        tc.applyCardEffect("R+2");
        int after = state.hands.get(1).size();
        assertEquals(before + 2, after, "Next player should have 2 more cards");
    }

    @Test
    void drawTwo_nextPlayerLosesTurn() {
        // After Draw Two on player 0, turn should skip player 1 and go to player 2
        tc.applyCardEffect("R+2");
        assertEquals(2, state.currentPlayer, "Draw Two skips the drawing player");
    }

    @Test
    void drawTwo_legalOnSameColorAndType() {
        assertTrue(CardRules.isLegal("R+2", "R7", ""), "DrawTwo legal same color");
        assertTrue(CardRules.isLegal("G+2", "Y+2", ""), "DrawTwo legal same type");
        assertFalse(CardRules.isLegal("R+2", "G7", ""), "DrawTwo illegal different");
    }

    // ---- Wild ----

    @Test
    void wild_alwaysLegal() {
        assertTrue(CardRules.isLegal("W", "R5", ""));
        assertTrue(CardRules.isLegal("W", "B+2", "Y"));
        assertTrue(CardRules.isLegal("W", "GR", "G"));
    }

    @Test
    void wild_calledColorUsedForNextPlay() {
        // After wild sets color to G, G cards should be legal
        assertTrue(CardRules.isLegal("G3", "W", "G"));
        assertFalse(CardRules.isLegal("R3", "W", "G"));
    }

    // ---- Wild Draw Four ----

    @Test
    void wildDrawFour_nextPlayerDrawsFourCards() {
        state.upCard = "R5";
        state.calledColor = "";
        int before = state.hands.get(1).size();
        tc.applyCardEffect("W4");
        int after = state.hands.get(1).size();
        assertEquals(before + 4, after, "Next player draws 4 cards");
    }

    @Test
    void wildDrawFour_nextPlayerLosesTurn() {
        state.upCard = "R5";
        state.calledColor = "";
        tc.applyCardEffect("W4");
        // Player 1 is skipped, should be at player 2
        assertEquals(2, state.currentPlayer, "W4 skips the drawing player");
    }

    @Test
    void wildDrawFour_alwaysLegal() {
        assertTrue(CardRules.isLegal("W4", "R9", ""));
        assertTrue(CardRules.isLegal("W4", "BS", "B"));
        assertTrue(CardRules.isLegal("W4", "G+2", "Y"));
    }
}
