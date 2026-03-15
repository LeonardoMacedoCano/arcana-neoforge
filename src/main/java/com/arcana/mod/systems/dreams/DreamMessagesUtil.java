package com.arcana.mod.systems.dreams;

import com.arcana.mod.util.common.ArcanaLog;
import com.arcana.mod.util.server.NarrativeResourceLoader;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DreamMessagesUtil {

    private static final String MODULE = "DREAMS";

    public static DreamMessages load(String dreamFileName, ServerPlayer player) {
        var jsonOpt = NarrativeResourceLoader.loadObject(player, "dreams/" + dreamFileName);
        if (jsonOpt.isEmpty() || !jsonOpt.get().has("first") || !jsonOpt.get().has("messages")) {
            ArcanaLog.warn(MODULE, "Invalid dream json {} — missing 'first' or 'messages' field", dreamFileName);
            return error();
        }

        var json = jsonOpt.get();
        String first = json.get("first").getAsString();
        var arr = json.getAsJsonArray("messages");

        List<String> msgs = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) msgs.add(arr.get(i).getAsString());

        ArcanaLog.info(MODULE, "Loaded {} dream messages for {}", msgs.size(), dreamFileName);
        return new DreamMessages(first, msgs);
    }

    private static DreamMessages error() {
        return new DreamMessages(
                "Dream messages failed to load: Invalid dream structure",
                List.of("Dream system error. Check logs.")
        );
    }

    public static List<Integer> shuffledIndices(int count) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(i);
        Collections.shuffle(list);
        return list;
    }

    public record DreamMessages(String first, List<String> rest) {}
}
