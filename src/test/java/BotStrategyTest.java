import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BotStrategy — card selection and color choice.
 */
public class BotStrategyTest {

    @Test
    void chooseCard_returnsMinusOne_whenNoLegalCard() {
        ArrayList<String> hand = new ArrayList<>(Arrays.asList("G3", "B7"));
        int chosen = BotStrategy.chooseCard(hand, "R5", "");
        assertEquals(-1, chosen, "No legal card -> -1");
    }

    @Test
    void chooseCard_playsLegalNumberCard() {
        ArrayList<String> hand = new ArrayList<>(Arrays.asList("R5", "G3"));
        int chosen = BotStrategy.chooseCard(hand, "R9", "");
        assertNotEquals(-1, chosen);
        assertTrue(CardRules.isLegal(hand.get(chosen), "R9", ""));
    }

    @Test
    void chooseCard_prefersActionOverNumber() {
        ArrayList<String> hand = new ArrayList<>(Arrays.asList("R5", "R+2", "W"));
        // R+2 is action, should be preferred over R5 and before W
        int chosen = BotStrategy.chooseCard(hand, "R9", "");
        assertEquals("R+2", hand.get(chosen), "Bot should prefer Draw Two over number");
    }

    @Test
    void chooseCard_prefersNumberOverWild() {
        ArrayList<String> hand = new ArrayList<>(Arrays.asList("R4", "W"));
        int chosen = BotStrategy.chooseCard(hand, "R9", "");
        assertEquals(0, chosen, "Bot should play number card before Wild");
    }

    @Test
    void chooseCard_playsWild_whenOnlyOption() {
        ArrayList<String> hand = new ArrayList<>(Arrays.asList("G3", "W"));
        int chosen = BotStrategy.chooseCard(hand, "R9", "");
        // G3 not legal on R9, W always legal
        assertEquals(1, chosen, "Bot plays Wild when it's the only legal option");
    }

    @Test
    void chooseCard_respectsCalledColor() {
        ArrayList<String> hand = new ArrayList<>(Arrays.asList("R5", "G5", "B3"));
        // calledColor = G, so only G5 is legal
        int chosen = BotStrategy.chooseCard(hand, "W", "G");
        assertNotEquals(-1, chosen);
        assertEquals("G5", hand.get(chosen));
    }

    @Test
    void chooseColor_picksColorWithMostCards() {
        ArrayList<String> hand = new ArrayList<>(Arrays.asList("B1", "B2", "B3", "R4", "G5"));
        String color = BotStrategy.chooseColor(hand);
        assertEquals("B", color, "Bot chooses color with most cards");
    }

    @Test
    void chooseColor_handWithNoColoredCards_returnsSomething() {
        ArrayList<String> hand = new ArrayList<>(Arrays.asList("W", "W4"));
        String color = BotStrategy.chooseColor(hand);
        assertTrue(color.equals("R") || color.equals("Y") || color.equals("G") || color.equals("B"),
                "Should return a valid color even with no colored cards");
    }
}
