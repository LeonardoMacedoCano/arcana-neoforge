package com.arcana.mod.event;

import com.arcana.mod.ArcanaMod;
import com.arcana.mod.event.DiaryEvents;
import com.arcana.mod.systems.diary.DiaryPersistenceHandler;
import com.arcana.mod.systems.dreams.DreamManager;
import com.arcana.mod.systems.dreams.types.DiaryGuideDream;
import com.arcana.mod.util.server.DelayedMessageHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        DreamManager.handleDreams(player);
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        DelayedMessageHandler.tick();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        DiaryPersistenceHandler.clearState();
        DiaryGuideDream.clearState();
        DiaryEvents.clearState();
        DelayedMessageHandler.clear();
    }
}