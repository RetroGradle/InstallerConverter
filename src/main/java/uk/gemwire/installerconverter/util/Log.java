package uk.gemwire.installerconverter.util;

import java.util.logging.Logger;

public abstract class Log {

    public static final Logger LOGGER = Logger.getLogger("InstallerConverter");

    public static void warn(String message) {
        LOGGER.warning(message);
    }

}
