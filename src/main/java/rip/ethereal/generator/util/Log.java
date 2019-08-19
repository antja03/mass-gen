package rip.ethereal.generator.util;

import org.apache.log4j.Logger;
import rip.ethereal.generator.AccountGenerator;
import rip.ethereal.generator.config.enums.LogDensity;

/**
 * @author antja03
 * @since 8/11/2019
 */
public class Log {

    /**
     * The logger used by the entire generator... its nice and pretty ok
     */
    private static final Logger LOGGER = Logger.getLogger("Account Generator");

    /**
     * Prints an error message to the console
     */
    public static void error(String message) {
        LOGGER.error(message);
    }

    /**
     * Prints a warning to the console (if the generator is configured to allow warnings)
     */
    public static void warn(String message) {
        if (!AccountGenerator.config.getLogDensity().equals(LogDensity.ERRORS))
          LOGGER.warn(message);
    }

    /**
     * Prints a message to the console (if the generator is configured to allow all messages)
     */
    public static void info(String message) {
        if (AccountGenerator.config.getLogDensity().equals(LogDensity.ALL))
            LOGGER.info(message);
    }

    /**
     * @return The logger used by the entire generator
     */
    public static Logger getLogger() {
        return LOGGER;
    }

}
