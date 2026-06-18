import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "uno_games")
public class UnoGameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private int roundsPlayed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private PlayerEntity winner;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnoRoundEntity> rounds = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerScoreEntity> scores = new ArrayList<>();

    protected UnoGameEntity() {}

    public UnoGameEntity(LocalDateTime startedAt, LocalDateTime completedAt, int roundsPlayed, PlayerEntity winner) {
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.roundsPlayed = roundsPlayed;
        this.winner = winner;
    }

    public Long getId() { return id; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public int getRoundsPlayed() { return roundsPlayed; }
    public PlayerEntity getWinner() { return winner; }
    public List<UnoRoundEntity> getRounds() { return rounds; }
    public List<PlayerScoreEntity> getScores() { return scores; }

    public void addRound(UnoRoundEntity round) {
        rounds.add(round);
        round.setGame(this);
    }

    public void addScore(PlayerScoreEntity score) {
        scores.add(score);
        score.setGame(this);
    }
}
