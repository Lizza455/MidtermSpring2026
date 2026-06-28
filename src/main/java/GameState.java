import java.util.*;

/**
 * All mutable game state kept in one place.
 * Supports multi-round play to a target score (default 500).
 */
public class GameState {
    // --- Player data ---
    ArrayList<String> playerNames   = new ArrayList<>();
    ArrayList<Boolean> humanPlayers = new ArrayList<>();
    ArrayList<ArrayList<String>> hands = new ArrayList<>();

    // --- Card piles ---
    ArrayList<String> deck    = new ArrayList<>();
    ArrayList<String> discard = new ArrayList<>();

    // --- Scores (cumulative across rounds) ---
    int[] scores = new int[10];

    // --- Turn state ---
    int currentPlayer = 0;
    int direction     = 1;   // 1 = clockwise, -1 = counterclockwise
    String upCard      = "";
    String calledColor = "";

    // --- Round bookkeeping ---
    int lastRoundPoints = 0;
    int roundsPlayed    = 0;

    // --- Multi-round target ---
    int targetScore = 500;

    // --- UNO penalty tracking ---
    // Index of the player who is currently at 1 card and has NOT yet been challenged.
    // -1 means no player is in the pending-UNO state.
    int unoCandidateIndex = -1;
    // Whether the candidate already called UNO (announced it).
    boolean unoCalled = false;

    // --- RNG ---
    Random random;

    // --- Replay log ---
    ArrayList<String> replayLog = new ArrayList<>();

    GameState(long seed) {
        this.random = new Random(seed);
    }

    void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
        Arrays.fill(scores, 0);
        if (human) {
            playerNames.add("You");
            humanPlayers.add(true);
            hands.add(new ArrayList<>());
        }
        for (int i = 1; i <= bots; i++) {
            playerNames.add("Bot" + i);
            humanPlayers.add(false);
            hands.add(new ArrayList<>());
        }
    }

    /**
     * Draws the top card from the deck.
     * Reshuffles the discard pile back into the deck when empty.
     */
    String draw() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) return "W"; // extremely rare edge case — documented
        return deck.remove(0);
    }

    /**
     * Advances currentPlayer one step in the current direction,
     * wrapping around at the ends.
     */
    void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = playerNames.size() - 1;
    }

    /**
     * Sums the point value of all cards in every hand except the winner's.
     */
    int tallyPoints(int winnerIndex) {
        int total = 0;
        for (int i = 0; i < hands.size(); i++) {
            if (i != winnerIndex) {
                for (String card : hands.get(i)) {
                    total += CardRules.points(card);
                }
            }
        }
        return total;
    }

    boolean isHuman(int playerIndex) {
        return humanPlayers.get(playerIndex);
    }

    String currentPlayerName() {
        return playerNames.get(currentPlayer);
    }

    ArrayList<String> currentHand() {
        return hands.get(currentPlayer);
    }

    /**
     * Returns the index of the first player who has reached or exceeded targetScore,
     * or -1 if no one has won yet.
     */
    int overallWinnerIndex() {
        for (int i = 0; i < playerNames.size(); i++) {
            if (scores[i] >= targetScore) return i;
        }
        return -1;
    }

    /**
     * Builds the standard 108-card UNO deck (unshuffled).
     * 4 colors × (one 0, two each of 1–9, two Skip, two Reverse, two Draw Two)
     * + 4 Wild + 4 Wild Draw Four = 108 cards total.
     */
    void buildDeck() {
        deck.clear();
        String[] colors = {"R", "Y", "G", "B"};
        for (String c : colors) {
            deck.add(c + "0");
            for (int n = 1; n <= 9; n++) { deck.add(c + n); deck.add(c + n); }
            deck.add(c + "S"); deck.add(c + "S");
            deck.add(c + "R"); deck.add(c + "R");
            deck.add(c + "+2"); deck.add(c + "+2");
        }
        for (int i = 0; i < 4; i++) { deck.add("W"); deck.add("W4"); }
    }

    void log(String event) {
        replayLog.add(event);
    }
}
