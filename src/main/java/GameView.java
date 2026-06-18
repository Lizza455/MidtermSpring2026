import java.util.ArrayList;
import java.util.Scanner;

// All console I/O goes through here — output and raw input reading.
// GameView never validates game rules. It just prints and reads.
// Call setQuiet(true) to suppress turn-by-turn output (final scores always print).
public class GameView {

    private static boolean quiet = false;
    private static final Scanner scanner = new Scanner(System.in);

    public static void setQuiet(boolean q) {
        quiet = q;
    }

    //Raw input reading

    public static String promptCardChoice() {
        System.out.print("Choose card index/code or draw: ");
        return scanner.nextLine().trim().toUpperCase();
    }

    public static String promptColorChoice() {
        System.out.print("Call color R/Y/G/B: ");
        return scanner.nextLine().trim().toUpperCase();
    }

    public static String promptDrawnCard(String card) {
        System.out.print("Play drawn card " + card + "? y/n: ");
        return scanner.nextLine();
    }

    //Game output

    // Final scores are always shown regardless of quiet mode.
    public static void showFinalScores(ArrayList<String> names, int[] scores) {
        System.out.println("\nFinal scores:");
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i) + ": " + scores[i]);
        }
    }

    public static void showGameHeader(int gameNumber) {
        if (quiet) return;
        System.out.println("\n=== Game " + gameNumber + " ===");
    }

    public static void showUpCard(String upCard, String calledColor) {
        if (quiet) return;
        String suffix = calledColor.isEmpty() ? "" : " called " + calledColor;
        System.out.println("\nUp card: " + upCard + suffix);
    }

    public static void showHand(String playerName, ArrayList<String> hand) {
        if (quiet) return;
        System.out.println(playerName + " hand: " + formatHand(hand));
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
        System.out.println(playerName + " calls " + color);
    }

    public static void showUno(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " says UNO!");
    }

    public static void showWin(String playerName, int points) {
        if (quiet) return;
        System.out.println(playerName + " wins and scores " + points);
    }

    public static void showPenaltyInvalidIndex(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " selected an invalid index and draws a penalty card.");
    }

    public static void showPenaltyIllegalCard(String playerName, String card) {
        if (quiet) return;
        System.out.println(playerName + " tried illegal card " + card + " and draws a penalty card.");
    }

    public static void showDrawTwo(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " draws two.");
    }

    public static void showDrawFour(String playerName) {
        if (quiet) return;
        System.out.println(playerName + " draws four.");
    }

    public static void showSafetyLimit() {
        if (quiet) return;
        System.out.println("Game stopped at safety limit.");
    }

    public static void showCardNotLegal() {
        System.out.println("That card is not legal.");
    }

    public static void showCardNotFound() {
        System.out.println("Card not found.");
    }

    // Loops until the human enters a valid color. Color validation is not a game rule.
    public static String resolveHumanColor() {
        while (true) {
            String input = promptColorChoice();
            if (input.equals("R") || input.equals("Y") || input.equals("G") || input.equals("B")) return input;
            System.out.println("Bad color.");
        }
    }

    // Formats a hand as "0:R5 1:GS 2:W" for display.
    private static String formatHand(ArrayList<String> hand) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            sb.append(i).append(":").append(hand.get(i));
            if (i < hand.size() - 1) sb.append(" ");
        }
        return sb.toString();
    }
}