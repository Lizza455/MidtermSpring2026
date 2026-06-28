# UNO Game — Final Project

A command-line UNO card game implemented in Java, supporting bot-only and human-vs-bot play,
multi-round scoring to a target score, and optional H2 database persistence.

---

## Requirements

- Java 17+
- Maven 3.8+

---

## Build

```bash
mvn compile
```

---

## Run

### Bot-only game (default: 3 bots, target 500)

```bash
mvn exec:java -Dexec.args="--no-db --quiet"
```

### Play as a human against 2 bots

```bash
mvn exec:java -Dexec.args="--human --bots 2 --no-db"
```

### Custom target score and seed (for reproducibility)

```bash
mvn exec:java -Dexec.args="--bots 3 --target 200 --seed 12345 --no-db"
```

### Multiple games in one session

```bash
mvn exec:java -Dexec.args="--games 5 --quiet --no-db"
```

### With database persistence (H2, auto-created)

```bash
mvn exec:java -Dexec.args="--bots 3"
```

---

## Human Turn Input

When it is your turn, you will see your hand formatted as `index:card`:

```
You's hand: 0:R5  1:GS  2:W  3:B+2
```

Enter one of:
- A **card index** (`0`, `1`, `2` …) to play that card
- A **card code** (`R5`, `GS`, `W`, `B+2`) to play by name
- `DRAW` to draw a card (you will be asked whether to play it)

When playing a Wild or Wild Draw Four, you will be prompted:

```
Call color R/Y/G/B:
```

Enter `R`, `Y`, `G`, or `B`.

---

## Tests

```bash
mvn test
```

Test classes cover:
- `CardRulesTest`     — color, rank, number, points, isLegal
- `DeckCompositionTest` — all 108 cards, correct counts per color/type
- `ActionCardTest`    — Skip, Reverse, Draw Two, Wild, Wild Draw Four effects
- `DrawPassUnoTest`   — draw/pass flow, UNO detection, scoring tally
- `ScoringGameOverTest` — point values, multi-round accumulation, target-score win
- `BotStrategyTest`   — card selection priority, color choice

---

## Reports (requires database)

```bash
mvn exec:java -Dexec.args="--history --limit 5"
mvn exec:java -Dexec.args="--wins"
mvn exec:java -Dexec.args="--high-scores --limit 10"
```

---

## Self-Test

```bash
mvn exec:java -Dexec.args="--self-test --no-db"
```

---

## Command-Line Reference

| Flag              | Default         | Description                              |
|-------------------|-----------------|------------------------------------------|
| `--bots N`        | 3               | Number of bot players                    |
| `--human`         | off             | Add a human player                       |
| `--games N`       | 1               | Number of complete games to play         |
| `--target N`      | 500             | Score needed to win the overall game     |
| `--seed N`        | current time    | RNG seed for reproducibility             |
| `--quiet`         | off             | Suppress turn-by-turn output             |
| `--no-db`         | off             | Skip database persistence                |
| `--db-url URL`    | H2 file default | Custom JDBC URL                          |
| `--history`       | off             | Show recent games report                 |
| `--wins`          | off             | Show player win counts                   |
| `--high-scores`   | off             | Show high scores                         |
| `--limit N`       | 10              | Limit report rows                        |
| `--self-test`     | —               | Run built-in characterization checks     |
| `--help`          | —               | Show usage                               |
