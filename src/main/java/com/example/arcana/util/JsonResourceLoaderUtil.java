package com.example.arcana.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStreamReader;
import java.util.Optional;

public class JsonResourceLoaderUtil {

    private static final Gson GSON = new Gson();

    public static Optional<JsonObject> loadJson(ResourceLocation path, String logTag) {
        try {
            Optional<Resource> res =
                    Minecraft.getInstance().getResourceManager().getResource(path);

            if (res.isEmpty()) {
                ArcanaLog.warn(logTag, "JSON file not found: {}", path);
                return Optional.empty();
            }

            try (var stream = res.get().open();
                 var reader = new InputStreamReader(stream)) {

                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json == null) {
                    ArcanaLog.warn(logTag, "JSON is null: {}", path);
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
