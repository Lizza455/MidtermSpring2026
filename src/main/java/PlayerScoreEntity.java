import jakarta.persistence.*;

@Entity
@Table(name = "player_scores", uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "player_id"}))
public class PlayerScoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private UnoGameEntity game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @Column(nullable = false)
    private int score;

    protected PlayerScoreEntity() {}

    public PlayerScoreEntity(PlayerEntity player, int score) {
        this.player = player;
        this.score = score;
    }

    void setGame(UnoGameEntity game) { this.game = game; }

    public Long getId() { return id; }
    public UnoGameEntity getGame() { return game; }
    public PlayerEntity getPlayer() { return player; }
    public int getScore() { return score; }
}
