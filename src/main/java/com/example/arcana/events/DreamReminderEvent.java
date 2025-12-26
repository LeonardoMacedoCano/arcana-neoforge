package com.example.arcana.events;

import com.example.arcana.ArcanaMod;
import com.example.arcana.persistence.DiaryWorldData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;

import java.util.*;

import static com.example.arcana.persistence.DiaryPersistenceHandler.isDiaryBondActive;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class DreamReminderEvent {

    private static final String FIRST_DREAM_MSG_TEMPLATE = "Essa noite sonhei com números... %s.\nNão faço ideia do que significa!";
    private static final String[] RANDOM_DREAM_MSG_TEMPLATES = new String[]{
            "De novo sonhei com os números %s.\nAlgo me diz que estão relacionados à planície...",
            "Os números %s voltaram no meu sonho.\nPreciso descobrir o que representam.",
            "Outra noite com os números %s em minha mente.\nSinto que é um caminho a seguir.",
            "Mais uma vez os números %s aparecem.\nEles parecem indicar algo importante.",
            "Sonhei novamente com %s.\nAlgo me aguarda na estrutura da planície."
    };

    private static final Map<UUID, List<Integer>> PLAYER_RANDOM_ORDER = new HashMap<>();
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        handleDreams(player);
    }

    private static void handleDreams(ServerPlayer player) {
        checkUnclaimedDiaryDream(player);
    }

    private static void checkUnclaimedDiaryDream(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        DiaryWorldData data = DiaryWorldData.get(level);

        ArcanaMod.LOGGER.debug("Checando sonho do diário para {}", player.getName().getString());

        if (!data.hasSpawned()) {
            ArcanaMod.LOGGER.debug("Estrutura do diário ainda não gerada.");
            return;
        }

        if (isDiaryBondActive(player)) {
            ArcanaMod.LOGGER.debug("Jogador {} já recebeu o diário", player.getName().getString());
            return;
        }

        BlockPos pos = data.getStructurePos();
        if (pos == null) {
            ArcanaMod.LOGGER.warn("Estrutura marcada como gerada mas posição é nula!");
            return;
        }

        ArcanaMod.LOGGER.debug("Enviando sonho do diário para {} na posição {}", player.getName().getString(), pos);
        sendDreamMessage(player, pos);
    }

    private static void sendDreamMessage(ServerPlayer player, BlockPos pos) {
        String numbers = String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ());
        boolean isFirstDream = !PLAYER_RANDOM_ORDER.containsKey(player.getUUID());

        String messageText;
        if (isFirstDream) {
            messageText = String.format(FIRST_DREAM_MSG_TEMPLATE, numbers);
            PLAYER_RANDOM_ORDER.put(player.getUUID(), generateRandomOrder());
        } else {
            List<Integer> order = PLAYER_RANDOM_ORDER.get(player.getUUID());
            if (order.isEmpty()) order.addAll(generateRandomOrder());
            int msgIndex = order.removeFirst();
            messageText = String.format(RANDOM_DREAM_MSG_TEMPLATES[msgIndex], numbers);
        }

        player.sendSystemMessage(
                Component.literal(messageText)
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
        );
    }

    private static List<Integer> generateRandomOrder() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < RANDOM_DREAM_MSG_TEMPLATES.length; i++) list.add(i);
        Collections.shuffle(list, RANDOM);
        return list;
    }
}
