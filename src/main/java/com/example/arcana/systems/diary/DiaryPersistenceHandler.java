package com.example.arcana.systems.diary;

import com.example.arcana.registry.ModItems;
import com.example.arcana.util.common.ArcanaLog;
import com.example.arcana.util.server.DelayedMessageHandler;
import com.example.arcana.util.server.DelayedMessageQueue;
import com.example.arcana.util.server.PlayerPersistentDataUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

public class DiaryPersistenceHandler {
    private static final String MODULE = "DIARY";
    private static final String PLAYER_BOUND_KEY = "arcana.diary_bound";
    private static final Map<UUID, Deque<Integer>> PLAYER_MESSAGE_ORDER = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static final Map<UUID, Long> SCHEDULED_RETURNS = new HashMap<>();
    private static final int RETURN_DELAY_TICKS = 100;
    private static final Map<UUID, TrackedDrop> TRACKED_DROPS = new HashMap<>();

    private record TrackedDrop(UUID owner, UUID entityUuid, long expireTick) {}

    public static void handleItemToss(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        ItemEntity entity = event.getEntity();
        ItemStack stack = entity.getItem();
        if (!isDiary(stack)) return;

        long expireTick = player.serverLevel().getServer().getTickCount() + RETURN_DELAY_TICKS;
        TRACKED_DROPS.put(entity.getUUID(), new TrackedDrop(player.getUUID(), entity.getUUID(), expireTick));
        ArcanaLog.playerDebug(MODULE, player, "Diary tossed and registered for tracking");
    }

    public static void handlePlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        restoreDiaryIfBondedAndMissing(player);
    }

    public static void handlePlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        restoreDiaryIfBondedAndMissing(player);
    }

    public static void handleContainerClose(PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isDiaryBondActive(player)) return;

        event.getContainer().slots.forEach(slot -> {
            if (slot.hasItem() && isDiary(slot.getItem()) && !(slot.container instanceof Inventory)) {
                slot.set(ItemStack.EMPTY);
                slot.setChanged();
                ensureDiaryReturnScheduled(player);
                ArcanaLog.playerDebug(MODULE, player, "Diary removed from external container and scheduled to return");
            }
        });
    }

    public static void handleServerTick(ServerTickEvent.Post event) {
        long tick = event.getServer().getTickCount();
        processTrackedDrops(event, tick);
        processScheduledReturns(event, tick);
        checkDiaryBondForAllPlayers(event);
    }

    private static void restoreDiaryIfBondedAndMissing(ServerPlayer player) {
        if (!isDiaryBondActive(player) || playerHasDiary(player)) return;
        ensureDiaryReturnScheduled(player);
        ArcanaLog.playerDebug(MODULE, player, "Player bonded to diary but missing it, scheduling return");
    }

    private static void processTrackedDrops(ServerTickEvent.Post event, long tick) {
        TRACKED_DROPS.values().removeIf(drop -> {
            var level = event.getServer().overworld();
            ItemEntity entity = (ItemEntity) level.getEntity(drop.entityUuid());

            if (entity == null) {
                ServerPlayer owner = event.getServer().getPlayerList().getPlayer(drop.owner());
                if (owner != null && !playerHasDiary(owner)) {
                    giveDiaryWithReminder(owner, "Diary returned because the dropped entity disappeared");
                }
                return true;
            }

            if (drop.expireTick() > tick) return false;

            entity.discard();
            ServerPlayer owner = event.getServer().getPlayerList().getPlayer(drop.owner());
            if (owner != null && !playerHasDiary(owner)) {
                giveDiaryWithReminder(owner, "Diary returned after drop expiration time");
            }

            return true;
        });
    }

    private static void processScheduledReturns(ServerTickEvent.Post event, long tick) {
        Iterator<Map.Entry<UUID, Long>> it = SCHEDULED_RETURNS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (entry.getValue() <= tick) {
                ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
                if (player != null) {
                    boolean success = giveDiary(player);
                    if (success) {
                        sendDiaryReminderMessage(player);
                        it.remove();
                    }
                } else {
                    it.remove();
                }
            }
        }
    }

    private static void checkDiaryBondForAllPlayers(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ensureDiaryBond(player);
        }
    }

    private static void ensureDiaryBond(ServerPlayer player) {
        if (!isDiaryBondActive(player) && playerHasDiary(player)) {
            PlayerPersistentDataUtil.setBoolean(player, PLAYER_BOUND_KEY, true);
            ArcanaLog.playerInfo(MODULE, player, "Player bonded to diary after acquiring it");
        }
    }

    public static boolean isDiaryBondActive(ServerPlayer player) {
        return PlayerPersistentDataUtil.getBoolean(player, PLAYER_BOUND_KEY);
    }

    private static boolean isDiary(ItemStack stack) {
        return stack.getItem() == ModItems.DIARY_KALIASTRUS.get();
    }

    private static boolean giveDiary(ServerPlayer player) {
        if (playerHasDiary(player)) return true;
        ItemStack stack = new ItemStack(ModItems.DIARY_KALIASTRUS.get());

        boolean added = player.getInventory().add(stack);
        if (added) {
            PlayerPersistentDataUtil.setBoolean(player, PLAYER_BOUND_KEY, true);
            ArcanaLog.playerInfo(MODULE, player, "Diary added to player inventory");
            return true;
        } else {
            return false;
        }
    }

    private static void giveDiaryWithReminder(ServerPlayer player, String logText) {
        boolean added = giveDiary(player);
        if (added) {
            sendDiaryReminderMessage(player);
            ArcanaLog.playerInfo(MODULE, player, logText);
        } else {
            long tick = player.serverLevel().getServer().getTickCount();
            SCHEDULED_RETURNS.put(player.getUUID(), tick + 20);
            ArcanaLog.playerInfo(MODULE, player, "Inventory full, diary return rescheduled");
        }
    }

    private static boolean playerHasDiary(ServerPlayer player) {
        return player.getInventory().items.stream().anyMatch(DiaryPersistenceHandler::isDiary)
                || isDiary(player.getOffhandItem())
                || isDiary(player.getMainHandItem())
                || cursorHasDiary(player);
    }

    private static boolean cursorHasDiary(ServerPlayer player) {
        ItemStack carried = player.containerMenu.getCarried();
        return !carried.isEmpty() && isDiary(carried);
    }

    private static void sendDiaryReminderMessage(ServerPlayer player) {
        UUID id = player.getUUID();
        Deque<Integer> order = PLAYER_MESSAGE_ORDER.computeIfAbsent(id, k -> generateMessageOrder());

        if (order.isEmpty()) {
            order = generateMessageOrder();
            PLAYER_MESSAGE_ORDER.put(id, order);
        }

        int index = order.removeFirst();

        List<Component> msgs = DiaryMessageLoader.getMessages(player);
        if (index >= msgs.size()) index = 0;

        DelayedMessageQueue queue = new DelayedMessageQueue(player, 20);
        queue.addMessage(msgs.get(index));
        DelayedMessageHandler.addQueue(queue);
    }

    private static Deque<Integer> generateMessageOrder() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);
        Collections.shuffle(list, RANDOM);
        return new ArrayDeque<>(list);
    }

    private static void ensureDiaryReturnScheduled(ServerPlayer player) {
        player.serverLevel().getServer().execute(() -> scheduleDiaryReturn(player));
    }

    private static void scheduleDiaryReturn(ServerPlayer player) {
        long targetTick = player.serverLevel().getServer().getTickCount() + RETURN_DELAY_TICKS;
        SCHEDULED_RETURNS.put(player.getUUID(), targetTick);
        ArcanaLog.playerDebug(MODULE, player, "Diary scheduled to return");
    }

    public static void preventExtraDiaryPickup(ServerPlayer player) {
        if (!playerHasDiary(player)) return;

        List<ItemEntity> nearby = player.level().getEntitiesOfClass(
            ItemEntity.class,
            player.getBoundingBox().inflate(1.5),
            e -> isDiary(e.getItem())
        );

        for (ItemEntity entity : nearby) {
            entity.setPickUpDelay(20);
            ArcanaLog.playerDebug(MODULE, player, "Blocked picking up extra diary from ground");
        }
    }
}
