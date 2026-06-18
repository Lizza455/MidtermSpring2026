import java.time.LocalDateTime;
import java.util.ArrayList;

public class GameResult {
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final ArrayList<String> playerNames;
    private final int[] finalScores;
    private final String winnerName;
    private final int pointsScored;
    private final int roundsPlayed;
    private final int turnsPlayed;

    public GameResult(LocalDateTime startedAt,
                      LocalDateTime completedAt,
                      ArrayList<String> playerNames,
                      int[] finalScores,
                      String winnerName,
                      int pointsScored,
                      int roundsPlayed,
                      int turnsPlayed) {
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.playerNames = new ArrayList<>(playerNames);
        this.finalScores = finalScores.clone();
        this.winnerName = winnerName;
        this.pointsScored = pointsScored;
        this.roundsPlayed = roundsPlayed;
        this.turnsPlayed = turnsPlayed;
    }

    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public ArrayList<String> getPlayerNames() { return new ArrayList<>(playerNames); }
    public int[] getFinalScores() { return finalScores.clone(); }
    public String getWinnerName() { return winnerName; }
    public int getPointsScored() { return pointsScored; }
    public int getRoundsPlayed() { return roundsPlayed; }
    public int getTurnsPlayed() { return turnsPlayed; }
}
