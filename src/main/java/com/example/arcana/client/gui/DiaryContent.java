package com.example.arcana.client.gui;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.ArcanaLog;
import com.example.arcana.util.LanguageUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiaryContent {
    private static final String MODULE = "DIARY";

    public static List<Component> getContent() {
        List<Component> result = new ArrayList<>();
        String code = LanguageUtil.getSupportedLanguage();
        ResourceLocation res = ResourceLocation.fromNamespaceAndPath(
                ArcanaMod.MODID,
                "narrative/diary/kaliastrus_diary_" + code + ".json"
        );

        try {
            Optional<Resource> optional = Minecraft.getInstance()
                    .getResourceManager()
                    .getResource(res);

            if (optional.isEmpty()) {
                ArcanaLog.error(MODULE, "Diary resource not found: " + res);
                result.add(Component.literal("Failed to load diary content (missing resource)."));
                return result;
            }

            Resource resource = optional.get();

            try (InputStream stream = resource.open()) {
                String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();

                JsonArray paragraphs = root.getAsJsonArray("paragraphs");

                for (JsonElement paragraphElement : paragraphs) {
                    JsonArray parts = paragraphElement.getAsJsonArray();
                    Component assembled = Component.empty();

                    for (JsonElement part : parts) {
                        ComponentSerialization.CODEC
                                .parse(JsonOps.INSTANCE, part)
                                .resultOrPartial(error -> ArcanaLog.error(MODULE, "JSON parse error: " + error))
                                .ifPresent(component -> assembled.getSiblings().add(component));
                    }

                    result.add(assembled);
                }
            }

        } catch (Exception e) {
            ArcanaLog.error(MODULE, "Failed to load diary content: " + e.getMessage());
            result.add(Component.literal("Failed to load diary content."));
        }

        return result;
    }

    public static String getTitle() {
        return "Diary of Kaliastrus";
    }
}
