package com.example.arcana.util;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class ClientResourceLoader {

    public static Optional<JsonObject> loadJson(ResourceLocation path, String logTag) {
        var resourceManager = Minecraft.getInstance().getResourceManager();
        return ResourceLoaderUtil.loadJson(resourceManager, path, logTag);
    }

    public static String getClientLanguage() {
        return Minecraft.getInstance()
                .getLanguageManager()
                .getSelected()
                .toLowerCase();
    }
}