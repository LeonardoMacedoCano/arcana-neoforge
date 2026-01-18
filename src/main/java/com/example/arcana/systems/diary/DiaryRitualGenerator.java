package com.example.arcana.systems.diary;

import com.example.arcana.content.item.DiaryItem;
import com.example.arcana.util.common.ArcanaLog;
import com.example.arcana.util.server.DelayedMessageHandler;
import com.example.arcana.util.server.DelayedMessageQueue;
import com.example.arcana.util.server.PlayerPersistentDataUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import static com.example.arcana.registry.ModItems.DIARY_KALIASTRUS;

public class DiaryRitualGenerator {
    private static final String MODULE = "DIARY_RITUAL";
    private static final String PLAYER_RECEIVED_RITUAL_KEY = "arcana.diary_received";
    private static final int SEARCH_RADIUS = 120;
    private static final int SEARCH_STEP = 6;
    private static final int AREA_MIN_OPEN_RADIUS = 12;
    private static final int CLEAR_RADIUS = 9;
    private static final int GROUND_RADIUS = 9;
    private static final int CIRCLE_RADIUS = 7;
    private static final int MIN_Y = 50;
    private static final int MAX_Y = 200;

    public static void tryGenerateRitual(PlayerEvent event) {
        if (shouldIgnoreEvent(event)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();

        ArcanaLog.debug(MODULE, "Processing PlayerTick for {}", player.getName().getString());

        boolean structureGenerated = generateRitualStructure(player.serverLevel(), player.blockPosition(), player);

        if (structureGenerated) {
            ArcanaLog.debug(MODULE, "Ritual generated successfully. Sending narrative messages.");
            markAsReceived(player);
            sendNarrativeMessages(player);
        } else {
            ArcanaLog.debug(MODULE, "Ritual structure not generated.");
        }
    }

    private static boolean shouldIgnoreEvent(PlayerEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return true;
        }
        return !isInPlains(player) || hasPlayerReceivedRitualDiary(player);
    }

    public static boolean hasPlayerReceivedRitualDiary(ServerPlayer player) {
        return PlayerPersistentDataUtil.getBoolean(player, PLAYER_RECEIVED_RITUAL_KEY);
    }

    private static void markAsReceived(ServerPlayer player) {
        PlayerPersistentDataUtil.setBoolean(player, PLAYER_RECEIVED_RITUAL_KEY, true);
    }

    private static boolean isInPlains(ServerPlayer player) {
        return player.serverLevel().getBiome(player.blockPosition()).is(Biomes.PLAINS);
    }

    private static void sendNarrativeMessages(ServerPlayer player) {
        ArcanaLog.debug(MODULE, "Preparing narrative messages for {}", player.getName().getString());

        var list = DiaryRitualMessageLoader.getMessages(player);
        String playerName = player.getName().getString();

        DelayedMessageQueue queue = new DelayedMessageQueue(player, 40);

        list.forEach(line -> {
            line = line.replace("%player%", playerName);

            if (line.equalsIgnoreCase("GOODBYE") || line.equalsIgnoreCase("ADEUS")) {
                queue.addMessage(Component.literal(line)
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
            } else if (line.contains("WORLD") || line.contains("MUNDO")) {
                queue.addMessage(Component.literal(line)
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
            } else {
                queue.addMessage(Component.literal(line)
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
            }
        });

        DelayedMessageHandler.addQueue(queue);
    }

    private static boolean generateRitualStructure(ServerLevel level, BlockPos playerPos, ServerPlayer player) {
        ArcanaLog.debug(MODULE, "Attempting to generate ritual structure...");

        DiaryWorldData state = DiaryWorldData.get(level);
        if (state.hasSpawned()) {
            ArcanaLog.debug(MODULE, "Ritual already generated previously. Aborting.");
            return false;
        }

        BlockPos base = findValidPlainsSpot(level, playerPos);
        if (base == null) {
            ArcanaLog.debug(MODULE, "No valid ritual spot found.");
            return false;
        }

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base);
        if (surface.getY() < MIN_Y || surface.getY() > MAX_Y) {
            ArcanaLog.debug(MODULE, "Found position outside allowed height. Y={}", surface.getY());
            return false;
        }

        ArcanaLog.debug(MODULE, "Valid ritual location found at {}", surface);

        clearAbove(level, surface);
        ArcanaLog.debug(MODULE, "Cleared ritual area.");

        buildRitual(level, surface, player);
        ArcanaLog.debug(MODULE, "Ritual built successfully.");

        state.setSpawned(surface);
        ArcanaLog.debug(MODULE, "World state updated: ritual marked as generated.");

        return true;
    }

    private static BlockPos findValidPlainsSpot(ServerLevel level, BlockPos origin) {
        ArcanaLog.debug(MODULE, "Searching ritual terrain...");

        for (int r = SEARCH_STEP; r <= SEARCH_RADIUS; r += SEARCH_STEP)
            for (int x = -r; x <= r; x += SEARCH_STEP)
                for (int z = -r; z <= r; z += SEARCH_STEP) {
                    BlockPos candidate = origin.offset(x, 0, z);
                    if (isGoodSpot(level, candidate)) {
                        ArcanaLog.debug(MODULE, "Valid spot found at {}", candidate);
                        return level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, candidate);
                    }
                }

        return null;
    }

    private static boolean isGoodSpot(ServerLevel level, BlockPos pos) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        if (surface.getY() < MIN_Y || surface.getY() > MAX_Y) return false;

        ResourceKey<Biome> biome = level.getBiome(surface).unwrapKey().orElse(null);
        if (biome == null || !biome.equals(Biomes.PLAINS)) return false;

        BlockPos ground = surface.below();
        var block = level.getBlockState(ground).getBlock();
        if (!(block == Blocks.GRASS_BLOCK
                || block == Blocks.DIRT
                || block == Blocks.COARSE_DIRT
                || block == Blocks.ROOTED_DIRT)) {
            return false;
        }

        return hasOpenArea(level, surface);
    }

    private static boolean hasOpenArea(ServerLevel level, BlockPos center) {
        int baseY = center.getY();
        for (int x = -AREA_MIN_OPEN_RADIUS; x <= AREA_MIN_OPEN_RADIUS; x++)
            for (int z = -AREA_MIN_OPEN_RADIUS; z <= AREA_MIN_OPEN_RADIUS; z++) {
                BlockPos check = center.offset(x, 0, z);
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, check);
                if (surface.getY() - baseY > 2) return false;
            }
        return true;
    }

    private static void buildRitual(ServerLevel level, BlockPos base, ServerPlayer player) {
        placeGround(level, base);
        placeMagicCircle(level, base);
        placeDetails(level, base);
        placeChest(level, base, player);
    }

    private static void clearAbove(ServerLevel level, BlockPos chestPos) {
        for (int x = -CLEAR_RADIUS; x <= CLEAR_RADIUS; x++)
            for (int z = -CLEAR_RADIUS; z <= CLEAR_RADIUS; z++)
                for (int y = 0; y <= 25; y++) {
                    BlockPos p = chestPos.offset(x, y, z);
                    if (!level.isEmptyBlock(p))
                        level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
                }
    }

    private static void placeGround(ServerLevel level, BlockPos base) {
        for (int x = -GROUND_RADIUS; x <= GROUND_RADIUS; x++)
            for (int z = -GROUND_RADIUS; z <= GROUND_RADIUS; z++)
                if (Math.sqrt(x * x + z * z) <= GROUND_RADIUS)
                    applyGroundVariation(level, base.offset(x, -1, z));
    }

    private static void applyGroundVariation(ServerLevel level, BlockPos pos) {
        switch (level.random.nextInt(3)) {
            case 0 -> level.setBlockAndUpdate(pos, Blocks.MOSS_BLOCK.defaultBlockState());
            case 1 -> level.setBlockAndUpdate(pos, Blocks.ROOTED_DIRT.defaultBlockState());
            default -> level.setBlockAndUpdate(pos, Blocks.COARSE_DIRT.defaultBlockState());
        }
    }

    private static void placeMagicCircle(ServerLevel level, BlockPos base) {
        for (int x = -CIRCLE_RADIUS; x <= CIRCLE_RADIUS; x++)
            for (int z = -CIRCLE_RADIUS; z <= CIRCLE_RADIUS; z++) {
                double dist = Math.sqrt(x * x + z * z);
                BlockPos pos = base.offset(x, -1, z);

                if (dist <= CIRCLE_RADIUS && dist >= CIRCLE_RADIUS - 1)
                    level.setBlockAndUpdate(pos, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                else if (dist <= CIRCLE_RADIUS - 2 && dist >= CIRCLE_RADIUS - 3)
                    level.setBlockAndUpdate(pos, Blocks.GILDED_BLACKSTONE.defaultBlockState());
                else if (dist < CIRCLE_RADIUS - 3)
                    level.setBlockAndUpdate(pos, Blocks.SMOOTH_BASALT.defaultBlockState());
            }

        level.setBlockAndUpdate(base.offset(3, -1, 0), Blocks.GLOWSTONE.defaultBlockState());
        level.setBlockAndUpdate(base.offset(-3, -1, 0), Blocks.GLOWSTONE.defaultBlockState());
        level.setBlockAndUpdate(base.offset(0, -1, 3), Blocks.GLOWSTONE.defaultBlockState());
        level.setBlockAndUpdate(base.offset(0, -1, -3), Blocks.GLOWSTONE.defaultBlockState());
    }

    private static void placeDetails(ServerLevel level, BlockPos base) {
        BlockPos p1 = base.offset(6, 0, 6);
        BlockPos p2 = base.offset(-6, 0, 6);
        BlockPos p3 = base.offset(6, 0, -6);
        BlockPos p4 = base.offset(-6, 0, -6);

        level.setBlockAndUpdate(p1, Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
        level.setBlockAndUpdate(p2, Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
        level.setBlockAndUpdate(p3, Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());
        level.setBlockAndUpdate(p4, Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState());

        level.setBlockAndUpdate(p1.above(), Blocks.SOUL_TORCH.defaultBlockState());
        level.setBlockAndUpdate(p2.above(), Blocks.SOUL_TORCH.defaultBlockState());
        level.setBlockAndUpdate(p3.above(), Blocks.SOUL_TORCH.defaultBlockState());
        level.setBlockAndUpdate(p4.above(), Blocks.SOUL_TORCH.defaultBlockState());

        ArcanaLog.debug(MODULE, "Decorative ritual details placed.");
    }

    private static void placeChest(ServerLevel level, BlockPos base, ServerPlayer player) {
        level.setBlockAndUpdate(base, Blocks.CHEST.defaultBlockState());
        ArcanaLog.debug(MODULE, "Chest placed at ritual center {}", base);
        addDiary(level, base, player);
    }

    private static void addDiary(Level level, BlockPos chestPos, ServerPlayer player) {
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            ItemStack diary = DiaryItem.createBoundDiary(player, DIARY_KALIASTRUS.get());
            chest.setItem(13, diary);
            ArcanaLog.debug(MODULE, "Bound diary inserted into chest at {}", chestPos);
        } else {
            ArcanaLog.debug(MODULE, "Failed to insert diary. Chest not found at {}", chestPos);
        }
    }
}
