package com.example.arcana.event;

import com.example.arcana.ArcanaMod;
import com.example.arcana.systems.diary.DiaryPersistenceHandler;
import com.example.arcana.systems.diary.DiaryRitualGenerator;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class DiaryEvents {

    private static final Map<UUID, Long> RITUAL_CHECK_COOLDOWN = new HashMap<>();
    private static final long RITUAL_CHECK_INTERVAL = 100L;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;

        long currentTick = player.serverLevel().getServer().getTickCount();
        if (currentTick - RITUAL_CHECK_COOLDOWN.getOrDefault(player.getUUID(), 0L) >= RITUAL_CHECK_INTERVAL) {
            RITUAL_CHECK_COOLDOWN.put(player.getUUID(), currentTick);
            DiaryRitualGenerator.tryGenerateRitual(event);
        }

        DiaryPersistenceHandler.preventExtraDiaryPickup(player);
    }

    public static void clearState() {
        RITUAL_CHECK_COOLDOWN.clear();
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        DiaryPersistenceHandler.handlePlayerLogin(event);
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        DiaryPersistenceHandler.handleItemToss(event);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        DiaryPersistenceHandler.handlePlayerRespawn(event);
    }

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        DiaryPersistenceHandler.handleContainerClose(event);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        DiaryPersistenceHandler.handleServerTick(event);
    }
}
