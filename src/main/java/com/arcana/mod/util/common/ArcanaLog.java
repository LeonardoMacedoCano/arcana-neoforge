package com.arcana.mod.util.common;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

public class ArcanaLog {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void info(String module, String message) {
        LOGGER.info(format(module, message));
    }

    public static void info(String module, String message, Object... args) {
        LOGGER.info(format(module, message), args);
    }

    public static void debug(String module, String message) {
        LOGGER.debug(format(module, message));
    }

    public static void debug(String module, String message, Object... args) {
        LOGGER.debug(format(module, message), args);
    }

    public static void warn(String module, String message) {
        LOGGER.warn(format(module, message));
    }

    public static void warn(String module, String message, Object... args) {
        LOGGER.warn(format(module, message), args);
    }

    public static void error(String module, String message) {
        LOGGER.error(format(module, message));
    }

    public static void error(String module, String message, Object... args) {
        LOGGER.error(format(module, message), args);
    }

    public static void playerInfo(String module, ServerPlayer player, String message) {
        LOGGER.info(formatPlayer(module, player, message));
    }

    public static void playerDebug(String module, ServerPlayer player, String message) {
        LOGGER.debug(formatPlayer(module, player, message));
    }

    public static void playerWarn(String module, ServerPlayer player, String message) {
        LOGGER.warn(formatPlayer(module, player, message));
    }

    public static void playerError(String module, ServerPlayer player, String message) {
        LOGGER.error(formatPlayer(module, player, message));
    }

    private static String format(String module, String text) {
        return "[ARCANA][" + module + "] " + text;
    }

    private static String formatPlayer(String module, ServerPlayer player, String text) {
        return "[ARCANA][" + module + "] Player=" +
                player.getName().getString() +
                " :: " + text;
    }

}

