public class WinCountReport {
    private final String playerName;
    private final long wins;

    public WinCountReport(String playerName, long wins) {
        this.playerName = playerName;
        this.wins = wins;
    }

    public String getPlayerName() { return playerName; }
    public long getWins() { return wins; }
}
