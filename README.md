# UNO CLI

A command-line implementation of the UNO card game written in Java 17 using Maven.

## Features

* Human and bot players
* Multiple games per run
* Deterministic execution using random seeds
* Game event logging
* JUnit regression tests
* H2 database persistence using Hibernate/JPA
* Game history and statistics reports

---

## Requirements

* Java 17
* Maven 3.9+

Verify installation:

```bash
java -version
mvn -version
```

---

## Build

Compile the project:

```powershell
mvn clean compile
```

Package the application:

```powershell
mvn clean package
```

---

## Run The Game

Example:

```powershell
mvn exec:java "-Dexec.args=--bots 3 --games 1 --quiet --seed 123"
```

Available options:

| Option        | Description                         |
| ------------- | ----------------------------------- |
| `--bots N`    | Number of bot players               |
| `--games N`   | Number of games to play             |
| `--human`     | Add a human player                  |
| `--quiet`     | Suppress turn-by-turn output        |
| `--seed N`    | Use a deterministic random seed     |
| `--self-test` | Run built-in characterization tests |
| `--help`      | Show usage information              |

Example with a human player:

```powershell
mvn exec:java "-Dexec.args=--human --bots 2"
```

---

## Run Tests

Execute all tests:

```powershell
mvn test
```

Tests include:

* UNO game rule validation
* Bot strategy validation
* Card effect validation
* Deterministic game execution
* Persistence repository tests

---

# Assignment 5 Persistence

Assignment 5 adds database persistence using:

* H2 Database
* Hibernate ORM / JPA

Game results are automatically stored after every completed game.

Persisted data includes:

* Players
* Games
* Rounds played
* Scores
* Winner
* Start timestamp
* Completion timestamp

---

## Database Location

The application stores data locally using H2.

Database files are created automatically:

```text
data/
├── uno.mv.db
└── uno.trace.db
```

---

## Reports

### Recent Games

Display recent game history:

```powershell
mvn exec:java "-Dexec.args=--history --limit 10"
```

Example output:

```text
Game 5
Winner: Bot2
Rounds: 34
Completed: 2026-06-19T00:18:59
```

---

### Player Win Counts

Display total wins per player:

```powershell
mvn exec:java "-Dexec.args=--wins"
```

Example output:

```text
Bot2 | wins=3
Bot3 | wins=1
Bot1 | wins=1
```

---

### Highest Scores

Display the highest recorded scores:

```powershell
mvn exec:java "-Dexec.args=--high-scores --limit 10"
```

Example output:

```text
Bot3 | score=155 | completed=2026-06-19 00:22:03
Bot3 | score=130 | completed=2026-06-19 00:22:03
Bot2 | score=65 | completed=2026-06-19 00:22:02
```

---

## Persistence Testing

Persistence tests use isolated in-memory H2 databases.

No external database setup is required.

Run:

```powershell
mvn test
```

Successful execution should end with:

```text
BUILD SUCCESS
```

---

## Project Structure

```text
src/
├── main/
│   └── java/
│       ├── GameRunner.java
│       ├── TurnController.java
│       ├── GameRepository.java
│       ├── JpaUtil.java
│       └── ...
│
└── test/
    └── java/
        ├── UnoRegressionTest.java
        └── PersistenceRepositoryTest.java
```

---

## Assignment 5 Verification

Before submission, verify:

```powershell
mvn test
mvn exec:java "-Dexec.args=--history"
mvn exec:java "-Dexec.args=--wins"
mvn exec:java "-Dexec.args=--high-scores"
```

All commands should complete successfully.
