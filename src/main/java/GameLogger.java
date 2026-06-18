import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class GameLogger {
    private static final Logger LOGGER = Logger.getLogger("uno");

    static {
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.INFO);
        handler.setFormatter(new SimpleFormatter());
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.INFO);
    }

    private GameLogger() {}

    public static void info(String event) {
        LOGGER.info(event);
    }

    public static void invalidInput(String event) {
        LOGGER.warning(event);
    }
}
