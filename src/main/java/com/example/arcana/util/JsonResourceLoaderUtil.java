package com.example.arcana.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.InputStreamReader;
import java.util.Optional;

public class JsonResourceLoaderUtil {

    private static final Gson GSON = new Gson();

    public static Optional<JsonObject> loadJson(ResourceLocation path, String logTag) {
        try {
            var clientJson = loadFromClient(path);
            if (clientJson.isPresent()) return clientJson;

            var serverJson = loadFromServer(path);
            if (serverJson.isPresent()) return serverJson;

            ArcanaLog.warn(logTag, "JSON not found in assets or data: {}", path);
            return Optional.empty();
        } catch (Exception e) {
            ArcanaLog.warn(logTag, "Failed to load JSON {} :: {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    private static Optional<JsonObject> loadFromClient(ResourceLocation path) {
        try {
            var resourceManager = Minecraft.getInstance().getResourceManager();
            var resourceOpt = resourceManager.getResource(path);
            return resourceOpt.flatMap(JsonResourceLoaderUtil::parseResource);
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    private static Optional<JsonObject> loadFromServer(ResourceLocation path) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return Optional.empty();

        var resourceManager = server.getServerResources().resourceManager();
        var resourceOpt = resourceManager.getResource(path);
        return resourceOpt.flatMap(JsonResourceLoaderUtil::parseResource);
    }

    private static Optional<JsonObject> parseResource(Resource resource) {
        try (var stream = resource.open();
             var reader = new InputStreamReader(stream)) {

            var json = GSON.fromJson(reader, JsonObject.class);
            if (json == null) return Optional.empty();
            return Optional.of(json);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
