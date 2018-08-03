package util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerUtil {
    private final static Logger LOGGER = Logger.getLogger(LoggerUtil.class.getName());
    //initializing the filehandler and setting to 0 or null
    private static FileHandler handler = null;

    public static void init() {
        try {
            //u is for a uniquely generated number, and g is for the generated number
            handler = new FileHandler("Scheduler-logcat.%u.%g.txt", 1024 * 1024, 10, true);
        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }
        //clears out the logger
        Logger logger = Logger.getLogger("");
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        //double check if should be INFO, FINE, WARNING, SEVERE....
        logger.setLevel(Level.INFO);
    }
}
