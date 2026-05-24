# Extension Readiness

## Which extension would your design support best?

**A replay log** is the most ready extension. The structure is already in place: `GameState` has a `replayLog` field (an `ArrayList<String>`) and a `log(String event)` method. Wiring it in requires adding `state.log(...)` calls in `Main.playTurn()` at the points where things happen, and printing or saving the log at the end of each game.

**Adding a new card effect** is the second most ready. The design makes it a small, contained change rather than a search through one large method.

## Where would each change be implemented?

### Replay log

1. In `Main.playTurn()`, add `state.log(...)` at the four meaningful events:
   - after a card is drawn: `state.log(name + " drew " + drawn);`
   - after a card is played: `state.log(name + " played " + card);`
   - after a color is called: `state.log(name + " called " + state.calledColor);`
   - after a win: `state.log(name + " won with " + points + " points");`
2. In `Main.playGame()` or `GameView`, print or write `state.replayLog` at game end.
3. Optionally write it to a file by adding a `saveReplayLog(String filename)` method to `GameState`.

No other files need changing. The log is isolated to `GameState` and the call sites in `Main`.

### New card effect (example: Swap Hands)

1. `CardRules.rank()` — add a new return value, e.g. `"SWAP_HANDS"`.
2. `CardRules.points()` — add a score value for the new card.
3. `CardRules.isLegal()` — add a legality condition if needed (most action cards need none beyond color/rank match).
4. `Main.applyCardEffect()` — add one `else if` branch for `"SWAP_HANDS"` that swaps the current player's hand with the next player's hand.
5. `GameView` — add a `showSwapHands(String p1, String p2)` print method.

Before this refactor, adding a card effect meant editing the 200-line game loop and finding the right copy of the legality check. Now it is five small, isolated edits in five focused locations.

### Smarter bot strategy

Currently `BotStrategy` is a class with static methods. To support multiple strategies without changing `Main`:

1. Define a `BotStrategy` interface with `int chooseCard(...)` and `String chooseColor(...)`.
2. Rename the current class to `BasicBotStrategy` implementing that interface.
3. Store a `BotStrategy` instance per player in `GameState`.
4. In `Main.playTurn()`, replace `BotStrategy.chooseCard(...)` with `state.getBotStrategy(state.currentPlayer).chooseCard(...)`.

This is a slightly larger change but `GameState` and `Main` are the only files that need touching.

## What part of your design still makes change difficult?

**Global state access.** `GameState` is better than loose static fields, but it is still a single shared object with public fields. `Main.applyCardEffect()` and `Main.playTurn()` access `state.currentPlayer`, `state.direction`, `state.hands`, and `state.deck` directly. A replay log can be added without touching this, but anything that needs to reason about state transitions from the outside (like a test that replays a recorded game) would still need to know the internal structure of `GameState`.

**The turn loop is not data.** A replay log stores strings, but replaying a game from a log would require re-parsing those strings back into actions. If replay fidelity matters, the log should store structured events (a small `TurnEvent` class with player, card, and type fields) rather than formatted strings. The current design makes adding the log easy but makes replaying from it harder.

**`applyCardEffect` is still conditional.** The if/else chain is easier to find and edit now, but it still requires editing existing code to add a new card type. A future step would be to replace it with a map from rank string to a functional effect, or to introduce a small effect interface that each card type implements.
