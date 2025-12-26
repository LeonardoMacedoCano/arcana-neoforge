package com.example.arcana.events;

import com.example.arcana.ArcanaMod;
import com.example.arcana.persistence.DiaryWorldData;
import com.example.arcana.util.DelayedMessageHandler;
import com.example.arcana.util.DelayedMessageQueue;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class PlayerPlainsFirstArrivalRitualEvent {

    private static final String PLAYER_RECEIVED_RITUAL_KEY = "arcana.diary_received";
    private static final Set<UUID> SESSION_CACHE = new HashSet<>();

    private static final int SEARCH_RADIUS = 120;
    private static final int SEARCH_STEP = 6;
    private static final int AREA_CLEAR_SIZE = 10;
    private static final int GROUND_RADIUS = 8;
    private static final int CIRCLE_RADIUS = 7;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (shouldIgnoreEvent(event)) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();

        SESSION_CACHE.add(player.getUUID());
        markAsReceived(player);
        sendNarrativeMessages(player);
        generateRitualStructure(player.serverLevel(), player.blockPosition());
    }

    private static boolean shouldIgnoreEvent(PlayerEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return true;
        }

        return !isInPlains(player) || hasPlayerReceivedRitualDiary(player);
    }

    public static boolean hasPlayerReceivedRitualDiary(ServerPlayer player) {
        UUID id = player.getUUID();

        if (SESSION_CACHE.contains(id)) {
            return true;
        }

        boolean received = player.getPersistentData().getBoolean(PLAYER_RECEIVED_RITUAL_KEY);

        if (received) {
            SESSION_CACHE.add(id);
        }

        return received;
    }

    private static void markAsReceived(ServerPlayer player) {
        player.getPersistentData().putBoolean(PLAYER_RECEIVED_RITUAL_KEY, true);
    }

    private static boolean isInPlains(ServerPlayer player) {
        return player.serverLevel()
                .getBiome(player.blockPosition())
                .is(Biomes.PLAINS);
    }

    private static void sendNarrativeMessages(ServerPlayer player) {
        DelayedMessageQueue queue = new DelayedMessageQueue(player, 40);
        queue.addMessage(Component.literal(player.getName().getString() + ",").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        queue.addMessage(Component.literal("eu não conquistei O MUNDO...").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        queue.addMessage(Component.literal("agora deixo meus últimos resquícios de existência contigo.").withStyle(ChatFormatting.DARK_PURPLE));
        queue.addMessage(Component.literal("ADEUS").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        DelayedMessageHandler.addQueue(queue);
    }

    private static void generateRitualStructure(ServerLevel level, BlockPos playerPos) {
        DiaryWorldData state = DiaryWorldData.get(level);

        if (state.hasSpawned()) {
            return;
        }

        BlockPos base = findValidPlainsSpot(level, playerPos);
        if (base == null) return;

        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base);

        buildRitual(level, surface);

        state.setSpawned(surface);
    }


    private static BlockPos findValidPlainsSpot(ServerLevel level, BlockPos origin) {
        for (int r = SEARCH_STEP; r <= SEARCH_RADIUS; r += SEARCH_STEP)
            for (int x = -r; x <= r; x += SEARCH_STEP)
                for (int z = -r; z <= r; z += SEARCH_STEP) {
                    BlockPos candidate = origin.offset(x, 0, z);
                    if (isGoodSpot(level, candidate)) {
                        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, candidate);
                    }
                }

        return null;
    }

    private static boolean isGoodSpot(ServerLevel level, BlockPos pos) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        ResourceKey<Biome> biome = level.getBiome(surface).unwrapKey().orElse(null);

        if (biome == null || !biome.equals(Biomes.PLAINS)) return false;

        return isFlatAndClear(level, surface);
    }

    private static boolean isFlatAndClear(ServerLevel level, BlockPos center) {
        int half = AREA_CLEAR_SIZE / 2;
        int baseY = center.getY();

        for (int x = -half; x <= half; x++)
            for (int z = -half; z <= half; z++) {
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.offset(x, 0, z));
                if (Math.abs(surface.getY() - baseY) > 1) return false;
                if (level.getBlockState(surface.below()).getBlock() == Blocks.WATER) return false;
            }

        return true;
    }

    private static void buildRitual(ServerLevel level, BlockPos base) {
        placeGround(level, base);
        placeMagicCircle(level, base);
        placeDetails(level, base);
        placeChest(level, base);
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
        level.setBlockAndUpdate(base.offset(6, 0, 6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(base.offset(-6, 0, 6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(base.offset(6, 0, -6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(base.offset(-6, 0, -6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
    }

    private static void placeChest(ServerLevel level, BlockPos base) {
        level.setBlockAndUpdate(base, Blocks.CHEST.defaultBlockState());
        addDiary(level, base);
    }

    private static void addDiary(Level level, BlockPos chestPos) {
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setItem(13, new ItemStack(ArcanaMod.DIARY_KALIASTRUS.get()));
        }
    }
}
