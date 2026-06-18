import java.util.ArrayList;
import java.util.Collections;

public class GameRunner {
    private final GameState state;
    private final TurnController turnController;

    public GameRunner(GameState state) {
        this.state = state;
        this.turnController = new TurnController(state);
    }

    public void playGame() {
        state.buildDeck();
        Collections.shuffle(state.deck, state.random);
        state.discard.clear();
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

        int guard = 0;
        while (guard < 300) {
            guard++;
            if (turnController.playTurn()) return;
        }

        log("GAME_END safetyLimitReached=true");
        GameView.showSafetyLimit();
    }

    private void log(String event) {
        state.log(event);
        GameLogger.info(event);
    }
}
