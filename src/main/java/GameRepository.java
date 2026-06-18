import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;

public class GameRepository {
    private final EntityManagerFactory emf;

    public GameRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public long saveGameResult(GameResult result) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            PlayerEntity winner = null;
            if (!"NO_WINNER".equals(result.getWinnerName())) {
                winner = findOrCreatePlayer(em, result.getWinnerName());
            }

            UnoGameEntity game = new UnoGameEntity(
                    result.getStartedAt(),
                    result.getCompletedAt(),
                    result.getRoundsPlayed(),
                    winner
            );

            UnoRoundEntity round = new UnoRoundEntity(
                    1,
                    result.getTurnsPlayed(),
                    winner,
                    result.getPointsScored()
            );
            game.addRound(round);

            List<String> names = result.getPlayerNames();
            int[] scores = result.getFinalScores();
            for (int i = 0; i < names.size(); i++) {
                PlayerEntity player = findOrCreatePlayer(em, names.get(i));
                game.addScore(new PlayerScoreEntity(player, scores[i]));
            }

            em.persist(game);
            em.getTransaction().commit();
            return game.getId();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    public List<UnoGameEntity> recentGames(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                            "select distinct g from UnoGameEntity g " +
                                    "left join fetch g.winner " +
                                    "left join fetch g.scores s " +
                                    "left join fetch s.player " +
                                    "order by g.completedAt desc", UnoGameEntity.class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<WinCountReport> playerWinCounts() {
        EntityManager em = emf.createEntityManager();
        try {
            List<Object[]> rows = em.createQuery(
                            "select p.name, count(g.id) " +
                                    "from PlayerEntity p left join UnoGameEntity g on g.winner = p " +
                                    "group by p.name order by count(g.id) desc, p.name", Object[].class)
                    .getResultList();
            List<WinCountReport> reports = new java.util.ArrayList<>();
            for (Object[] row : rows) {
                reports.add(new WinCountReport((String) row[0], (Long) row[1]));
            }
            return reports;
        } finally {
            em.close();
        }
    }

    public List<HighScoreReport> highestScores(int limit) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Object[]> rows = em.createQuery(
                            "select p.name, s.score, g.completedAt " +
                                    "from PlayerScoreEntity s join s.player p join s.game g " +
                                    "where s.score > 0 " +
                                    "order by s.score desc, g.completedAt desc",
                            Object[].class)
                    .setMaxResults(limit)
                    .getResultList();

            List<HighScoreReport> reports = new java.util.ArrayList<>();
            for (Object[] row : rows) {
                reports.add(new HighScoreReport(
                        (String) row[0],
                        (Integer) row[1],
                        (java.time.LocalDateTime) row[2]
                ));
            }
            return reports;
        } finally {
            em.close();
        }
    }

    private PlayerEntity findOrCreatePlayer(EntityManager em, String name) {
        List<PlayerEntity> existing = em.createQuery(
                        "select p from PlayerEntity p where p.name = :name", PlayerEntity.class)
                .setParameter("name", name)
                .getResultList();
        if (!existing.isEmpty()) return existing.get(0);

        PlayerEntity player = new PlayerEntity(name);
        em.persist(player);
        return player;
    }
}
