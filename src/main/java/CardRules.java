// Card parsing and rule logic
public class CardRules {

    // Returns the color prefix of a card ("R", "Y", "G", "B"), or "" for wilds.
    public static String color(String card) {
        if (card.startsWith("R")) return "R";
        if (card.startsWith("Y")) return "Y";
        if (card.startsWith("G")) return "G";
        if (card.startsWith("B")) return "B";
        return "";
    }

    // Returns a symbolic rank name: WILD, WILD_DRAW_FOUR, SKIP, REVERSE, DRAW_TWO, or NUMBER.
    public static String rank(String card) {
        if (card.equals("W"))    return "WILD";
        if (card.equals("W4"))   return "WILD_DRAW_FOUR";
        if (card.endsWith("S"))  return "SKIP";
        if (card.endsWith("R"))  return "REVERSE";
        if (card.endsWith("+2")) return "DRAW_TWO";
        return "NUMBER";
    }

    // Extracts the numeric face value for number cards; returns -1 for non-number cards.
    public static int number(String card) {
        if (rank(card).equals("NUMBER")) {
            return Integer.parseInt(card.substring(1));
        }
        return -1;
    }

    // Scoring value of a card when tallying a losing hand.
    public static int points(String card) {
        String r = rank(card);
        if (r.equals("NUMBER"))                                        return number(card);
        if (r.equals("SKIP") || r.equals("REVERSE") || r.equals("DRAW_TWO")) return 20;
        if (r.equals("WILD") || r.equals("WILD_DRAW_FOUR"))           return 50;
        return 0;
    }
    // Legality checking and truth validity, used by both the turn loop and bots.
    public static boolean isLegal(String card, String upCard, String calledColor) {
        if (card.startsWith("W"))                                              return true;
        if (color(card).equals(color(upCard)))                                 return true;
        if (!calledColor.isEmpty() && color(card).equals(calledColor))         return true;
        if (rank(card).equals(rank(upCard)) && !rank(card).equals("NUMBER"))   return true;
        if (rank(card).equals("NUMBER") && rank(upCard).equals("NUMBER")
                && number(card) == number(upCard))                             return true;
        return false;
    }
}