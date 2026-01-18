package com.example.arcana.util.common;

import com.example.arcana.ArcanaMod;
import net.minecraft.server.level.ServerPlayer;

public class ArcanaLog {

    public static void info(String module, String message) {
        ArcanaMod.LOGGER.info(format(module, message));
    }

    public static void info(String module, String message, Object... args) {
        ArcanaMod.LOGGER.info(format(module, formatArgs(message, args)));
    }

    public static void debug(String module, String message) {
        ArcanaMod.LOGGER.debug(format(module, message));
    }

    public static void debug(String module, String message, Object... args) {
        ArcanaMod.LOGGER.debug(format(module, formatArgs(message, args)));
    }

    public static void warn(String module, String message) {
        ArcanaMod.LOGGER.warn(format(module, message));
    }

    public static void warn(String module, String message, Object... args) {
        ArcanaMod.LOGGER.warn(format(module, formatArgs(message, args)));
    }

    public static void error(String module, String message) {
        ArcanaMod.LOGGER.error(format(module, message));
    }

    public static void error(String module, String message, Object... args) {
        ArcanaMod.LOGGER.error(format(module, formatArgs(message, args)));
    }

    public static void playerInfo(String module, ServerPlayer player, String message) {
        ArcanaMod.LOGGER.info(formatPlayer(module, player, message));
    }

    public static void playerDebug(String module, ServerPlayer player, String message) {
        ArcanaMod.LOGGER.debug(formatPlayer(module, player, message));
    }

    public static void playerWarn(String module, ServerPlayer player, String message) {
        ArcanaMod.LOGGER.warn(formatPlayer(module, player, message));
    }

    public static void playerError(String module, ServerPlayer player, String message) {
        ArcanaMod.LOGGER.error(formatPlayer(module, player, message));
    }

    private static String format(String module, String text) {
        return "[ARCANA][" + module + "] " + text;
    }

    private static String formatPlayer(String module, ServerPlayer player, String text) {
        return "[ARCANA][" + module + "] Player=" +
                player.getName().getString() +
                " :: " + text;
    }

    private static String formatArgs(String message, Object... args) {
        String formatted = message.replace("{}", "%s");
        return args == null || args.length == 0 ? message : String.format(formatted, args);
    }
}
