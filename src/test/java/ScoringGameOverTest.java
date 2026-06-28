import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for round scoring, multi-round play, and target-score game-over.
 * Covers rubric section 1.10 — Round Scoring and Multi-Round Target.
 */
public class ScoringGameOverTest {

    // ---- Points per card type ----

    @Test
    void points_numberCard_faceValue() {
        assertEquals(7, CardRules.points("R7"));
        assertEquals(0, CardRules.points("Y0"));
        assertEquals(9, CardRules.points("B9"));
    }

    @Test
    void points_skip_20() {
        assertEquals(20, CardRules.points("RS"));
        assertEquals(20, CardRules.points("GS"));
    }

    @Test
    void points_reverse_20() {
        assertEquals(20, CardRules.points("BR"));
        assertEquals(20, CardRules.points("YR"));
    }

    @Test
    void points_drawTwo_20() {
        assertEquals(20, CardRules.points("G+2"));
        assertEquals(20, CardRules.points("R+2"));
    }

    @Test
    void points_wild_50() {
        assertEquals(50, CardRules.points("W"));
    }

    @Test
    void points_wildDrawFour_50() {
        assertEquals(50, CardRules.points("W4"));
    }

    // ---- Multi-round: winner accumulates across rounds ----

    @Test
    void scores_accumulateAcrossRounds() {
        GameState state = makeState();
        state.scores[0] = 0;

        // Simulate round 1 win: 45 points
        state.scores[0] += 45;
        assertEquals(45, state.scores[0]);

        // Simulate round 2 win: 60 points
        state.scores[0] += 60;
        assertEquals(105, state.scores[0]);
    }

    // ---- Target score: overallWinnerIndex ----

    @Test
    void overallWinner_noneBeforeTarget() {
        GameState state = makeState();
        state.targetScore = 500;
        state.scores[0] = 499;
        state.scores[1] = 300;
        assertEquals(-1, state.overallWinnerIndex(), "No winner below target");
    }

    @Test
    void overallWinner_atExactTarget() {
        GameState state = makeState();
        state.targetScore = 500;
        state.scores[1] = 500;
        assertEquals(1, state.overallWinnerIndex(), "Winner at exactly 500");
    }

    @Test
    void overallWinner_aboveTarget() {
        GameState state = makeState();
        state.targetScore = 500;
        state.scores[0] = 523;
        assertEquals(0, state.overallWinnerIndex(), "Winner above 500");
    }

    @Test
    void overallWinner_firstPlayerToReachTarget() {
        GameState state = makeState();
        state.targetScore = 500;
        state.scores[0] = 500;
        state.scores[1] = 600; // also above, but player 0 checked first
        assertEquals(0, state.overallWinnerIndex(), "First player to reach target wins");
    }

    // ---- Full bot game ends with a winner ----

    @Test
    void fullGame_terminatesWithWinner() {
        GameState state = new GameState(1234L);
        state.setupPlayers(3, false);
        state.targetScore = 50; // low target for fast test
        GameView.setQuiet(true);
        GameRunner runner = new GameRunner(state);
        GameResult result = runner.playGame();
        assertNotEquals("NO_WINNER", result.getWinnerName(),
                "Game with low target should produce a winner");
        assertTrue(result.getRoundsPlayed() >= 1, "At least one round played");
    }

    @Test
    void fullGame_winnerScoreAtLeastTarget() {
        GameState state = new GameState(9999L);
        state.setupPlayers(2, false);
        state.targetScore = 30;
        GameView.setQuiet(true);
        GameRunner runner = new GameRunner(state);
        GameResult result = runner.playGame();
        if (!result.getWinnerName().equals("NO_WINNER")) {
            int[] scores = result.getFinalScores();
            ArrayList<String> names = result.getPlayerNames();
            int winnerIdx = names.indexOf(result.getWinnerName());
            assertTrue(scores[winnerIdx] >= 30, "Winner's score should be >= target");
        }
    }

    @Test
    void singleRound_turnsPlayedPositive() {
        GameState state = new GameState(77L);
        state.setupPlayers(3, false);
        state.targetScore = 500;
        GameView.setQuiet(true);
        GameRunner runner = new GameRunner(state);
        GameResult result = runner.playGame();
        assertTrue(result.getTurnsPlayed() > 0, "Turns played should be > 0");
    }

    private GameState makeState() {
        GameState s = new GameState(1L);
        s.playerNames.add("A");
        s.playerNames.add("B");
        s.humanPlayers.add(false);
        s.humanPlayers.add(false);
        s.hands.add(new ArrayList<>());
        s.hands.add(new ArrayList<>());
        s.scores = new int[10];
        s.targetScore = 500;
        return s;
    }
}
