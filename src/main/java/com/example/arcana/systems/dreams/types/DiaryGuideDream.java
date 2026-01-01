package com.example.arcana.systems.dreams.types;

import com.example.arcana.ArcanaMod;
import com.example.arcana.systems.dreams.DreamType;
import com.example.arcana.systems.diary.DiaryWorldData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

import static com.example.arcana.systems.diary.DiaryPersistenceHandler.isDiaryBondActive;

public class DiaryGuideDream implements DreamType {

    private static final String FIRST_DREAM_MSG =
            "Essa noite sonhei com números... %s.\nNão faço ideia do que significa!";

    private static final String[] DREAM_MSGS = {
            "De novo sonhei com os números %s.\nAlgo me diz que estão relacionados à planície...",
            "Os números %s voltaram no meu sonho.\nPreciso descobrir o que representam.",
            "Outra noite com os números %s em minha mente.\nSinto que é um caminho a seguir.",
            "Mais uma vez os números %s aparecem.\nEles parecem indicar algo importante.",
            "Sonhei novamente com %s.\nAlgo me aguarda na estrutura da planície."
    };

    private static final Map<UUID, List<Integer>> ORDER = new HashMap<>();
    private static final Random RNG = new Random();

    @Override
    public String getName() {
        return "Diary Guide Dream";
    }

    @Override
    public boolean shouldTrigger(ServerPlayer player, ServerLevel level) {
        ArcanaMod.LOGGER.debug("Processando condição do diário para {}", player.getName().getString());

        DiaryWorldData data = DiaryWorldData.get(level);

        if (!data.hasSpawned()) {
            ArcanaMod.LOGGER.debug("Estrutura do diário ainda não foi gerada.");
            return false;
        }

        if (isDiaryBondActive(player)) {
            ArcanaMod.LOGGER.debug("Jogador {} já possui o diário.", player.getName().getString());
            return false;
        }

        boolean ok = data.getStructurePos() != null;

        ArcanaMod.LOGGER.debug("Diário pode gerar sonho? {}", ok);
        return ok;
    }

    @Override
    public void runDream(ServerPlayer player, ServerLevel level) {
        ArcanaMod.LOGGER.debug("Processando conteúdo do diário para {}", player.getName().getString());

        BlockPos pos = DiaryWorldData.get(level).getStructurePos();
        String coords = "%d %d %d".formatted(pos.getX(), pos.getY(), pos.getZ());

        boolean first = !ORDER.containsKey(player.getUUID());
        String msg;

        if (first) {
            msg = FIRST_DREAM_MSG.formatted(coords);
            ORDER.put(player.getUUID(), shuffled());
            ArcanaMod.LOGGER.debug("Primeiro sonho enviado.");
        } else {
            List<Integer> order = ORDER.get(player.getUUID());
            if (order.isEmpty()) order.addAll(shuffled());
            msg = DREAM_MSGS[order.removeFirst()].formatted(coords);
        }

        player.sendSystemMessage(
                Component.literal(msg)
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
        );

        ArcanaMod.LOGGER.debug("Sonho do diário enviado com sucesso para {}", player.getName().getString());
    }

    private List<Integer> shuffled() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < DREAM_MSGS.length; i++) list.add(i);
        Collections.shuffle(list, RNG);
        return list;
    }
}
