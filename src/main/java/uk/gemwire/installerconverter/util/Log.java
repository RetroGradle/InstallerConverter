package uk.gemwire.installerconverter.util;

import java.util.logging.Logger;

public abstract class Log {

    public static final Logger LOGGER = Logger.getLogger("InstallerConverter");

    public static void trace(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warning(message);
    }

    public static void error(String message) {
        LOGGER.severe(message);
    }
}
