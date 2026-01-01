package com.example.arcana.event;

import com.example.arcana.ArcanaMod;
import com.example.arcana.systems.diary.DiaryManager;
import com.example.arcana.systems.diary.DiaryRitualGenerator;
import com.example.arcana.systems.dreams.DreamManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class DiaryEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().isClientSide) {
            return;
        }

        DiaryRitualGenerator.tryGenerateRitual(event);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ArcanaMod.LOGGER.debug("Jogador {} entrou no servidor",
                player.getName().getString());

        // Aqui você pode adicionar lógica para verificar estado do diário
        // Por exemplo: verificar se deve ter um diário e não tem
        validatePlayerDiaryState(player);
    }

    /**
     * Valida o estado do diário do jogador ao entrar no servidor.
     * Útil para detectar inconsistências ou problemas.
     */
    private static void validatePlayerDiaryState(ServerPlayer player) {
        boolean hasBond = DiaryManager.hasActiveDiaryBond(player);
        boolean hasInInventory = DiaryManager.hasPlayerDiaryInInventory(player);

        if (hasBond && !hasInInventory) {
            ArcanaMod.LOGGER.warn("Jogador {} possui vínculo mas não tem diário no inventário",
                    player.getName().getString());

            // Você pode adicionar lógica aqui para lidar com isso
            // Exemplo: dar um novo diário, enviar mensagem, etc.
        }
    }

    /**
     * Quando o jogador sai do servidor - cleanup se necessário.
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ArcanaMod.LOGGER.debug("Jogador {} saiu do servidor",
                player.getName().getString());

        // Adicione cleanup aqui se necessário
        // Exemplo: limpar caches temporários relacionados ao jogador
    }
}