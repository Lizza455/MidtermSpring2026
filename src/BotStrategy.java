import java.util.ArrayList;

// Bot decision logic
public class BotStrategy {

    // Picks an index in hand to play, or -1 to draw.
    // Priorities set : draw-two > skip > number card > wild.
    public static int chooseCard(ArrayList<String> hand, String upCard, String calledColor) {
        for (int i = 0; i < hand.size(); i++) {
            if (CardRules.rank(hand.get(i)).equals("DRAW_TWO")
                    && CardRules.isLegal(hand.get(i), upCard, calledColor)) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            if (CardRules.rank(hand.get(i)).equals("SKIP")
                    && CardRules.isLegal(hand.get(i), upCard, calledColor)) {
                return i;
            }
        }
        for (int i = 0; i < hand.size(); i++) {
            if (CardRules.rank(hand.get(i)).equals("NUMBER")
                    && CardRules.isLegal(hand.get(i), upCard, calledColor)) {
                return i;
            }
        }
        // Wilds are always legal; play one only as a last resort.
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).startsWith("W")) return i;
        }
        return -1;
    }

    // Picks the color the bot holds the most of after playing a wild.
    public static String chooseColor(ArrayList<String> hand) {
        int r = 0, y = 0, g = 0, b = 0;
        for (String card : hand) {
            String c = CardRules.color(card);
            if (c.equals("R"))      r++;
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