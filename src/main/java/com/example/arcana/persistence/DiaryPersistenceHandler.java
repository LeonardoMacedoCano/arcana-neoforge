package com.example.arcana.persistence;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.DelayedMessageHandler;
import com.example.arcana.util.DelayedMessageQueue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class DiaryPersistenceHandler {

    private static final String PLAYER_BOUND_KEY = "arcana.diary_bound";
    private static final Map<UUID, List<Integer>> PLAYER_MESSAGE_ORDER = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static final Set<UUID> SESSION_ALREADY_HAD_DIARY = new HashSet<>();
    private static final Map<UUID, Integer> DIARY_MISSING_TICKS = new HashMap<>();
    private static final int RETURN_DELAY_TICKS = 200;

    private static final Component[] MESSAGES = new Component[]{
            Component.literal("Hei… você me deixou cair.\nEu ainda tenho histórias pra te contar.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.literal("Ei… esse chão é frio demais pra mim.\nNão me abandone agora.")
                    .withStyle(ChatFormatting.DARK_PURPLE),
            Component.literal("Somos parte do mesmo destino agora.\nNão me perca.")
                    .withStyle(ChatFormatting.DARK_PURPLE),
            Component.literal("Eu caí… mas continuo com você.\nSempre voltarei.")
                    .withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.literal("Não dá pra fugir de mim tão fácil.\nAinda precisamos terminar essa história.")
                    .withStyle(ChatFormatting.DARK_PURPLE)
    };

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (!hasPlayerReceivedDiary(player) && playerHasDiary(player)) {
            markDiaryAsReceived(player);
            ArcanaMod.LOGGER.debug("Jogador {} pegou o diário!", player.getName().getString());
        }

        if (!isDiaryBondActive(player)) {
            tryBindPlayerToDiary(player);
            return;
        }

        if (playerHasDiary(player)) {
            resetMissingTimer(player);
            return;
        }

        if (!hasExceededReturnDelay(player)) return;
        if (isHoldingCursorItem(player)) return;

        removeWorldDiaryInstances(player);
        sendSmartDiaryMessage(player);
        returnDiary(player);
    }

    public static boolean hasPlayerReceivedDiary(ServerPlayer player) {
        UUID id = player.getUUID();
        if (SESSION_ALREADY_HAD_DIARY.contains(id)) return true;
        boolean received = player.getPersistentData().getBoolean(PLAYER_BOUND_KEY);
        if (received) SESSION_ALREADY_HAD_DIARY.add(id);
        return received;
    }

    private static void markDiaryAsReceived(ServerPlayer player) {
        player.getPersistentData().putBoolean(PLAYER_BOUND_KEY, true);
        SESSION_ALREADY_HAD_DIARY.add(player.getUUID());
    }

    public static boolean isDiaryBondActive(ServerPlayer player) {
        return SESSION_ALREADY_HAD_DIARY.contains(player.getUUID()) ||
                player.getPersistentData().getBoolean(PLAYER_BOUND_KEY);
    }

    private static void tryBindPlayerToDiary(ServerPlayer player) {
        if (!playerHasDiary(player)) return;
        markDiaryAsReceived(player);
    }

    private static boolean playerHasDiary(ServerPlayer player) {
        if (inventoryContainsDiary(player)) return true;
        if (player.getOffhandItem().getItem() == ArcanaMod.DIARY_KALIASTRUS.get()) return true;
        if (player.getMainHandItem().getItem() == ArcanaMod.DIARY_KALIASTRUS.get()) return true;
        return cursorHasDiary(player);
    }

    private static boolean inventoryContainsDiary(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items)
            if (stack.getItem() == ArcanaMod.DIARY_KALIASTRUS.get())
                return true;
        return false;
    }

    private static boolean cursorHasDiary(ServerPlayer player) {
        ItemStack carried = player.containerMenu.getCarried();
        if (carried.isEmpty()) return false;
        return carried.getItem() == ArcanaMod.DIARY_KALIASTRUS.get();
    }

    private static void resetMissingTimer(ServerPlayer player) {
        DIARY_MISSING_TICKS.remove(player.getUUID());
    }

    private static boolean hasExceededReturnDelay(ServerPlayer player) {
        UUID id = player.getUUID();
        int ticks = DIARY_MISSING_TICKS.getOrDefault(id, 0) + 1;
        DIARY_MISSING_TICKS.put(id, ticks);
        if (ticks < RETURN_DELAY_TICKS) return false;
        DIARY_MISSING_TICKS.remove(id);
        return true;
    }

    private static boolean isHoldingCursorItem(ServerPlayer player) {
        return !player.containerMenu.getCarried().isEmpty();
    }

    private static void removeWorldDiaryInstances(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<ItemEntity> toRemove = new ArrayList<>();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(64))) {
            if (item.getItem().getItem() == ArcanaMod.DIARY_KALIASTRUS.get()) toRemove.add(item);
        }
        toRemove.forEach(ItemEntity::discard);
    }

    private static void sendSmartDiaryMessage(ServerPlayer player) {
        UUID id = player.getUUID();
        List<Integer> order = PLAYER_MESSAGE_ORDER.computeIfAbsent(id, k -> generateOrder());
        if (order.isEmpty()) {
            order = generateOrder();
            PLAYER_MESSAGE_ORDER.put(id, order);
        }
        int msgIndex = order.removeFirst();
        Component msg = MESSAGES[msgIndex];
        DelayedMessageQueue queue = new DelayedMessageQueue(player, 20);
        queue.addMessage(msg);
        DelayedMessageHandler.addQueue(queue);
    }

    private static List<Integer> generateOrder() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);
        Collections.shuffle(list, RANDOM);
        return new ArrayList<>(list);
    }

    private static void returnDiary(ServerPlayer player) {
        player.getInventory().add(new ItemStack(ArcanaMod.DIARY_KALIASTRUS.get()));
    }
}
