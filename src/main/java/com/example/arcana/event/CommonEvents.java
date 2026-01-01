package com.example.arcana.event;

import com.example.arcana.ArcanaMod;
import com.example.arcana.systems.dreams.DreamManager;
import com.example.arcana.util.DelayedMessageHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
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
}