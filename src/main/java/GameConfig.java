public class GameConfig {
    int bots = 3;
    int games = 1;
    boolean human = false;
    boolean quiet = false;
    boolean noDb = false;
    boolean reportRecent = false;
    boolean reportWins = false;
    boolean reportHighScores = false;
    int reportLimit = 10;
    String dbUrl = null;
    long seed = System.currentTimeMillis();
}
