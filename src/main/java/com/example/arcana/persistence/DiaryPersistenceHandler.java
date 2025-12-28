package com.example.arcana.persistence;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.DelayedMessageHandler;
import com.example.arcana.util.DelayedMessageQueue;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class DiaryPersistenceHandler {

    private static final String PLAYER_BOUND_KEY = "arcana.diary_bound";
    private static final Map<UUID, List<Integer>> PLAYER_MESSAGE_ORDER = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static final Map<UUID, Integer> DIARY_MISSING_TICKS = new HashMap<>();
    private static final int RETURN_DELAY_TICKS = 200;

    private static final Component[] MESSAGES = new Component[]{
            Component.literal("Oi… você se afastou e me deixou sozinho.\nAinda tenho histórias para te contar.").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.literal("Ei… não importa onde estou, espero que não me perca agora.").withStyle(ChatFormatting.DARK_PURPLE),
            Component.literal("Estamos conectados de alguma forma.\nNão me deixe para trás.").withStyle(ChatFormatting.DARK_PURPLE),
            Component.literal("Mesmo que eu esteja longe, continuo com você.\nSempre voltarei.").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.literal("Não dá para fugir de mim tão fácil.\nAinda temos muito a compartilhar.").withStyle(ChatFormatting.DARK_PURPLE)
    };

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        registerDiaryPickup(player);

        if (!isDiaryBondActive(player)) {
            bindDiaryToPlayerIfHeld(player);
            return;
        }

        if (playerHasDiary(player)) {
            resetDiaryMissingTimer(player);
            return;
        }

        if (!canReturnDiary(player)) return;
        if (playerIsHoldingItem(player)) return;

        ArcanaMod.LOGGER.debug("[Diary] Recuperação iniciada para {}", player.getName().getString());

        removeAllDiariesFromWorldAndPlayer(player);
        sendDiaryReminderMessage(player);
        returnDiaryToPlayer(player);

        ArcanaMod.LOGGER.debug("[Diary] Diário devolvido ao jogador {}", player.getName().getString());
    }

    public static boolean hasPlayerReceivedDiary(ServerPlayer player) {
        return player.getPersistentData().getBoolean(PLAYER_BOUND_KEY);
    }

    private static void registerDiaryPickup(ServerPlayer player) {
        if (!hasPlayerReceivedDiary(player) && playerHasDiary(player)) {
            markDiaryAsReceived(player);
            ArcanaMod.LOGGER.debug("[Diary] Jogador {} vinculou-se ao diário", player.getName().getString());
        }
    }

    private static void markDiaryAsReceived(ServerPlayer player) {
        player.getPersistentData().putBoolean(PLAYER_BOUND_KEY, true);
    }

    public static boolean isDiaryBondActive(ServerPlayer player) {
        return player.getPersistentData().getBoolean(PLAYER_BOUND_KEY);
    }

    private static void bindDiaryToPlayerIfHeld(ServerPlayer player) {
        if (playerHasDiary(player)) markDiaryAsReceived(player);
    }

    private static boolean playerHasDiary(ServerPlayer player) {
        return inventoryContainsDiary(player)
                || player.getOffhandItem().getItem() == ArcanaMod.DIARY_KALIASTRUS.get()
                || player.getMainHandItem().getItem() == ArcanaMod.DIARY_KALIASTRUS.get()
                || cursorHasDiary(player);
    }

    private static boolean inventoryContainsDiary(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items)
            if (stack.getItem() == ArcanaMod.DIARY_KALIASTRUS.get())
                return true;
        return false;
    }

    private static boolean cursorHasDiary(ServerPlayer player) {
        ItemStack carried = player.containerMenu.getCarried();
        return !carried.isEmpty() && carried.getItem() == ArcanaMod.DIARY_KALIASTRUS.get();
    }

    private static void resetDiaryMissingTimer(ServerPlayer player) {
        DIARY_MISSING_TICKS.remove(player.getUUID());
    }

    private static boolean canReturnDiary(ServerPlayer player) {
        UUID id = player.getUUID();
        int ticks = DIARY_MISSING_TICKS.getOrDefault(id, 0) + 1;
        DIARY_MISSING_TICKS.put(id, ticks);
        if (ticks < RETURN_DELAY_TICKS) return false;
        DIARY_MISSING_TICKS.remove(id);
        ArcanaMod.LOGGER.debug("[Diary] Diário ausente tempo suficiente para {}", player.getName().getString());
        return true;
    }

    private static boolean playerIsHoldingItem(ServerPlayer player) {
        return !player.containerMenu.getCarried().isEmpty();
    }

    private static void removeAllDiariesFromWorldAndPlayer(ServerPlayer player) {
        ArcanaMod.LOGGER.debug("[Diary] Removendo diários do mundo e containers para {}", player.getName().getString());
        removeWorldDiaries(player);
        removeAllDiariesFromNearbyContainers(player);
        removeCursorDiary(player);
        removeHandDiaries(player);
        removeInventoryDiaries(player);
    }

    private static void removeWorldDiaries(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(128))) {
            if (item.getItem().getItem() == ArcanaMod.DIARY_KALIASTRUS.get()) item.discard();
        }
    }

    private static void removeAllDiariesFromNearbyContainers(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        int radius = 64;
        BlockPos playerPos = player.blockPosition();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof Container container) {
                        for (int i = 0; i < container.getContainerSize(); i++) {
                            ItemStack stack = container.getItem(i);
                            if (stack.getItem() == ArcanaMod.DIARY_KALIASTRUS.get()) {
                                container.setItem(i, ItemStack.EMPTY);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void removeCursorDiary(ServerPlayer player) {
        ItemStack cursor = player.containerMenu.getCarried();
        if (!cursor.isEmpty() && cursor.getItem() == ArcanaMod.DIARY_KALIASTRUS.get())
            player.containerMenu.setCarried(ItemStack.EMPTY);
    }

    private static void removeHandDiaries(ServerPlayer player) {
        if (player.getMainHandItem().getItem() == ArcanaMod.DIARY_KALIASTRUS.get())
            player.setItemInHand(player.getUsedItemHand(), ItemStack.EMPTY);
        if (player.getOffhandItem().getItem() == ArcanaMod.DIARY_KALIASTRUS.get())
            player.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    private static void removeInventoryDiaries(ServerPlayer player) {
        player.getInventory().items.removeIf(stack -> stack.getItem() == ArcanaMod.DIARY_KALIASTRUS.get());
    }

    private static void sendDiaryReminderMessage(ServerPlayer player) {
        UUID id = player.getUUID();
        List<Integer> order = PLAYER_MESSAGE_ORDER.computeIfAbsent(id, k -> generateMessageOrder());
        if (order.isEmpty()) order.addAll(generateMessageOrder());
        int msgIndex = order.removeFirst();
        Component msg = MESSAGES[msgIndex];
        DelayedMessageQueue queue = new DelayedMessageQueue(player, 20);
        queue.addMessage(msg);
        DelayedMessageHandler.addQueue(queue);
        ArcanaMod.LOGGER.debug("[Diary] Mensagem de lembrança enviada para {}", player.getName().getString());
    }

    private static List<Integer> generateMessageOrder() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);
        Collections.shuffle(list, RANDOM);
        return new ArrayList<>(list);
    }

    private static void returnDiaryToPlayer(ServerPlayer player) {
        if (!playerHasDiary(player))
            player.getInventory().add(new ItemStack(ArcanaMod.DIARY_KALIASTRUS.get()));
    }
}
