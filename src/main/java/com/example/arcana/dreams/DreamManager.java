package com.example.arcana.dreams;

import com.example.arcana.ArcanaMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class DreamManager {

    private static final List<DreamType> DREAMS = new ArrayList<>();

    public static void register(DreamType dream) {
        ArcanaMod.LOGGER.debug("Registrando tipo de sonho: {}", dream.getName());
        DREAMS.add(dream);
    }

    public static void handleDreams(ServerPlayer player) {
        ServerLevel level = player.serverLevel();

        ArcanaMod.LOGGER.debug("Processando sonhos para jogador {}", player.getName().getString());

        for (DreamType dream : DREAMS) {
            ArcanaMod.LOGGER.debug("Verificando condição do sonho: {}", dream.getName());

            try {
                if (dream.shouldTrigger(player, level)) {
                    ArcanaMod.LOGGER.debug("Condição atendida para: {}", dream.getName());
                    dream.runDream(player, level);
                    return;
                }
            } catch (Exception e) {
                ArcanaMod.LOGGER.error("Erro ao processar sonho {}: {}", dream.getName(), e.getMessage());
            }
        }

        ArcanaMod.LOGGER.debug("Nenhum sonho acionado para {}", player.getName().getString());
    }
}
