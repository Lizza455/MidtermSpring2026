import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CardRules: color, rank, number, points, and isLegal.
 * Covers rubric sections 1.1 (deck), 1.2 (legal play), 1.3-1.7 (action cards).
 */
public class CardRulesTest {

    // ---- color() ----

    @Test
    void color_red() {
        assertEquals("R", CardRules.color("R5"));
    }

    @Test
    void color_yellow() {
        assertEquals("Y", CardRules.color("Y0"));
    }

    @Test
    void color_green() {
        assertEquals("G", CardRules.color("GS"));
    }

    @Test
    void color_blue() {
        assertEquals("B", CardRules.color("B+2"));
    }

    @Test
    void color_wild_isEmpty() {
        assertEquals("", CardRules.color("W"));
        assertEquals("", CardRules.color("W4"));
    }

    // ---- rank() ----

    @Test
    void rank_number() {
        assertEquals("NUMBER", CardRules.rank("R5"));
        assertEquals("NUMBER", CardRules.rank("B0"));
    }

    @Test
    void rank_skip() {
        assertEquals("SKIP", CardRules.rank("RS"));
        assertEquals("SKIP", CardRules.rank("GS"));
    }

    @Test
    void rank_reverse() {
        assertEquals("REVERSE", CardRules.rank("YR"));
        assertEquals("REVERSE", CardRules.rank("BR"));
    }

    @Test
    void rank_drawTwo() {
        assertEquals("DRAW_TWO", CardRules.rank("G+2"));
        assertEquals("DRAW_TWO", CardRules.rank("R+2"));
    }

    @Test
    void rank_wild() {
        assertEquals("WILD", CardRules.rank("W"));
    }

    @Test
    void rank_wildDrawFour() {
        assertEquals("WILD_DRAW_FOUR", CardRules.rank("W4"));
    }

    // ---- number() ----

    @Test
    void number_returnsDigit() {
        assertEquals(5, CardRules.number("R5"));
        assertEquals(0, CardRules.number("G0"));
        assertEquals(9, CardRules.number("B9"));
    }

    @Test
    void number_nonNumber_returnsMinusOne() {
        assertEquals(-1, CardRules.number("RS"));
        assertEquals(-1, CardRules.number("W"));
        assertEquals(-1, CardRules.number("W4"));
    }

    // ---- points() ----

    @Test
    void points_numberCard() {
        assertEquals(7, CardRules.points("R7"));
        assertEquals(0, CardRules.points("G0"));
    }

    @Test
    void points_actionCards_20() {
        assertEquals(20, CardRules.points("RS"));
        assertEquals(20, CardRules.points("YR"));
        assertEquals(20, CardRules.points("B+2"));
    }

    @Test
    void points_wilds_50() {
        assertEquals(50, CardRules.points("W"));
        assertEquals(50, CardRules.points("W4"));
    }

    // ---- isLegal() — color match ----

    @Test
    void legal_sameColor() {
        assertTrue(CardRules.isLegal("R2", "R9", ""));
        assertTrue(CardRules.isLegal("RS", "R5", ""));
    }

    @Test
    void legal_differentColorAndNumber_illegal() {
        assertFalse(CardRules.isLegal("B3", "R9", ""));
    }

    // ---- isLegal() — number match ----

    @Test
    void legal_sameNumber_differentColor() {
        assertTrue(CardRules.isLegal("G9", "R9", ""));
        assertTrue(CardRules.isLegal("B0", "Y0", ""));
    }

    // ---- isLegal() — action type match ----

    @Test
    void legal_skipOnSkip() {
        assertTrue(CardRules.isLegal("RS", "GS", ""));
    }

    @Test
    void legal_reverseOnReverse() {
        assertTrue(CardRules.isLegal("YR", "BR", ""));
    }

    @Test
    void legal_drawTwoOnDrawTwo() {
        assertTrue(CardRules.isLegal("R+2", "G+2", ""));
    }

    @Test
    void illegal_skipOnReverse() {
        assertFalse(CardRules.isLegal("RS", "GR", ""));
    }

    // ---- isLegal() — wilds always legal ----

    @Test
    void legal_wildAlwaysLegal() {
        assertTrue(CardRules.isLegal("W", "R9", ""));
        assertTrue(CardRules.isLegal("W", "B+2", "Y"));
    }

    @Test
    void legal_wildDrawFourAlwaysLegal() {
        assertTrue(CardRules.isLegal("W4", "G7", ""));
        assertTrue(CardRules.isLegal("W4", "RS", "R"));
    }

    // ---- isLegal() — calledColor after wild ----

    @Test
    void legal_calledColorMatch() {
        assertTrue(CardRules.isLegal("B3", "W", "B"));
        assertTrue(CardRules.isLegal("BS", "W4", "B"));
    }

    @Test
    void illegal_calledColorMismatch() {
        assertFalse(CardRules.isLegal("G3", "W", "B"));
        assertFalse(CardRules.isLegal("R5", "W4", "Y"));
    }

    @Test
    void illegal_calledColorBlocksColorMatch() {
        // Even if card color matches upCard color, calledColor takes precedence
        assertFalse(CardRules.isLegal("G3", "R9", "Y"));
    }
}
