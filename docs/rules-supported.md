# Supported UNO Rules

This document lists which rules from `Final_Project_UNO_rules_reference.md` are implemented and documents the rule variants and accepted simplifications used in this project.

---

## Deck Composition

The game uses the standard **108-card UNO deck**:

* Four colors: Red (R), Yellow (Y), Green (G), Blue (B)
* One `0` card per color
* Two of each `1–9` card per color
* Two `Skip` cards per color
* Two `Reverse` cards per color
* Two `Draw Two` cards per color
* Four `Wild` cards
* Four `Wild Draw Four` cards

Total: **108 cards**.

The deck composition is verified by `DeckCompositionTest`.

---

## Legal Play Validation

A card is legal when at least one of the following is true:

1. It is a `Wild` or `Wild Draw Four` card.
2. Its color matches the current active color.
3. After a Wild card, its color matches the called color.
4. Its action type matches the top card (Skip, Reverse, or Draw Two).
5. Its number matches the top card number.

Illegal plays are rejected. If a human attempts to play an illegal card or selects an invalid card, a penalty card is drawn and the turn passes.

---

## Skip

* The next player loses their turn.
* Play continues with the following player.
* Works correctly in 2-, 3-, and 4-player games.

---

## Reverse

* Reverses the direction of play.
* In a two-player game, Reverse acts like Skip, so the player who played Reverse immediately takes another turn.
* This behavior is documented and tested.

---

## Draw Two

* The next player draws two cards.
* The next player loses their turn.
* Play continues with the following player.

Variant used:

* Draw Two cards cannot be stacked.

---

## Wild

* The player chooses the next active color.
* Bots automatically choose the color they hold the most cards of.
* Human players are prompted to choose a color.
* All subsequent legal-play checks use the chosen color.

---

## Wild Draw Four

* The player chooses the next active color.
* The next player draws four cards.
* The next player loses their turn.

Variant used:

* Wild Draw Four challenge rules are not implemented.

---

## Draw / Pass Behavior

This project implements the following draw/pass variant:

* A player with no legal card draws one card.
* If the drawn card is legal:

    * Bots play it immediately.
    * Human players are asked whether they want to play it.
* If the drawn card is not played, the turn passes.

This behavior is covered by `DrawPassUnoTest`.

---

## UNO Call and Penalty

When a player finishes a turn with exactly one card remaining:

* The game automatically detects the one-card state.
* Bots automatically call UNO.
* Human players are prompted to type `UNO`.

If the human player fails to call UNO:

* The player receives a two-card penalty.
* The penalty is applied at the start of the next player's turn.
* After the penalty check, the pending UNO state is cleared.

Both successful UNO calls and missed UNO penalties are verified by `DrawPassUnoTest`.

---

## Round Scoring

At the end of each round, the winner receives the total value of all cards remaining in the other players' hands.

Card values:

| Card           |           Points |
| -------------- | ---------------: |
| Number cards   | Face value (0–9) |
| Skip           |               20 |
| Reverse        |               20 |
| Draw Two       |               20 |
| Wild           |               50 |
| Wild Draw Four |               50 |

---

## Multi-Round Game to Target Score

* The game continues across multiple rounds.
* The default target score is **500**.
* The target score can be changed using `--target`.
* The first player to reach or exceed the target score wins the overall game.
* Scores are displayed after every round.
* Final scores are displayed when the game ends.

---

## Starting Card Variant

If the initial discard card is an action card:

* **Skip:** The first player's turn is skipped.
* **Reverse:** Play direction is reversed before the first turn.
* **Draw Two:** The first player draws two cards and loses their turn.
* **Wild** and **Wild Draw Four:** A new starting card is drawn instead.

This behavior is documented and implemented.

---

## Accepted Simplifications

The following official UNO features are intentionally not implemented:

* Wild Draw Four challenge rule
* Draw Two stacking
* Wild Draw Four stacking
* Additional house-rule variations
* Graphical user interface (text-based CLI only)

These simplifications are acceptable within the project rubric.
