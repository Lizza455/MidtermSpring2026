import java.util.*;

// All mutable game states kept in one place to make  the turn loop in Main easier to follow and extensions simpler to add.
public class GameState {
    ArrayList<String> playerNames = new ArrayList<>();
    ArrayList<Boolean> humanPlayers = new ArrayList<>();
    ArrayList<ArrayList<String>> hands = new ArrayList<>();
    ArrayList<String> deck = new ArrayList<>();
    ArrayList<String> discard = new ArrayList<>();
    int[] scores = new int[10];
    int currentPlayer = 0;
    int direction = 1;
    String upCard = "";
    String calledColor = "";
    int lastRoundPoints = 0;
    Random random;

    // Extension hook: each element is one logged turn event.
    // Call log() during playTurn to enable a full replay log feature.
    ArrayList<String> replayLog = new ArrayList<>();

    GameState(long seed) {
        this.random = new Random(seed);
    }

    void setupPlayers(int bots, boolean human) {
        playerNames.clear();
        humanPlayers.clear();
        hands.clear();
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

    // Draws the top card, reshuffling the discard pile into the deck if needed.
    String draw() {
        if (deck.isEmpty()) {
            deck.addAll(discard);
            discard.clear();
            Collections.shuffle(deck, random);
        }
        if (deck.isEmpty()) return "W"; // both piles exhausted — documented edge case
        return deck.remove(0);
    }

    // Advances currentPlayer one step in the current direction.
    void next() {
        currentPlayer += direction;
        if (currentPlayer >= playerNames.size()) currentPlayer = 0;
        if (currentPlayer < 0) currentPlayer = playerNames.size() - 1;
    }

    // Sums the card values in all hands except the winner's.
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

    // Builds the standard 108-card UNO deck into deck (unshuffled).
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

    // Appends a turn event to the replay log.
    void log(String event) {
        replayLog.add(event);
    }
}