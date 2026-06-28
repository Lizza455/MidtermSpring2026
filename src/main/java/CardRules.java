/**
 * Card parsing and rule logic.
 *
 * Card encoding:
 *   "R0".."R9", "Y0".."Y9", "G0".."G9", "B0".."B9"  — number cards
 *   "RS", "YS", "GS", "BS"                             — Skip
 *   "RR", "YR", "GR", "BR"                             — Reverse
 *   "R+2", "Y+2", "G+2", "B+2"                        — Draw Two
 *   "W"                                                 — Wild
 *   "W4"                                                — Wild Draw Four
 */
public class CardRules {

    // Returns the color prefix of a card ("R", "Y", "G", "B"), or "" for wilds.
    public static String color(String card) {
        if (card == null || card.isEmpty()) return "";
        if (card.startsWith("R")) return "R";
        if (card.startsWith("Y")) return "Y";
        if (card.startsWith("G")) return "G";
        if (card.startsWith("B")) return "B";
        return "";
    }

    // Returns a symbolic rank name: WILD, WILD_DRAW_FOUR, SKIP, REVERSE, DRAW_TWO, or NUMBER.
    public static String rank(String card) {
        if (card == null) return "NUMBER";
        if (card.equals("W"))    return "WILD";
        if (card.equals("W4"))   return "WILD_DRAW_FOUR";
        if (card.endsWith("S"))  return "SKIP";
        if (card.endsWith("R") && card.length() == 2) return "REVERSE";
        if (card.endsWith("+2")) return "DRAW_TWO";
        return "NUMBER";
    }

    // Extracts the numeric face value for number cards; returns -1 for non-number cards.
    public static int number(String card) {
        if (rank(card).equals("NUMBER")) {
            try {
                return Integer.parseInt(card.substring(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Scoring value of a card when tallying a losing hand.
     * Number cards: face value (0-9)
     * Skip / Reverse / Draw Two: 20
     * Wild / Wild Draw Four: 50
     */
    public static int points(String card) {
        String r = rank(card);
        if (r.equals("NUMBER"))         return Math.max(0, number(card));
        if (r.equals("SKIP") || r.equals("REVERSE") || r.equals("DRAW_TWO")) return 20;
        if (r.equals("WILD") || r.equals("WILD_DRAW_FOUR"))                   return 50;
        return 0;
    }

    /**
     * Returns true when card is legal to play given the current upCard and calledColor.
     *
     * Rules:
     *  1. Wild and Wild Draw Four are always legal.
     *  2. If a color was called (after a wild), only match that color.
     *  3. Otherwise match the upCard's color.
     *  4. Match by same action-type (Skip/Reverse/Draw Two).
     *  5. Match by same number (both must be NUMBER rank).
     */
    public static boolean isLegal(String card, String upCard, String calledColor) {
        if (card == null || upCard == null) return false;

        // Wilds are always legal
        if (card.startsWith("W")) return true;

        String cardColor = color(card);
        String cardRank  = rank(card);
        String upRank    = rank(upCard);

        // After a wild: only the called color matters
        if (calledColor != null && !calledColor.isEmpty()) {
            return cardColor.equals(calledColor);
        }

        // Match by color
        if (!cardColor.isEmpty() && cardColor.equals(color(upCard))) return true;

        // Match action type (Skip on Skip, Reverse on Reverse, Draw Two on Draw Two)
        if (!cardRank.equals("NUMBER") && cardRank.equals(upRank)) return true;

        // Match by number
        if (cardRank.equals("NUMBER") && upRank.equals("NUMBER")
                && number(card) == number(upCard)) return true;

        return false;
    }

    /**
     * Returns true if the given string is a valid UNO card code.
     */
    public static boolean isValidCard(String card) {
        if (card == null || card.isEmpty()) return false;
        if (card.equals("W") || card.equals("W4")) return true;
        String c = color(card);
        if (c.isEmpty()) return false;
        String rest = card.substring(1);
        if (rest.equals("S") || rest.equals("R") || rest.equals("+2")) return true;
        try {
            int n = Integer.parseInt(rest);
            return n >= 0 && n <= 9;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
