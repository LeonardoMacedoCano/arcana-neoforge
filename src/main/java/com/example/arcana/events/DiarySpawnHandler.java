package com.example.arcana.events;

import com.example.arcana.ArcanaMod;
import com.example.arcana.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class DiarySpawnHandler {

    private static final String DIARY_RECEIVED_TAG = "arcana:diary_received";
    private static final int CHEST_CENTER_SLOT = 13;

    @SubscribeEvent
    public static void onPlayerJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (hasReceivedDiary(player)) {
            logDebug("Player " + player.getName().getString() + " already received diary");
            return;
        }

        if (spawnDiaryChestNearPlayer(player)) {
            markDiaryAsReceived(player);
            ArcanaMod.LOGGER.info("Spawned Diary of Kaliastrus chest for player: {}",
                    player.getName().getString());
        } else {
            ArcanaMod.LOGGER.warn("Failed to spawn diary chest for player: {}",
                    player.getName().getString());
        }
    }

    private static boolean hasReceivedDiary(ServerPlayer player) {
        return player.getPersistentData().getBoolean(DIARY_RECEIVED_TAG);
    }

    private static void markDiaryAsReceived(ServerPlayer player) {
        player.getPersistentData().putBoolean(DIARY_RECEIVED_TAG, true);
    }

    private static boolean spawnDiaryChestNearPlayer(ServerPlayer player) {
        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        BlockPos chestPos = findSafeChestPosition(level, playerPos);

        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);

        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            ItemStack diary = new ItemStack(ArcanaMod.DIARY_KALIASTRUS.get());
            chest.setItem(CHEST_CENTER_SLOT, diary);
            logDebug("Chest spawned at: " + chestPos);
            return true;
        }

        return false;
    }

    private static BlockPos findSafeChestPosition(Level level, BlockPos playerPos) {
        int maxDistance = Config.CHEST_SPAWN_DISTANCE.get();

        logDebug("Searching for chest position with max distance: " + maxDistance);

        for (int distance = 2; distance <= maxDistance; distance++) {
            for (int angle = 0; angle < 360; angle += 45) {
                double radians = Math.toRadians(angle);
                int offsetX = (int) Math.round(distance * Math.cos(radians));
                int offsetZ = (int) Math.round(distance * Math.sin(radians));

                BlockPos testPos = playerPos.offset(offsetX, 0, offsetZ);
                BlockPos groundPos = findGroundLevel(level, testPos);

                if (groundPos != null && isPositionSafeForChest(level, groundPos)) {
                    return groundPos;
                }
            }
        }

        BlockPos fallbackPos = playerPos.offset(2, 0, 2);
        logDebug("No safe position found, using fallback: " + fallbackPos);
        return fallbackPos;
    }

    private static BlockPos findGroundLevel(Level level, BlockPos startPos) {
        int searchRange = 10;

        for (int y = 0; y <= searchRange; y++) {
            BlockPos checkBelow = startPos.offset(0, -y, 0);
            if (isValidGroundBlock(level, checkBelow)) {
                return checkBelow.above();
            }
        }

        for (int y = 1; y <= searchRange; y++) {
            BlockPos checkAbove = startPos.offset(0, y, 0);
            if (isValidGroundBlock(level, checkAbove)) {
                return checkAbove.above();
            }
        }

        return null;
    }

    private static boolean isValidGroundBlock(Level level, BlockPos pos) {
        return !level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir() &&
                level.getBlockState(pos.above(2)).isAir();
    }

    private static boolean isPositionSafeForChest(Level level, BlockPos pos) {
        return level.getBlockState(pos).isAir() &&
                level.getBlockState(pos.above()).isAir() &&
                !level.getBlockState(pos.below()).isAir();
    }

    private static void logDebug(String message) {
        if (Config.DEBUG_MODE.get()) {
            ArcanaMod.LOGGER.info("[DEBUG] {}", message);
        }
    }
}