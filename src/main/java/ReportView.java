import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportView {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void showRecentGames(List<UnoGameEntity> games) {
        System.out.println("\nRecent games:");
        if (games.isEmpty()) {
            System.out.println("No saved games yet.");
            return;
        }
        for (UnoGameEntity game : games) {
            String winner = game.getWinner() == null ? "NO_WINNER" : game.getWinner().getName();
            System.out.println("Game #" + game.getId()
                    + " | completed=" + FORMATTER.format(game.getCompletedAt())
                    + " | winner=" + winner
                    + " | rounds=" + game.getRoundsPlayed());
            for (PlayerScoreEntity score : game.getScores()) {
                System.out.println("  " + score.getPlayer().getName() + ": " + score.getScore());
            }
        }
    }

    public static void showWinCounts(List<WinCountReport> rows) {
        System.out.println("\nPlayer win count:");
        if (rows.isEmpty()) {
            System.out.println("No players saved yet.");
            return;
        }
        for (WinCountReport row : rows) {
            System.out.println(row.getPlayerName() + ": " + row.getWins());
        }
    }

    public static void showHighestScores(List<HighScoreReport> rows) {
        System.out.println("\nHighest scores:");
        if (rows.isEmpty()) {
            System.out.println("No saved scores yet.");
            return;
        }
        for (HighScoreReport row : rows) {
            System.out.println(row.getPlayerName()
                    + " | score=" + row.getScore()
                    + " | completed=" + FORMATTER.format(row.getCompletedAt()));
        }
    }
}
