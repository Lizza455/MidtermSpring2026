import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the standard 108-card UNO deck composition.
 * Covers rubric section 1.1 — Correct Deck Composition.
 */
public class DeckCompositionTest {

    private GameState state;

    @BeforeEach
    void setUp() {
        state = new GameState(42);
        state.buildDeck();
    }

    @Test
    void deck_totalSize_108() {
        assertEquals(108, state.deck.size());
    }

    @Test
    void deck_has_four_colors() {
        long red    = state.deck.stream().filter(c -> CardRules.color(c).equals("R")).count();
        long yellow = state.deck.stream().filter(c -> CardRules.color(c).equals("Y")).count();
        long green  = state.deck.stream().filter(c -> CardRules.color(c).equals("G")).count();
        long blue   = state.deck.stream().filter(c -> CardRules.color(c).equals("B")).count();
        assertEquals(25, red,    "25 red cards");
        assertEquals(25, yellow, "25 yellow cards");
        assertEquals(25, green,  "25 green cards");
        assertEquals(25, blue,   "25 blue cards");
    }

    @Test
    void deck_one_zero_per_color() {
        for (String color : new String[]{"R", "Y", "G", "B"}) {
            long count = state.deck.stream().filter(c -> c.equals(color + "0")).count();
            assertEquals(1, count, "one " + color + "0");
        }
    }

    @Test
    void deck_two_of_each_1_to_9_per_color() {
        for (String color : new String[]{"R", "Y", "G", "B"}) {
            for (int n = 1; n <= 9; n++) {
                final String card = color + n;
                long count = state.deck.stream().filter(c -> c.equals(card)).count();
                assertEquals(2, count, "two " + card);
            }
        }
    }

    @Test
    void deck_two_skip_per_color() {
        for (String color : new String[]{"R", "Y", "G", "B"}) {
            long count = state.deck.stream().filter(c -> c.equals(color + "S")).count();
            assertEquals(2, count, "two " + color + "S");
        }
    }

    @Test
    void deck_two_reverse_per_color() {
        for (String color : new String[]{"R", "Y", "G", "B"}) {
            long count = state.deck.stream().filter(c -> c.equals(color + "R")).count();
            assertEquals(2, count, "two " + color + "R");
        }
    }

    @Test
    void deck_two_drawTwo_per_color() {
        for (String color : new String[]{"R", "Y", "G", "B"}) {
            long count = state.deck.stream().filter(c -> c.equals(color + "+2")).count();
            assertEquals(2, count, "two " + color + "+2");
        }
    }

    @Test
    void deck_four_wilds() {
        long count = state.deck.stream().filter(c -> c.equals("W")).count();
        assertEquals(4, count, "four Wild cards");
    }

    @Test
    void deck_four_wildDrawFours() {
        long count = state.deck.stream().filter(c -> c.equals("W4")).count();
        assertEquals(4, count, "four Wild Draw Four cards");
    }

    @Test
    void deck_rebuild_is_fresh() {
        int firstSize = state.deck.size();
        state.deck.clear();
        state.buildDeck();
        assertEquals(firstSize, state.deck.size());
    }
}
