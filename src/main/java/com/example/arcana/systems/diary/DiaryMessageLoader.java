package com.example.arcana.systems.diary;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.ArcanaLog;
import com.example.arcana.util.JsonResourceLoaderUtil;
import com.example.arcana.util.LanguageUtil;
import com.google.gson.JsonArray;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class DiaryMessageLoader {

    public static List<Component> getMessages() {

        String lang = LanguageUtil.getSupportedLanguage();

        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(
                ArcanaMod.MODID,
                "narrative/diary/diary_messages_" + lang + ".json"
        );

        var jsonOpt = JsonResourceLoaderUtil.loadJson(path, "Diary");

        if (jsonOpt.isEmpty() || !jsonOpt.get().has("messages")) {
            ArcanaLog.error("Diary",
                    "Invalid or missing diary messages JSON structure {}", path);
            return fallback();
        }

        JsonArray arr = jsonOpt.get().getAsJsonArray("messages");
        List<Component> list = new ArrayList<>();

        for (int i = 0; i < arr.size(); i++) {
            String text = arr.get(i).getAsString();

            list.add(
                    Component.literal(text)
                            .withStyle((i % 2 == 0)
                                    ? ChatFormatting.LIGHT_PURPLE
                                    : ChatFormatting.DARK_PURPLE
                            )
            );
        }

        ArcanaLog.info(
                "Diary",
                "Loaded {} diary messages ({})",
                list.size(),
                lang
        );

        return list;
    }

    private static List<Component> fallback() {
        return List.of(
                Component.literal("Something went wrong… but I will still return.")
                        .withStyle(ChatFormatting.DARK_RED)
        );
    }
}
