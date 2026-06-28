# Final Report

## What UNO Rules Are Implemented

All major UNO rules from the reference document are implemented:

**Deck Composition** — The standard 108-card deck is built by `GameState.buildDeck()`:
four colors, one zero each, two of cards 1–9, two Skip/Reverse/Draw Two each, four Wild and four Wild Draw Four.
Verified by `DeckCompositionTest` which checks total count and per-card counts.

**Legal Play Validation** — `CardRules.isLegal()` checks all five legal-play conditions:
wild cards (always legal), color match, called-color match (after wild), action-type match, and number match.
Covered by `CardRulesTest` with positive and negative test cases.

**Skip** — `TurnController.applyCardEffect()` skips the next player by calling `state.next()` twice.
Works in all player counts. Tested in `ActionCardTest`.

**Reverse** — Direction is flipped (`state.direction *= -1`). In a two-player game, Reverse
acts like Skip (documented variant). Tested in `ActionCardTest`.

**Draw Two** — Next player draws two cards and loses their turn. Tested in `ActionCardTest`.

**Wild** — Bot calls color via `BotStrategy.chooseColor()` (most-cards-in-color heuristic).
Human is prompted via `GameView.resolveHumanColor()`. Tested in `ActionCardTest` and `BotStrategyTest`.

**Wild Draw Four** — Next player draws four cards and loses their turn; color is chosen same as Wild.
No challenge rule (documented simplification). Tested in `ActionCardTest`.

**Draw/Pass Behavior** — Draw one card, optionally play if legal (bot plays automatically; human prompted).
Tested in `DrawPassUnoTest`.

**UNO Call and Penalty** — One-card state is detected after every play. Bots call UNO automatically.
Humans are prompted immediately: typing `UNO` registers the call; anything else is a missed call.
If missed, the player draws **2 penalty cards** at the start of the next player's turn.
Both detection and penalty are tested in `DrawPassUnoTest`.

**Round Scoring** — `GameState.tallyPoints()` sums card values of all non-winner hands.
Standard point values applied. Tested in `ScoringGameOverTest`.

**Multi-Round to Target Score** — `GameRunner.playGame()` loops rounds until a player reaches
`targetScore` (default 500, configurable with `--target`). `GameState.overallWinnerIndex()`
checks all scores. Tested in `ScoringGameOverTest`.

---

## How to Play from the CLI

Start a human game:

```bash
mvn exec:java -Dexec.args="--human --bots 2 --no-db"
```

On your turn you see:

```
Up card: R5
You's hand: 0:R3  1:GS  2:W  3:B+2
Choose card (index / card code / DRAW):
```

- Enter `0` (or `R3`) to play Red 3
- Enter `DRAW` to draw a card
- If drawn card is legal you are asked `Play it? (y/n)`
- When you play Wild/W4 you are asked `Call color R/Y/G/B:`

Scores are shown after each round. The game ends when someone hits the target score.

---

## Architecture: Game Logic vs. CLI

The project uses a clear separation of concerns:

| Class | Responsibility |
|-------|---------------|
| `CardRules` | Stateless card parsing and legality logic — no I/O |
| `GameState` | All mutable state (deck, hands, scores, direction) — no I/O |
| `TurnController` | Turn execution logic — delegates all I/O to GameView |
| `GameRunner` | Multi-round orchestration — delegates all I/O to GameView |
| `BotStrategy` | Bot card selection and color choice — no I/O |
| `GameView` | All `System.out.println` and `Scanner` calls — no game logic |
| `Main` | Argument parsing and wiring only |

`TurnController` and `GameRunner` can be instantiated and tested without console input because
all I/O is routed through `GameView`. Setting `GameView.setQuiet(true)` suppresses output in tests.

Game rules (`CardRules`, `GameState`) have no dependency on `GameView` at all —
they can be unit-tested purely in Java.

---

## Tests Added

| Test Class | What It Covers |
|------------|---------------|
| `CardRulesTest` | color(), rank(), number(), points(), isLegal() — 20+ cases |
| `DeckCompositionTest` | 108-card deck, per-color counts, per-type counts |
| `ActionCardTest` | Skip, Reverse (including 2P), Draw Two, Wild, Wild Draw Four |
| `DrawPassUnoTest` | draw/reshuffle, pass/advance, UNO detection, scoring tally |
| `ScoringGameOverTest` | point values, multi-round accumulation, target-score winner |
| `BotStrategyTest` | card priority, color choice, calledColor handling |

Run all tests:

```bash
mvn test
```

---

## Limitations

1. **Wild Draw Four challenge** — Not implemented (accepted simplification per the rubric).
2. **No Wild Draw Four challenge** — Accepted simplification per the rubric.
3. **No Draw Two stacking** — Cards cannot be stacked; each Draw Two forces a draw immediately.
4. **Bot strategy is simple** — The bot prioritizes action cards first, keeps wilds for last, and picks the most-represented color. A smarter bot could track cards seen.
5. **Human plays full multi-round** — The game correctly tracks rounds, but a human might need many rounds to reach 500. Use `--target N` to lower the target for shorter sessions.
6. **No GUI** — Text-only CLI as specified.
