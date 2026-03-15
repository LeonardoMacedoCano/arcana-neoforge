package com.arcana.mod.util.server;

import com.arcana.mod.ArcanaMod;
import com.arcana.mod.util.common.ArcanaLog;
import com.arcana.mod.util.common.LanguageUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class NarrativeResourceLoader {

    private static final String MODULE = "NARRATIVE";

    /**
     * Loads a narrative JSON and returns a single array field.
     * The file is resolved as: data/arcana/narrative/{subPath}_{lang}.json
     *
     * @param player     the player (used for language detection and server access)
     * @param subPath    path relative to narrative/, e.g. "diary/diary_messages"
     * @param arrayField the JSON field name containing the array, e.g. "messages"
     */
    public static Optional<JsonArray> loadArray(ServerPlayer player, String subPath, String arrayField) {
        var jsonOpt = loadObject(player, subPath);
        if (jsonOpt.isEmpty() || !jsonOpt.get().has(arrayField)) {
            ArcanaLog.warn(MODULE, "Missing '{}' field in narrative JSON: narrative/{}_{}.json",
                    arrayField, subPath, LanguageUtil.validate(ServerResourceLoader.getPlayerLanguage(player)));
            return Optional.empty();
        }
        return Optional.of(jsonOpt.get().getAsJsonArray(arrayField));
    }

    /**
     * Loads a full narrative JSON object.
     * The file is resolved as: data/arcana/narrative/{subPath}_{lang}.json
     *
     * @param player  the player (used for language detection and server access)
     * @param subPath path relative to narrative/, e.g. "dreams/diary_guide_dream"
     */
    public static Optional<JsonObject> loadObject(ServerPlayer player, String subPath) {
        var server = player.getServer();
        if (server == null) {
            ArcanaLog.warn(MODULE, "Server is null for player: {}", player.getName().getString());
            return Optional.empty();
        }

        String validatedLang = LanguageUtil.validate(ServerResourceLoader.getPlayerLanguage(player));
        ResourceLocation path = ResourceLocation.fromNamespaceAndPath(
                ArcanaMod.MODID,
                "narrative/" + subPath + "_" + validatedLang + ".json"
        );

        return ServerResourceLoader.loadJson(server, path, MODULE);
    }
}
