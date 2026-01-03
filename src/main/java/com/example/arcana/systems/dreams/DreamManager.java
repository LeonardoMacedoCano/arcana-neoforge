package com.example.arcana.systems.dreams;

import com.example.arcana.util.ArcanaLog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class DreamManager {
    private static final String MODULE = "DREAM_MANAGER";
    private static final List<DreamType> DREAMS = new ArrayList<>();

    public static void register(DreamType dream) {
        ArcanaLog.debug(MODULE, "Registering dream type: {}", dream.getName());
        DREAMS.add(dream);
    }

    public static void handleDreams(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        ArcanaLog.debug(MODULE, "Processing dreams for player {}", player.getName().getString());

        for (DreamType dream : DREAMS) {
            ArcanaLog.debug(MODULE, "Checking dream condition: {}", dream.getName());

            try {
                if (dream.shouldTrigger(player, level)) {
                    ArcanaLog.debug(MODULE, "Condition met for {}", dream.getName());
                    dream.runDream(player, level);
                    return;
                }
            } catch (Exception e) {
                ArcanaLog.error(MODULE, "Error while processing dream {}: {}", dream.getName(), e.getMessage());
            }
        }

        ArcanaLog.debug(MODULE, "No dreams triggered for {}", player.getName().getString());
    }
}
