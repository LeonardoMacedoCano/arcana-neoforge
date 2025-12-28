package com.example.arcana.events;

import com.example.arcana.ArcanaMod;
import com.example.arcana.dreams.DreamManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class PlayerWakeUpDreamTriggerEvent {

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ArcanaMod.LOGGER.debug("Jogador acordou. Verificando sonhos possíveis...");
            DreamManager.handleDreams(player);
        }
    }
}
