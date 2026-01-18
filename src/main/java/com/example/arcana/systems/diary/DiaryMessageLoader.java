package com.example.arcana.systems.diary;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.ArcanaLog;
import com.example.arcana.util.LanguageUtil;
import com.example.arcana.util.ServerResourceLoader;
import com.google.gson.JsonArray;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class DiaryMessageLoader {

    public static List<Component> getMessages(ServerPlayer player) {
        String playerLang = ServerResourceLoader.getPlayerLanguage(player);
        String validatedLang = LanguageUtil.validate(playerLang);

        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(
                ArcanaMod.MODID,
                "narrative/diary/diary_messages_" + validatedLang + ".json"
        );
        var server = player.getServer();
        var jsonOpt = ServerResourceLoader.loadJson(server, path, "Diary");

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
                validatedLang
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
