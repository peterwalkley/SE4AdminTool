package tfa.se4.logger;

public interface LoggerInterface {

    enum LogLevel {
        TRACE("TRACE"),
        DEBUG("DEBUG"),
        ERROR("ERROR"),
        INFO("INFO");

        public final String label;

        LogLevel(final String label) {
            this.label = label;
        }
    }

    enum LogType {
        CHECK("CHECK"),
        GAMEBAN("GAMEBAN"),
        GAME_DATA("GAME_DATA"),
        GAME_ENDED("GAME_ENDED"),
        IPBAN("IPBAN"),
        PLAYERBAN("PLAYERBAN"),
        MSG("MSG"),
        STEAM("STEAM"),
        LEAVE("LEAVE"),
        JOIN("JOIN"),
        SYSTEM("SYSTEM"),
        VAC("VAC"),
        WHITELIST("WHITELIST");

        public final String label;

        LogType(final String label) {
            this.label = label;
        }
    }

    void log(LogLevel level, LogType type, String message, Object... args);
    void log(LogLevel level, LogType type, Throwable t, String message, Object... args);
}
