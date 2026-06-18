import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PersistenceRepositoryTest {

    @Test
    void saveGameResultPersistsPlayersGameRoundWinnerTimestampAndScores() {
        EntityManagerFactory emf = JpaUtil.createEntityManagerFactory(
                "jdbc:h2:mem:uno_save_test;DB_CLOSE_DELAY=-1"
        );
        try {
            GameRepository repository = new GameRepository(emf);
            GameResult result = sampleResult("Bot1", new int[]{120, 0, 40});

            long gameId = repository.saveGameResult(result);

            List<UnoGameEntity> games = repository.recentGames(10);
            assertEquals(1, games.size());
            UnoGameEntity saved = games.get(0);
            assertEquals(gameId, saved.getId());
            assertEquals("Bot1", saved.getWinner().getName());
            assertEquals(1, saved.getRoundsPlayed());
            assertNotNull(saved.getStartedAt());
            assertNotNull(saved.getCompletedAt());
            assertEquals(3, saved.getScores().size());
            assertTrue(saved.getScores().stream()
                    .anyMatch(score -> score.getPlayer().getName().equals("Bot1")
                            && score.getScore() == 120));;
        } finally {
            emf.close();
        }
    }

    @Test
    void reportsReturnRecentGamesWinCountsAndHighestScores() {
        EntityManagerFactory emf = JpaUtil.createEntityManagerFactory(
                "jdbc:h2:mem:uno_report_test;DB_CLOSE_DELAY=-1"
        );
        try {
            GameRepository repository = new GameRepository(emf);
            repository.saveGameResult(sampleResult("Bot1", new int[]{80, 0, 20}));
            repository.saveGameResult(sampleResult("Bot2", new int[]{80, 140, 20}));
            repository.saveGameResult(sampleResult("Bot1", new int[]{240, 140, 20}));

            assertEquals(2, repository.recentGames(2).size());

            List<WinCountReport> wins = repository.playerWinCounts();
            assertEquals("Bot1", wins.get(0).getPlayerName());
            assertEquals(2, wins.get(0).getWins());

            List<HighScoreReport> highs = repository.highestScores(1);
            assertEquals(1, highs.size());
            assertEquals("Bot1", highs.get(0).getPlayerName());
            assertEquals(240, highs.get(0).getScore());
        } finally {
            emf.close();
        }
    }

    private GameResult sampleResult(String winner, int[] scores) {
        return new GameResult(
                LocalDateTime.now().minusMinutes(2),
                LocalDateTime.now(),
                new ArrayList<>(Arrays.asList("Bot1", "Bot2", "Bot3")),
                scores,
                winner,
                80,
                1,
                25
        );
    }
}
