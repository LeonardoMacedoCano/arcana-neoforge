package com.example.arcana.systems.dreams;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.ArcanaLog;
import com.example.arcana.util.JsonResourceLoaderUtil;
import com.example.arcana.util.LanguageUtil;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class DreamMessagesUtil {

    public static DreamMessages load(String dreamFileName, ServerPlayer player) {
        String lang = LanguageUtil.getSupportedLanguage(player.getLanguage());

        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(
                ArcanaMod.MODID,
                "narrative/dreams/" + dreamFileName + "_" + lang + ".json"
        );

        var jsonOpt = JsonResourceLoaderUtil.loadJson(path, "Dreams");

        if (jsonOpt.isEmpty() ||
                !jsonOpt.get().has("first") ||
                !jsonOpt.get().has("messages")) {

            ArcanaLog.warn("Dreams",
                    "Invalid dream json {} ({})",
                    dreamFileName, lang
            );

            return error();
        }

        var json = jsonOpt.get();

        String first = json.get("first").getAsString();
        JsonArray arr = json.getAsJsonArray("messages");

        List<String> msgs = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            msgs.add(arr.get(i).getAsString());
        }

        ArcanaLog.info("Dreams",
                "Loaded {} dream messages for {} ({})",
                msgs.size(), dreamFileName, lang
        );

        return new DreamMessages(first, msgs);
    }

    private static DreamMessages error() {
        return new DreamMessages(
                "Dream messages failed to load: " + "Invalid dream structure",
                List.of("Dream system error. Check logs.")
        );
    }

    public record DreamMessages(String first, List<String> rest) {}
}
