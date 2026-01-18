package com.example.arcana.util;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ServerResourceLoader {

    public static Optional<JsonObject> loadJson(
            MinecraftServer server,
            ResourceLocation path,
            String logTag
    ) {
        if (server == null) {
            ArcanaLog.warn(logTag, "Server is null, cannot load resource: {}", path);
            return Optional.empty();
        }

        var resourceManager = server.getServerResources().resourceManager();
        return ResourceLoaderUtil.loadJson(resourceManager, path, logTag);
    }

    public static String getPlayerLanguage(ServerPlayer player) {
        return player.getLanguage().toLowerCase();
    }
}