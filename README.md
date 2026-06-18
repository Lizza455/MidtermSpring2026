# UNO CLI

A command-line UNO game with Maven build support, automated tests, Java logging, and Docker support.

## Requirements

- Java 17+
- Maven 3.9+
- Docker, only for Docker commands

## Local build

```bash
mvn clean compile
```

## Local test

```bash
mvn test
```

## Local run

```bash
mvn exec:java -Dexec.args="--bots 3 --games 1 --quiet --seed 123"
```

For an interactive human game:

```bash
mvn exec:java -Dexec.args="--bots 3 --games 1 --human --seed 123"
```

## Package creation

```bash
mvn clean package
```

Run the packaged jar:

```bash
java -jar target/uno-cli-1.0.0.jar --bots 3 --games 1 --quiet --seed 123
```

## Docker build

```bash
docker build -t uno-cli .
```

## Docker run

```bash
docker run --rm uno-cli
```

Or pass your own game arguments:

```bash
docker run --rm uno-cli --bots 3 --games 1 --quiet --seed 123
```

## Logging

The application uses `java.util.logging`. It logs important diagnostic events while keeping the normal CLI output readable:

- game start
- player turn
- card played
- card drawn
- invalid input
- game end
