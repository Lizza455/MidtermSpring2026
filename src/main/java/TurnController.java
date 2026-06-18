import java.util.ArrayList;

public class TurnController {
    private final GameState state;

    public TurnController(GameState state) {
        this.state = state;
    }

    public boolean playTurn() {
        String name = state.currentPlayerName();
        ArrayList<String> hand = state.currentHand();
        log("PLAYER_TURN player=" + name + " handSize=" + hand.size() + " upCard=" + state.upCard + " calledColor=" + state.calledColor);

        GameView.showUpCard(state.upCard, state.calledColor);
        GameView.showHand(name, hand);

        int chosen = state.isHuman(state.currentPlayer)
                ? resolveHumanChoice(hand)
                : BotStrategy.chooseCard(hand, state.upCard, state.calledColor);

        if (chosen == -1) {
            chosen = drawAndMaybePlay(name, hand);
        }

        if (chosen >= 0) {
            return playChosenCard(name, hand, chosen);
        }

        state.next();
        return false;
    }

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

        hand.remove(chosen);
        state.discard.add(state.upCard);
        state.upCard = card;
        state.calledColor = "";
        GameView.showPlay(name, card);
        log("CARD_PLAYED player=" + name + " card=" + card);

        if (card.equals("W") || card.equals("W4")) {
            state.calledColor = state.isHuman(state.currentPlayer)
                    ? GameView.resolveHumanColor()
                    : BotStrategy.chooseColor(hand);
            GameView.showCalledColor(name, state.calledColor);
            log("COLOR_CALLED player=" + name + " color=" + state.calledColor);
        }

        if (hand.size() == 1) GameView.showUno(name);

        if (hand.isEmpty()) {
            int points = state.tallyPoints(state.currentPlayer);
            state.scores[state.currentPlayer] += points;
            GameView.showWin(name, points);
            log("GAME_END winner=" + name + " points=" + points);
            return true;
        }

        applyCardEffect(card);
        return false;
    }

    public void applyCardEffect(String card) {
        String rank = CardRules.rank(card);
        if (rank.equals("SKIP")) {
            state.next();
            log("PLAYER_SKIPPED player=" + state.currentPlayerName());
            state.next();
        } else if (rank.equals("REVERSE")) {
            state.direction *= -1;
            log("DIRECTION_REVERSED direction=" + state.direction);
            state.next();
            if (state.playerNames.size() == 2) state.next();
        } else if (rank.equals("DRAW_TWO")) {
            state.next();
            state.currentHand().add(state.draw());
            state.currentHand().add(state.draw());
            GameView.showDrawTwo(state.currentPlayerName());
            log("CARD_DRAWN player=" + state.currentPlayerName() + " count=2 reason=DRAW_TWO");
            state.next();
        } else if (rank.equals("WILD_DRAW_FOUR")) {
            state.next();
            for (int i = 0; i < 4; i++) state.currentHand().add(state.draw());
            GameView.showDrawFour(state.currentPlayerName());
            log("CARD_DRAWN player=" + state.currentPlayerName() + " count=4 reason=WILD_DRAW_FOUR");
            state.next();
        } else {
            state.next();
        }
    }

    public int resolveHumanChoice(ArrayList<String> hand) {
        while (true) {
            String input = GameView.promptCardChoice();
            if (input.equals("DRAW")) return -1;
            try {
                int index = Integer.parseInt(input);
                if (index >= 0 && index < hand.size()) return index;
                logInvalid("INVALID_INPUT reason=indexOutOfRange input=" + input);
            } catch (NumberFormatException ignored) {
                // Input may be a card code, checked below.
            }

            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i).equals(input)) {
                    if (CardRules.isLegal(hand.get(i), state.upCard, state.calledColor)) return i;
                    GameView.showCardNotLegal();
                    logInvalid("INVALID_INPUT reason=illegalCard input=" + input);
                }
            }

            GameView.showCardNotFound();
            logInvalid("INVALID_INPUT reason=cardNotFound input=" + input);
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
