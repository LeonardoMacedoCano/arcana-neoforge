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

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class DiaryEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide) return;
        DiaryRitualGenerator.tryGenerateRitual(event);
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
