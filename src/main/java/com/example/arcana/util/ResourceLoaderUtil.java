package com.example.arcana.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.util.Optional;

public class ResourceLoaderUtil {

    private static final Gson GSON = new Gson();

    public static Optional<JsonObject> loadJson(
            ResourceManager resourceManager,
            ResourceLocation path,
            String logTag
    ) {
        if (resourceManager == null) {
            ArcanaLog.warn(logTag, "ResourceManager is null for path: {}", path);
            return Optional.empty();
        }

        try {
            var resourceOpt = resourceManager.getResource(path);

            if (resourceOpt.isEmpty()) {
                ArcanaLog.warn(logTag, "Resource not found: {}", path);
                return Optional.empty();
            }

            try (var stream = resourceOpt.get().open();
                 var reader = new InputStreamReader(stream)) {

                var json = GSON.fromJson(reader, JsonObject.class);

                if (json == null) {
                    ArcanaLog.warn(logTag, "JSON is null after parsing: {}", path);
                    return Optional.empty();
                }

                return Optional.of(json);
            }
        } catch (Exception e) {
            ArcanaLog.warn(logTag, "Failed to load JSON {} :: {}", path, e.getMessage());
            return Optional.empty();
        }
    }
}