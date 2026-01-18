package com.example.arcana.systems.diary;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.server.ServerResourceLoader;
import com.example.arcana.util.common.ArcanaLog;
import com.example.arcana.util.common.LanguageUtil;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class DiaryRitualMessageLoader {

    private static final String MODULE = "DiaryRitual";

    public static List<String> getMessages(ServerPlayer player) {
        String playerLang = ServerResourceLoader.getPlayerLanguage(player);
        String validatedLang = LanguageUtil.validate(playerLang);

        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(
                ArcanaMod.MODID,
                "narrative/ritual/diary_ritual_message_" + validatedLang + ".json"
        );

        var server = player.getServer();
        if (server == null) {
            ArcanaLog.warn(MODULE, "Server is null for player: {}", player.getName().getString());
            return error("Server unavailable");
        }

        var jsonOpt = ServerResourceLoader.loadJson(server, path, MODULE);

        if (jsonOpt.isEmpty() || !jsonOpt.get().has("lines")) {
            ArcanaLog.warn(MODULE, "Invalid ritual JSON: {}", path);
            return error("Invalid ritual json structure");
        }

        JsonArray arr = jsonOpt.get().getAsJsonArray("lines");

        List<String> msgs = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            msgs.add(arr.get(i).getAsString());
        }

        ArcanaLog.info(MODULE,
                "Loaded {} ritual narrative lines for player {} (lang: {})",
                msgs.size(), player.getName().getString(), validatedLang
        );

        return msgs;
    }

    private static List<String> error(String msg) {
        return List.of("Ritual narrative failed to load: " + msg);
    }
}