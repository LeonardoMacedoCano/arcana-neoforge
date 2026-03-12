package com.arcana.mod.client.gui;

import com.arcana.mod.ArcanaMod;
import com.arcana.mod.util.client.ClientResourceLoader;
import com.arcana.mod.util.common.ArcanaLog;
import com.arcana.mod.util.common.LanguageUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class DiaryContent {
    private static final String MODULE = "DIARY";

    public record DiaryBook(List<Component> intro, List<DiaryDay> days) {}

    public record DiaryDay(String date, List<Component> paragraphs, List<ResourceLocation> icons) {}

    public static DiaryBook load() {
        String code = ClientResourceLoader.getClientLanguage();
        String validatedCode = LanguageUtil.validate(code);

        ResourceLocation res = ResourceLocation.fromNamespaceAndPath(
                ArcanaMod.MODID,
                "lore/diary/kaliastrus_diary_" + validatedCode + ".json"
        );

        var jsonOpt = ClientResourceLoader.loadJson(res, MODULE);

        if (jsonOpt.isEmpty()) {
            ArcanaLog.error(MODULE, "Diary resource not found: {}", res);
            return new DiaryBook(List.of(Component.literal("Failed to load diary.")), List.of());
        }

        JsonObject root = jsonOpt.get();
        List<Component> intro = new ArrayList<>();
        List<DiaryDay> days = new ArrayList<>();

        if (root.has("intro")) {
            for (JsonElement el : root.getAsJsonArray("intro")) {
                intro.add(parseParagraph(el.getAsJsonArray()));
            }
        }

        if (root.has("days")) {
            for (JsonElement dayEl : root.getAsJsonArray("days")) {
                JsonObject dayObj = dayEl.getAsJsonObject();
                String date = dayObj.get("date").getAsString();

                List<Component> paragraphs = new ArrayList<>();
                if (dayObj.has("paragraphs")) {
                    for (JsonElement paraEl : dayObj.getAsJsonArray("paragraphs")) {
                        paragraphs.add(parseParagraph(paraEl.getAsJsonArray()));
                    }
                }

                List<ResourceLocation> icons = new ArrayList<>();
                if (dayObj.has("icons")) {
                    for (JsonElement iconEl : dayObj.getAsJsonArray("icons")) {
                        icons.add(ResourceLocation.parse(iconEl.getAsString()));
                    }
                }

                days.add(new DiaryDay(date, paragraphs, icons));
            }
        }

        return new DiaryBook(intro, days);
    }

    private static Component parseParagraph(JsonArray parts) {
        Component assembled = Component.empty();
        for (JsonElement part : parts) {
            ComponentSerialization.CODEC
                    .parse(com.mojang.serialization.JsonOps.INSTANCE, part)
                    .resultOrPartial(error -> ArcanaLog.error(MODULE, "JSON parse error: {}", error))
                    .ifPresent(c -> assembled.getSiblings().add(c));
        }
        return assembled;
    }

    public static String getTitle() {
        return "Diary of Kaliastrus";
    }
}
