# Extension Readiness

## Which extension would fit this design best?

The easiest extension to add right now would probably be a replay log. The structure for it already exists inside `GameState` through the `replayLog` field and the `log(String event)` method.

To make it work fully, I would mainly just need to add `state.log(...)` calls inside `Main.playTurn()` whenever something important happens, like:
- a player draws a card
- a player plays a card
- a wild color is called
- a player wins the game

At the end of the game, the log could either be printed to the console or written to a file.

Another extension that would fit the current structure fairly well is adding a new card effect. Since rule logic is now separated better than before, adding a new effect would only require a few small edits instead of modifying one huge game loop.

---

## Example extension: new card effect

For example, if I wanted to add a `"SWAP_HANDS"` card effect:

1. Add a new rank inside `CardRules.rank()`
2. Add its score value inside `CardRules.points()`
3. Update legality checks if needed
4. Add a new branch inside `Main.applyCardEffect()`
5. Add a small output method inside `GameView`

Before refactoring, adding a new effect would require searching through a very large `Main` class and duplicated legality logic. Now the changes are much smaller and more isolated.

---

## Example extension: smarter bots

The current `BotStrategy` uses static methods, but the structure could still support multiple bot types fairly easily.

One possible improvement would be:
- creating a `BotStrategy` interface
- renaming the current implementation to something like `BasicBotStrategy`
- storing one strategy per player inside `GameState`

That would make it possible to add aggressive bots, defensive bots, or random bots without changing the main game loop very much.

---

## What still makes future changes harder?

One remaining issue is that `GameState` fields are still accessed directly from `Main`. The state is much more organized than before, but parts of the game still depend on internal fields like `currentPlayer`, `direction`, `hands`, and `deck`.

Another limitation is that `applyCardEffect()` is still an `if/else` chain. It is much cleaner now because it is isolated in one place, but adding completely new card behaviors would still require editing that method directly.

The replay log also stores plain strings instead of structured events. That makes logging easy, but replaying an exact game from the log would be harder because the strings would need to be parsed again later.