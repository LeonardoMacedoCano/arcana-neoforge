package com.arcana.mod.systems.diary;

import com.arcana.mod.util.common.ArcanaLog;
import com.arcana.mod.util.server.NarrativeResourceLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class DiaryMessageLoader {

    private static final String MODULE = "DIARY";

    public static List<Component> getMessages(ServerPlayer player) {
        var arrOpt = NarrativeResourceLoader.loadArray(player, "diary/diary_messages", "messages");
        if (arrOpt.isEmpty()) return fallback();

        var arr = arrOpt.get();
        List<Component> list = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            list.add(Component.literal(arr.get(i).getAsString())
                    .withStyle(i % 2 == 0 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.DARK_PURPLE));
        }

        ArcanaLog.info(MODULE, "Loaded {} diary messages", list.size());
        return list;
    }

    private static List<Component> fallback() {
        return List.of(
                Component.literal("Something went wrong… but I will still return.")
                        .withStyle(ChatFormatting.DARK_RED)
        );
    }
}
