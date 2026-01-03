package com.example.arcana.systems.diary;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.ArcanaLog;
import com.example.arcana.util.JsonResourceLoaderUtil;
import com.example.arcana.util.LanguageUtil;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class DiaryRitualMessageLoader {

    public static List<String> getMessages() {
        String lang = LanguageUtil.getSupportedLanguage();

        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(
                ArcanaMod.MODID,
                "narrative/ritual/diary_ritual_message_" + lang + ".json"
        );

        var jsonOpt = JsonResourceLoaderUtil.loadJson(path, "DiaryRitual");

        if (jsonOpt.isEmpty() || !jsonOpt.get().has("lines")) {
            ArcanaLog.warn("DiaryRitual", "Invalid ritual JSON: {}", path);
            return error("Invalid ritual json structure");
        }

        JsonArray arr = jsonOpt.get().getAsJsonArray("lines");

        List<String> msgs = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++)
            msgs.add(arr.get(i).getAsString());

        ArcanaLog.info("DiaryRitual",
                "Loaded {} ritual narrative lines ({})",
                msgs.size(), lang
        );

        return msgs;
    }

    private static List<String> error(String msg) {
        return List.of("Ritual narrative failed to load: " + msg);
    }
}
