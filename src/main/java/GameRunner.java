import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

public class GameRunner {
    private final GameState state;
    private final TurnController turnController;

    public GameRunner(GameState state) {
        this.state = state;
        this.turnController = new TurnController(state);
    }

    public GameResult playGame() {
        LocalDateTime startedAt = LocalDateTime.now();
        state.buildDeck();
        Collections.shuffle(state.deck, state.random);
        state.discard.clear();
        state.replayLog.clear();
        for (ArrayList<String> hand : state.hands) hand.clear();

        for (int i = 0; i < state.playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) state.hands.get(i).add(state.draw());
        }

        state.upCard = state.draw();
        while (state.upCard.startsWith("W")) {
            state.discard.add(state.upCard);
            state.upCard = state.draw();
        }

        state.calledColor = "";
        state.direction = 1;
        state.currentPlayer = state.random.nextInt(state.playerNames.size());
        log("GAME_START firstPlayer=" + state.currentPlayerName() + " upCard=" + state.upCard);

        int turnsPlayed = 0;
        while (turnsPlayed < 300) {
            turnsPlayed++;
            int playerBeforeTurn = state.currentPlayer;
            if (turnController.playTurn()) {
                String winnerName = state.playerNames.get(playerBeforeTurn);
                int pointsScored = state.lastRoundPoints;
                return new GameResult(
                        startedAt,
                        LocalDateTime.now(),
                        state.playerNames,
                        state.scores,
                        winnerName,
                        pointsScored,
                        1,
                        turnsPlayed
                );
            }
        }

        log("GAME_END safetyLimitReached=true");
        GameView.showSafetyLimit();
        return new GameResult(
                startedAt,
                LocalDateTime.now(),
                state.playerNames,
                state.scores,
                "NO_WINNER",
                0,
                1,
                turnsPlayed
        );
    }

    private void log(String event) {
        state.log(event);
        GameLogger.info(event);
    }
}
