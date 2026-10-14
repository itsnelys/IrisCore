package net.opal.iris.commun.utils;

import net.opal.iris.Iris;

public class FunctionUtilsLogs {

    public static void debugLog(String context, String message) {
        Iris.LOGGER.debug("[Iris/{}] {}", context, message);
    }
    public static void infoLog(String context, String message) {
        Iris.LOGGER.info("[Iris/{}] {}", context, message);
    }
    public static void actionLog(String context, String message) {
        Iris.LOGGER.info("[Iris/{}] {}", context, message);
    }
    public static void errorLog(String context, String message) {
        Iris.LOGGER.error("[Iris/{}] {}", context, message);
    }
    public static void errorLog(String context, String message, Throwable cause) {
        Iris.LOGGER.error("[Iris/" + context + "] " + message, cause);
    }
}
