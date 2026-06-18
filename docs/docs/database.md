# Assignment 5 Database Documentation

## Selected database and persistence framework

This project uses:

- **Database:** H2
- **Persistence framework:** Hibernate/JPA
- **Persistence unit:** `unoPU`, configured in `src/main/resources/META-INF/persistence.xml`

The default database URL is:

```text
jdbc:h2:./data/uno;AUTO_SERVER=TRUE
```

This creates a local H2 database under the `data/` directory when the game is run.

## Schema

The schema supports the required Assignment 5 data:

- `players` stores player names.
- `uno_games` stores game start timestamp, completion timestamp, rounds played, and winner.
- `uno_rounds` stores round number, turns played, round winner, and points scored.
- `player_scores` stores each player's score for each game.

A schema reference script is included at:

```text
src/main/resources/db/schema.sql
```

Hibernate is also configured with `hibernate.hbm2ddl.auto=update`, so the local H2 schema is created automatically during normal runs.

## How game results are persisted

`GameRunner.playGame()` returns a `GameResult` object after each UNO game finishes. `Main` passes that result to `GameRepository.saveGameResult(...)`.

`GameRepository` handles all database work. The main game loop does not contain SQL.

Persisted data includes:

- player names
- game start and completion timestamps
- rounds played
- winner
- turns played in the round
- points scored by the winner
- per-player final scores

## Run the game and save history

```bash
mvn exec:java -Dexec.args="--bots 3 --games 5 --quiet --seed 123"
```

Persistence is enabled by default. To run without saving to the database:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 1 --quiet --no-db"
```

To use a custom H2 database URL:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 1 --db-url jdbc:h2:./data/custom-uno"
```

## Query/report commands

List recent games:

```bash
mvn exec:java -Dexec.args="--history --limit 10"
```

Show player win counts:

```bash
mvn exec:java -Dexec.args="--wins"
```

Show highest scores:

```bash
mvn exec:java -Dexec.args="--high-scores --limit 10"
```

## Run tests

Run all regression and persistence tests:

```bash
mvn test
```

The persistence tests use isolated in-memory H2 databases, for example:

```text
jdbc:h2:mem:uno_save_test;DB_CLOSE_DELAY=-1
```

So the tests do not depend on a private database on the developer's machine.
