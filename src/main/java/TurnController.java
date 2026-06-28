import java.util.ArrayList;

/**
 * Handles a single player's turn in UNO.
 * Separated from GameRunner so turn logic can be tested without a CLI.
 *
 * UNO missed-call penalty (documented behavior):
 *   - When a player plays down to 1 card, state.unoCandidateIndex is set to that player.
 *   - state.unoCalled is set to true immediately for bots (they always call it).
 *   - For humans, they are prompted to call UNO when they have 1 card left.
 *   - At the START of the next player's turn, before they play, we check:
 *       if unoCandidateIndex != -1 AND !unoCalled → the candidate missed the UNO call
 *       → candidate draws 2 penalty cards.
 *   - After the check, unoCandidateIndex is reset to -1.
 *   - Timing: the penalty window is the entire next player's turn start.
 *     This is the standard "catch them before they play" variant.
 */
public class TurnController {
    private final GameState state;

    public TurnController(GameState state) {
        this.state = state;
    }

    /**
     * Executes one turn for the current player.
     * Returns true if the round is over (current player emptied their hand).
     */
    public boolean playTurn() {
        String name = state.currentPlayerName();
        ArrayList<String> hand = state.currentHand();

        // --- Check for missed UNO penalty before this player acts ---
        checkMissedUnoPenalty();

        log("PLAYER_TURN player=" + name
                + " handSize=" + hand.size()
                + " upCard=" + state.upCard
                + " calledColor=" + state.calledColor);

        GameView.showUpCard(state.upCard, state.calledColor);
        GameView.showHand(name, hand);

        // --- Choose a card to play ---
        int chosen = state.isHuman(state.currentPlayer)
                ? resolveHumanChoice(hand)
                : BotStrategy.chooseCard(hand, state.upCard, state.calledColor);

        // --- If no legal card, draw one and optionally play it ---
        if (chosen == -1) {
            chosen = drawAndMaybePlay(name, hand);
        }

        // --- Play the chosen card or pass ---
        if (chosen >= 0) {
            return playChosenCard(name, hand, chosen);
        }

        log("PLAYER_PASS player=" + name);
        state.next();
        return false;
    }

    /**
     * Checks if the previous UNO candidate missed their call.
     * If so, applies the 2-card penalty.
     * Timing: runs at the start of each new player's turn.
     */
    private void checkMissedUnoPenalty() {
        if (state.unoCandidateIndex == -1) return;

        int candidate = state.unoCandidateIndex;
        state.unoCandidateIndex = -1; // clear regardless of outcome

        if (!state.unoCalled) {
            // Missed the UNO call — draw 2 penalty cards
            String candidateName = state.playerNames.get(candidate);
            ArrayList<String> candidateHand = state.hands.get(candidate);
            candidateHand.add(state.draw());
            candidateHand.add(state.draw());
            GameView.showMissedUnoPenalty(candidateName);
            log("UNO_PENALTY player=" + candidateName + " drew=2 reason=missedUnoCall");
        }

        state.unoCalled = false; // reset for next time
    }

    /**
     * Draw/pass behavior: draw one card, play it if legal (bot: auto; human: prompted).
     */
    private int drawAndMaybePlay(String name, ArrayList<String> hand) {
        String drawn = state.draw();
        hand.add(drawn);
        GameView.showDraw(name, drawn);
        log("CARD_DRAWN player=" + name + " card=" + drawn);

        if (!CardRules.isLegal(drawn, state.upCard, state.calledColor)) return -1;

        if (!state.isHuman(state.currentPlayer)) return hand.size() - 1;

        String answer = GameView.promptDrawnCard(drawn);
        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) return hand.size() - 1;
        return -1;
    }

    /**
     * Validates and executes playing the card at index chosen.
     */
    private boolean playChosenCard(String name, ArrayList<String> hand, int chosen) {
        if (chosen >= hand.size()) {
            GameView.showPenaltyInvalidIndex(name);
            hand.add(state.draw());
            logInvalid("INVALID_INPUT player=" + name + " reason=invalidIndex");
            state.next();
            return false;
        }

        String card = hand.get(chosen);

        if (!CardRules.isLegal(card, state.upCard, state.calledColor)) {
            GameView.showPenaltyIllegalCard(name, card);
            hand.add(state.draw());
            logInvalid("INVALID_INPUT player=" + name + " reason=illegalCard card=" + card);
            state.next();
            return false;
        }

        // Play the card
        hand.remove(chosen);
        state.discard.add(state.upCard);
        state.upCard      = card;
        state.calledColor = "";
        GameView.showPlay(name, card);
        log("CARD_PLAYED player=" + name + " card=" + card);

        // Wild: choose color
        if (card.equals("W") || card.equals("W4")) {
            state.calledColor = state.isHuman(state.currentPlayer)
                    ? GameView.resolveHumanColor()
                    : BotStrategy.chooseColor(hand);
            GameView.showCalledColor(name, state.calledColor);
            log("COLOR_CALLED player=" + name + " color=" + state.calledColor);
        }

        // UNO detection — player now has exactly 1 card
        if (hand.size() == 1) {
            state.unoCandidateIndex = state.currentPlayer;

            if (!state.isHuman(state.currentPlayer)) {
                // Bots always call UNO automatically
                state.unoCalled = true;
                GameView.showUno(name);
                log("UNO_CALLED player=" + name + " auto=true");
            } else {
                // Human must call UNO themselves when prompted
                String unoInput = GameView.promptUnoCall();
                if (unoInput.equalsIgnoreCase("UNO")) {
                    state.unoCalled = true;
                    GameView.showUno(name);
                    log("UNO_CALLED player=" + name + " auto=false");
                } else {
                    state.unoCalled = false;
                    log("UNO_MISSED player=" + name + " input=" + unoInput);
                    // Penalty applied at START of next player's turn via checkMissedUnoPenalty()
                }
            }
        }

        // Round end
        if (hand.isEmpty()) {
            int points = state.tallyPoints(state.currentPlayer);
            state.lastRoundPoints = points;
            state.scores[state.currentPlayer] += points;
            state.unoCandidateIndex = -1; // clear on round end
            GameView.showWin(name, points);
            log("ROUND_WIN winner=" + name + " points=" + points
                    + " totalScore=" + state.scores[state.currentPlayer]);
            return true;
        }

        applyCardEffect(card);
        return false;
    }

    /**
     * Applies the special effect of the played card and advances currentPlayer.
     *
     * Skip:           next player loses their turn.
     * Reverse:        direction flips; in 2-player acts like Skip.
     * Draw Two:       next player draws 2 cards and loses their turn.
     * Wild Draw Four: next player draws 4 cards and loses their turn.
     * Wild / Number:  normal advance.
     */
    public void applyCardEffect(String card) {
        String rank = CardRules.rank(card);
        switch (rank) {
            case "SKIP" -> {
                state.next();
                log("PLAYER_SKIPPED player=" + state.currentPlayerName());
                state.next();
            }
            case "REVERSE" -> {
                state.direction *= -1;
                log("DIRECTION_REVERSED direction=" + state.direction);
                if (state.playerNames.size() == 2) {
                    log("REVERSE_AS_SKIP_2P player=" + state.currentPlayerName());
                    state.next();
                    state.next();
                } else {
                    state.next();
                }
            }
            case "DRAW_TWO" -> {
                state.next();
                ArrayList<String> victim = state.currentHand();
                victim.add(state.draw());
                victim.add(state.draw());
                GameView.showDrawTwo(state.currentPlayerName());
                log("DRAW_TWO_APPLIED player=" + state.currentPlayerName() + " count=2");
                state.next();
            }
            case "WILD_DRAW_FOUR" -> {
                state.next();
                ArrayList<String> victim = state.currentHand();
                for (int i = 0; i < 4; i++) victim.add(state.draw());
                GameView.showDrawFour(state.currentPlayerName());
                log("WILD_DRAW_FOUR_APPLIED player=" + state.currentPlayerName() + " count=4");
                state.next();
            }
            default -> state.next();
        }
    }

    /**
     * Reads and validates human card selection from the CLI.
     */
    public int resolveHumanChoice(ArrayList<String> hand) {
        while (true) {
            String input = GameView.promptCardChoice();
            if (input.equals("DRAW")) return -1;

            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) return index;
                logInvalid("INVALID_INPUT reason=indexOutOfRange input=" + input);
                GameView.showCardNotFound();
                continue;
            } catch (NumberFormatException ignored) {}

            boolean found = false;
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equalsIgnoreCase(input)) {
                    found = true;
                    if (CardRules.isLegal(hand.get(i), state.upCard, state.calledColor)) return i;
                    GameView.showCardNotLegal();
                    logInvalid("INVALID_INPUT reason=illegalCard input=" + input);
                    break;
                }
            }
            if (!found) {
                GameView.showCardNotFound();
                logInvalid("INVALID_INPUT reason=cardNotFound input=" + input);
            }
        }
    }

    private void log(String event) {
        state.log(event);
        GameLogger.info(event);
    }

    private void logInvalid(String event) {
        state.log(event);
        GameLogger.invalidInput(event);
    }
}
