import java.time.LocalDateTime;

public class HighScoreReport {
    private final String playerName;
    private final int score;
    private final LocalDateTime completedAt;

    public HighScoreReport(String playerName, int score, LocalDateTime completedAt) {
        this.playerName = playerName;
        this.score = score;
        this.completedAt = completedAt;
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public LocalDateTime getCompletedAt() { return completedAt; }
}
