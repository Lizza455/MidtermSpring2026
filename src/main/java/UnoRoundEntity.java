import jakarta.persistence.*;

@Entity
@Table(name = "uno_rounds")
public class UnoRoundEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private UnoGameEntity game;

    @Column(nullable = false)
    private int roundNumber;

    @Column(nullable = false)
    private int turnsPlayed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private PlayerEntity winner;

    @Column(nullable = false)
    private int pointsScored;

    protected UnoRoundEntity() {}

    public UnoRoundEntity(int roundNumber, int turnsPlayed, PlayerEntity winner, int pointsScored) {
        this.roundNumber = roundNumber;
        this.turnsPlayed = turnsPlayed;
        this.winner = winner;
        this.pointsScored = pointsScored;
    }

    void setGame(UnoGameEntity game) { this.game = game; }

    public Long getId() { return id; }
    public int getRoundNumber() { return roundNumber; }
    public int getTurnsPlayed() { return turnsPlayed; }
    public PlayerEntity getWinner() { return winner; }
    public int getPointsScored() { return pointsScored; }
}
