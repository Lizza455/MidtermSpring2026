import java.util.ArrayList;
import java.util.Scanner;

/**
 * All console I/O lives here — output and raw input reading.
 * GameView never validates game rules; it only prints and reads.
 * Call setQuiet(true) to suppress turn-by-turn output;
 * scores, penalties, and UNO events always print.
 */
public class GameView {

    private static boolean quiet = false;
    private static final Scanner scanner = new Scanner(System.in);

    public static void setQuiet(boolean q) { quiet = q; }

    // ---- Raw input reading ----

    public static String promptCardChoice() {
        System.out.print("Choose card (index / card code / DRAW): ");
        return scanner.nextLine().trim().toUpperCase();
    }

    public static String promptColorChoice() {
        System.out.print("Call color R/Y/G/B: ");
        return scanner.nextLine().trim().toUpperCase();
    }

    public static String promptDrawnCard(String card) {
        System.out.print("You drew " + card + ". Play it? (y/n): ");
        return scanner.nextLine().trim();
    }

    /**
     * Prompts the human to call UNO after playing down to 1 card.
     * They must type "UNO" to successfully call it; anything else is a missed call
     * and will trigger a 2-card penalty at the start of the next player's turn.
     */
    public static String promptUnoCall() {
        System.out.print("You have 1 card left! Type UNO to call it (or press Enter to skip): ");
        return scanner.nextLine().trim();
    }

    // ---- Game output ----

    public static void showFinalScores(ArrayList<String> names, int[] scores) {
        System.out.println("\n=== Final Scores ===");
        for (int i = 0; i < names.size(); i++) {
            System.out.printf("  %-10s %d%n", names.get(i) + ":", scores[i]);
        }
    }

    public static void showRoundScores(ArrayList<String> names, int[] scores, int round) {
        System.out.println("\n--- After Round " + round + " ---");
        for (int i = 0; i < names.size(); i++) {
            System.out.printf("  %-10s %d%n", names.get(i) + ":", scores[i]);
        }
    }

    public static void showGameHeader(int gameNumber) {
        System.out.println("\n========== Game " + gameNumber + " ==========");
    }

    public static void showUpCard(String upCard, String calledColor) {
        if (quiet) return;
        String suffix = calledColor.isEmpty() ? "" : " (called " + calledColor + ")";
        System.out.println("Up card: " + upCard + suffix);
    }

    public static void showHand(String playerName, ArrayList<String> hand) {
        if (quiet) return;
        System.out.println(playerName + "'s hand: " + formatHand(hand));
    }

    public static void showDraw(String playerName, String card) {
        if (quiet) return;
        System.out.println(playerName + " draws " + card);
    }

    public static void showPlay(String playerName, String card) {
        if (quiet) return;
        System.out.println(playerName + " plays " + card);
    }

    public static void showCalledColor(String playerName, String color) {
        if (quiet) return;
        System.out.println(playerName + " calls color: " + color);
    }

    public static void showUno(String playerName) {
        // UNO call always shown regardless of quiet mode
        System.out.println("*** " + playerName + " says UNO! ***");
    }

    /** Always shown — missed UNO penalties are game-critical events. */
    public static void showMissedUnoPenalty(String playerName) {
        System.out.println("*** " + playerName + " forgot to call UNO and draws 2 penalty cards! ***");
    }

    public static void showWin(String playerName, int points) {
        if (quiet) return;
        System.out.println(playerName + " wins the round and scores " + points + " points!");
    }

    public static void showPenaltyInvalidIndex(String playerName) {
        System.out.println(playerName + " chose an invalid index — draws a penalty card.");
    }

    public static void showPenaltyIllegalCard(String playerName, String card) {
        System.out.println(playerName + " tried illegal card " + card + " — draws a penalty card.");
    }

    public static void showDrawTwo(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " must draw two cards and skip their turn.");
    }

    public static void showDrawFour(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " must draw four cards and skip their turn.");
    }

    public static void showSafetyLimit() {
        System.out.println("Safety limit reached — game ended without a winner.");
    }

    public static void showCardNotLegal() {
        System.out.println("That card is not legal to play right now.");
    }

    public static void showCardNotFound() {
        System.out.println("Card not found in hand. Try an index (0, 1, 2...) or card code (e.g. R5, W).");
    }

    /** Loops until the human enters a valid color (R/Y/G/B). */
    public static String resolveHumanColor() {
        while (true) {
            String input = promptColorChoice();
            if (input.equals("R") || input.equals("Y")
                    || input.equals("G") || input.equals("B")) return input;
            System.out.println("Invalid color. Please enter R, Y, G, or B.");
        }
    }

    /** Formats a hand for display: "0:R5  1:GS  2:W" */
    private static String formatHand(ArrayList<String> hand) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            sb.append(i).append(":").append(hand.get(i));
            if (i < hand.size() - 1) sb.append("  ");
        }
        return sb.toString();
    }
}
