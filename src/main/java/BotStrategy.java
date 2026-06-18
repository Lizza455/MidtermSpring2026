import java.util.ArrayList;

// Simple bot strategy.
// The bot tries to play stronger action cards first, keeps wilds for later,
// and only draws when it has no legal move.
public class BotStrategy {

    // Chooses which card to play from the hand.
    // Returns the card index, or -1 if no legal card can be played.
    public static int chooseCard(ArrayList<String> hand, String upCard, String calledColor) {

        // Play cards in this order of preference.
        // Action cards are prioritized because they affect other players.
        String[] priority = {
                "DRAW_TWO",
                "SKIP",
                "REVERSE",
                "NUMBER",
                "WILD_DRAW_FOUR",
                "WILD"
        };

        // Look for the first legal card that matches the current priority.
        for (String wantedRank : priority) {
            for (int i = 0; i < hand.size(); i++) {
                String card = hand.get(i);

                if (CardRules.rank(card).equals(wantedRank)
                        && CardRules.isLegal(card, upCard, calledColor)) {
                    return i;
                }
            }
        }

        // No legal card found, so the bot must draw.
        return -1;
    }

    // Chooses a color after playing a wild card.
    // The bot simply picks the color it currently has the most of.
    public static String chooseColor(ArrayList<String> hand) {
        int r = 0, y = 0, g = 0, b = 0;

        for (String card : hand) {
            String c = CardRules.color(card);

            if (c.equals("R")) r++;
            else if (c.equals("Y")) y++;
            else if (c.equals("G")) g++;
            else if (c.equals("B")) b++;
        }

        if (r >= y && r >= g && r >= b) return "R";
        if (y >= r && y >= g && y >= b) return "Y";
        if (g >= r && g >= y && g >= b) return "G";
        return "B";
    }
}