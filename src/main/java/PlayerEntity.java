import jakarta.persistence.*;

@Entity
@Table(name = "players", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class PlayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    protected PlayerEntity() {}

    public PlayerEntity(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
