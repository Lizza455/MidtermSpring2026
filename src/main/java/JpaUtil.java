import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public final class JpaUtil {
    private JpaUtil() {}

    public static EntityManagerFactory createEntityManagerFactory(String dbUrl) {
        Map<String, String> properties = new HashMap<>();
        if (dbUrl != null && !dbUrl.isBlank()) {
            properties.put("jakarta.persistence.jdbc.url", dbUrl);
        }
        return Persistence.createEntityManagerFactory("unoPU", properties);
    }
}
