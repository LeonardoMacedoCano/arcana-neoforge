package com.arcana.mod.systems.diary;

import com.arcana.mod.util.common.ArcanaLog;
import com.arcana.mod.util.server.NarrativeResourceLoader;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class DiaryRitualMessageLoader {

    private static final String MODULE = "DIARY_RITUAL";

    public static List<String> getMessages(ServerPlayer player) {
        var arrOpt = NarrativeResourceLoader.loadArray(player, "ritual/diary_ritual_message", "lines");
        if (arrOpt.isEmpty()) return error("Failed to load ritual messages");

        var arr = arrOpt.get();
        List<String> msgs = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) msgs.add(arr.get(i).getAsString());

        ArcanaLog.info(MODULE, "Loaded {} ritual narrative lines for player {}",
                msgs.size(), player.getName().getString());
        return msgs;
    }

    private static List<String> error(String msg) {
        return List.of("Ritual narrative failed to load: " + msg);
    }
}
