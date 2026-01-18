package com.example.arcana.client.gui;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.ClientResourceLoader;
import com.example.arcana.util.ArcanaLog;
import com.example.arcana.util.LanguageUtil;
import com.google.gson.JsonArray;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class DiaryContent {
    private static final String MODULE = "DIARY";

    public static List<Component> getContent() {
        List<Component> result = new ArrayList<>();

        String code = ClientResourceLoader.getClientLanguage();
        String validatedCode = LanguageUtil.validate(code);

        ResourceLocation res = ResourceLocation.fromNamespaceAndPath(
                ArcanaMod.MODID,
                "lore/diary/kaliastrus_diary_" + validatedCode + ".json"
        );

        var jsonOpt = ClientResourceLoader.loadJson(res, MODULE);

        if (jsonOpt.isEmpty() || !jsonOpt.get().has("paragraphs")) {
            ArcanaLog.error(MODULE, "Diary resource not found or invalid: {}", res);
            result.add(Component.literal("Failed to load diary content."));
            return result;
        }

        JsonArray paragraphs = jsonOpt.get().getAsJsonArray("paragraphs");

        for (var paragraphElement : paragraphs) {
            JsonArray parts = paragraphElement.getAsJsonArray();
            Component assembled = Component.empty();

            for (var part : parts) {
                ComponentSerialization.CODEC
                        .parse(com.mojang.serialization.JsonOps.INSTANCE, part)
                        .resultOrPartial(error -> ArcanaLog.error(MODULE, "JSON parse error: {}", error))
                        .ifPresent(component -> assembled.getSiblings().add(component));
            }

            result.add(assembled);
        }

        return result;
    }

    public static String getTitle() {
        return "Diary of Kaliastrus";
    }
}