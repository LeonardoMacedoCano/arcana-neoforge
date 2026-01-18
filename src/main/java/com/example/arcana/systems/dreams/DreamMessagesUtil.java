package com.example.arcana.systems.dreams;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.ArcanaLog;
import com.example.arcana.util.LanguageUtil;
import com.example.arcana.util.ServerResourceLoader;
import com.google.gson.JsonArray;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class DreamMessagesUtil {

    public static DreamMessages load(String dreamFileName, ServerPlayer player) {
        String playerLang = ServerResourceLoader.getPlayerLanguage(player);
        String validatedLang = LanguageUtil.validate(playerLang);

        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(
                ArcanaMod.MODID,
                "narrative/dreams/" + dreamFileName + "_" + validatedLang + ".json"
        );

        var server = player.getServer();
        var jsonOpt = ServerResourceLoader.loadJson(server, path, "Dreams");

        if (jsonOpt.isEmpty() ||
                !jsonOpt.get().has("first") ||
                !jsonOpt.get().has("messages")) {

            ArcanaLog.warn("Dreams",
                    "Invalid dream json {} ({})",
                    dreamFileName, validatedLang
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
                msgs.size(), dreamFileName, validatedLang
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
