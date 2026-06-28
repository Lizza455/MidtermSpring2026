import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Orchestrates a multi-round UNO game.
 * Rounds continue until a player reaches or exceeds targetScore.
 */
public class GameRunner {
    private final GameState state;
    private final TurnController turnController;

    public GameRunner(GameState state) {
        this.state = state;
        this.turnController = new TurnController(state);
    }

    /**
     * Plays a complete game (multiple rounds) until a player reaches targetScore.
     * Returns a GameResult describing the final outcome.
     */
    public GameResult playGame() {
        LocalDateTime startedAt = LocalDateTime.now();
        state.roundsPlayed = 0;
        for (int i = 0; i < state.playerNames.size(); i++) state.scores[i] = 0;
        state.replayLog.clear();

        int totalTurns  = 0;
        String overallWinner = "NO_WINNER";

        while (true) {
            state.roundsPlayed++;
            setupRound();

            log("ROUND_START round=" + state.roundsPlayed
                    + " firstPlayer=" + state.currentPlayerName()
                    + " upCard=" + state.upCard);

            // Apply action-card effect for the starting upCard
            applyStartCardEffect(state.upCard);

            // Play turns until round ends
            int roundTurns  = 0;
            boolean roundDone = false;
            while (roundTurns < 500) {
                roundTurns++;
                totalTurns++;
                if (turnController.playTurn()) {
                    roundDone = true;
                    break;
                }
            }

            if (!roundDone) {
                log("SAFETY_LIMIT_REACHED round=" + state.roundsPlayed);
                GameView.showSafetyLimit();
                break;
            }

            log("ROUND_END round=" + state.roundsPlayed);
            GameView.showRoundScores(state.playerNames, state.scores, state.roundsPlayed);

            // Check overall game winner
            int winnerIdx = state.overallWinnerIndex();
            if (winnerIdx >= 0) {
                overallWinner = state.playerNames.get(winnerIdx);
                log("GAME_END winner=" + overallWinner
                        + " score=" + state.scores[winnerIdx]
                        + " rounds=" + state.roundsPlayed);
                break;
            }
        }

        int[] finalScores = java.util.Arrays.copyOf(state.scores, state.playerNames.size());
        return new GameResult(
                startedAt,
                LocalDateTime.now(),
                state.playerNames,
                finalScores,
                overallWinner,
                state.lastRoundPoints,
                state.roundsPlayed,
                totalTurns
        );
    }

    /** Resets piles, deals 7 cards each, flips starting card (re-draws if wild). */
    private void setupRound() {
        state.buildDeck();
        Collections.shuffle(state.deck, state.random);
        state.discard.clear();
        for (ArrayList<String> hand : state.hands) hand.clear();

        for (int i = 0; i < state.playerNames.size(); i++) {
            for (int j = 0; j < 7; j++) state.hands.get(i).add(state.draw());
        }

        // Starting card must not be a wild
        state.upCard = state.draw();
        while (state.upCard.startsWith("W")) {
            state.discard.add(state.upCard);
            state.upCard = state.draw();
        }

        state.calledColor = "";
        state.direction   = 1;
        // First player chosen randomly each round
        state.currentPlayer = state.random.nextInt(state.playerNames.size());
    }

    /**
     * Documented variant: if the first upCard is a Skip, Reverse, or Draw Two,
     * apply its effect before the first player's turn.
     * Wild/W4 are re-drawn above (never reach here).
     */
    private void applyStartCardEffect(String card) {
        String rank = CardRules.rank(card);
        switch (rank) {
            case "SKIP" -> {
                log("START_CARD_SKIP firstPlayer=" + state.currentPlayerName() + " loses turn");
                state.next();
            }
            case "REVERSE" -> {
                state.direction = -1;
                log("START_CARD_REVERSE direction=-1");
                state.next();
            }
            case "DRAW_TWO" -> {
                String victim = state.currentPlayerName();
                state.currentHand().add(state.draw());
                state.currentHand().add(state.draw());
                log("START_CARD_DRAW_TWO victim=" + victim);
                state.next();
            }
            default -> { /* number card — no effect */ }
        }
    }

    private void log(String event) {
        state.log(event);
        GameLogger.info(event);
    }
}
