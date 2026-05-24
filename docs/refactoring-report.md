# Refactoring Report

The goal of this midterm was not to rewrite the UNO game from scratch, but to improve the structure of the existing code while preserving the original behavior.

Originally, most of the logic was inside one large `Main` class. Game rules, bot behavior, console output, mutable state, and turn handling were all tightly connected, which made the project difficult to test and extend.

After refactoring, the project structure became:

```text
src/
├── Main.java
├── CardRules.java
├── BotStrategy.java
├── GameView.java
├── GameState.java
└── GameTest.java
```

Responsibilities were separated like this:

- `Main` controls the game flow and turn loop
- `CardRules` contains rule and legality logic
- `BotStrategy` handles bot decisions
- `GameView` handles console I/O
- `GameState` stores mutable game state
- `GameTest` contains characterization tests

The main goals were:
- separating rule logic from the game loop
- reducing duplicated logic
- separating console I/O from gameplay logic
- making behavior testable without running the full CLI
- organizing mutable state into one place

---

## What behavior was characterized before refactoring?

Before refactoring, I played several games manually to understand how this implementation actually behaves. Then I wrote tests based on the current behavior of the code, not based on ideal UNO rules.

The tests cover:
- color, number, and action-rank matching
- Wild and Wild Draw Four behavior
- called-color behavior after wilds
- Skip, Reverse, Draw Two, and Wild Draw Four effects
- Reverse acting as Skip in two-player games
- illegal play handling
- scoring for all card types
- deck drawing and reshuffling behavior
- bot priority logic
- bot color selection
- state transitions for all card effects

I also tested some less obvious behaviors. For example, if a wild card is on top but no color has been called yet, colored cards become illegal because `color("W")` returns an empty string and there is no `calledColor` value available.

State-transition tests directly call `applyCardEffect()` using a real `GameState` to verify that player turns, directions, and hand sizes change correctly after card effects.

---

## What were the biggest design problems?

The biggest issue was duplicated legality logic. The same legality check existed inside `isLegal()` and was repeated several more times inside `chooseBotCard()`.

Another major issue was the amount of global mutable state. Variables like `deck`, `discard`, `hands`, `upCard`, `calledColor`, `currentPlayer`, `direction`, and `scores` all lived directly inside `Main`, making the game flow difficult to follow.

Console output was also mixed heavily into gameplay logic through scattered `if (!quiet)` checks and print statements.

Finally, input reading and legality validation were mixed together inside `askHuman()` even though they are separate responsibilities.

---

## What refactorings were performed?

The project was refactored in small steps while repeatedly rerunning the game and tests after each change.

### Step 1 — CardRules

`color()`, `rank()`, `number()`, `points()`, and `isLegal()` were moved into a stateless `CardRules` class. Duplicate legality checks inside bot logic were replaced with calls to `CardRules.isLegal()`.

### Step 2 — BotStrategy

Bot logic was moved into its own class. Instead of reading global fields directly, bot methods now receive `upCard` and `calledColor` as parameters, making the bot testable independently.

### Step 3 — GameView

All console output was moved into `GameView`. Input helper methods like `promptCardChoice()` and `promptColorChoice()` were also added to separate raw input reading from gameplay validation.

### Step 4 — GameState

All mutable game data was grouped into a `GameState` object. Methods like `draw()`, `next()`, `buildDeck()`, and `tallyPoints()` were moved there as well.

### Step 5 — Turn-loop extraction

`applyCardEffect()` was extracted from the middle of the game loop so Skip, Reverse, Draw Two, and Wild Draw Four logic no longer lived directly inside `playTurn()`.

### Step 6 — Separating parsing from validation

`GameView.promptCardChoice()` now only reads console input, while `Main.resolveHumanChoice()` handles legality checks and validation separately.

---

## What behavior was intentionally preserved?

The gameplay behavior stayed the same after the refactor:

- Human players can still type `draw` even with legal moves
- Invalid indexes still cause a penalty draw and lost turn
- Illegal card plays still cause penalties
- Bots still automatically play legal drawn cards
- Reverse still acts like Skip in two-player games
- The discard pile is reshuffled back into the deck
- `draw()` still returns `"W"` if both piles become empty
- The 3000-turn safety limit still exists
- `--self-test` still works exactly as before

---

## What risks still remain?

Two main risks still remain intentionally.

`GameState` fields are still package-visible instead of private. Fully encapsulating them would require a large number of getters and setters, which would mostly add boilerplate at this project size.

`applyCardEffect()` is also still implemented as an `if/else` chain over rank strings. A more advanced polymorphic design would scale better in a larger project, but for this assignment I wanted to avoid adding unnecessary patterns.