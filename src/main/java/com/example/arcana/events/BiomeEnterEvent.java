package com.example.arcana.events;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.DelayedMessageHandler;
import com.example.arcana.util.DelayedMessageQueue;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class BiomeEnterEvent {

    private static final Set<UUID> PLAYERS_TRIGGERED = new HashSet<>();
    private static final int STRUCTURE_SEARCH_RADIUS = 120;
    private static final int STRUCTURE_SEARCH_STEP = 6;
    private static final int STRUCTURE_CLEAR_SIZE = 10;
    private static final int GROUND_RADIUS = 8;
    private static final int CIRCLE_RADIUS = 7;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (PLAYERS_TRIGGERED.contains(player.getUUID())) return;
        if (!isPlayerInPlains(player)) return;

        PLAYERS_TRIGGERED.add(player.getUUID());
        sendMysticMessages(player);
        generateStructure(player.serverLevel(), player.blockPosition());
    }

    private static boolean isPlayerInPlains(ServerPlayer player) {
        ResourceKey<Biome> biomeKey = player.serverLevel().getBiome(player.blockPosition()).unwrapKey().orElse(null);
        return biomeKey != null && biomeKey.equals(Biomes.PLAINS);
    }

    private static void sendMysticMessages(ServerPlayer player) {
        DelayedMessageQueue queue = new DelayedMessageQueue(player, 40);
        queue.addMessage(Component.literal(player.getName().getString() + ",").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        queue.addMessage(Component.literal("eu não conquistei O MUNDO...").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        queue.addMessage(Component.literal("agora deixo meus últimos resquícios de existência contigo.").withStyle(ChatFormatting.DARK_PURPLE));
        queue.addMessage(Component.literal("ADEUS").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        DelayedMessageHandler.addQueue(queue);
    }

    private static void generateStructure(ServerLevel level, BlockPos playerPos) {
        BlockPos base = findValidBiomePos(level, playerPos);
        if (base == null) return;
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base);
        buildRitualStructure(level, surface);
    }

    private static BlockPos findValidBiomePos(ServerLevel level, BlockPos origin) {
        for (int r = STRUCTURE_SEARCH_STEP; r <= STRUCTURE_SEARCH_RADIUS; r += STRUCTURE_SEARCH_STEP)
            for (int x = -r; x <= r; x += STRUCTURE_SEARCH_STEP)
                for (int z = -r; z <= r; z += STRUCTURE_SEARCH_STEP)
                    if (isValidBiomeSpot(level, origin.offset(x, 0, z)))
                        return level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.offset(x, 0, z));
        return null;
    }

    private static boolean isValidBiomeSpot(ServerLevel level, BlockPos pos) {
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        ResourceKey<Biome> foundBiome = level.getBiome(surface).unwrapKey().orElse(null);
        if (foundBiome == null || !foundBiome.equals(Biomes.PLAINS)) return false;
        return isAreaFlatAndClear(level, surface);
    }

    private static boolean isAreaFlatAndClear(ServerLevel level, BlockPos center) {
        int half = STRUCTURE_CLEAR_SIZE / 2;
        int baseY = center.getY();

        for (int x = -half; x <= half; x++)
            for (int z = -half; z <= half; z++) {
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.offset(x, 0, z));
                if (Math.abs(surface.getY() - baseY) > 1) return false;
                if (level.getBlockState(surface.below()).getBlock() == Blocks.WATER) return false;
            }

        return true;
    }

    private static void buildRitualStructure(ServerLevel level, BlockPos base) {
        placeMysticGround(level, base);
        placeTarotCircle(level, base);
        placeArcaneDetails(level, base);
        placeRitualChest(level, base);
    }

    private static void placeMysticGround(ServerLevel level, BlockPos base) {
        for (int x = -GROUND_RADIUS; x <= GROUND_RADIUS; x++)
            for (int z = -GROUND_RADIUS; z <= GROUND_RADIUS; z++)
                if (Math.sqrt(x * x + z * z) <= GROUND_RADIUS)
                    applyRandomGroundBlock(level, base.offset(x, -1, z));
    }

    private static void applyRandomGroundBlock(ServerLevel level, BlockPos pos) {
        switch (level.random.nextInt(3)) {
            case 0 -> level.setBlockAndUpdate(pos, Blocks.MOSS_BLOCK.defaultBlockState());
            case 1 -> level.setBlockAndUpdate(pos, Blocks.ROOTED_DIRT.defaultBlockState());
            default -> level.setBlockAndUpdate(pos, Blocks.COARSE_DIRT.defaultBlockState());
        }
    }

    private static void placeTarotCircle(ServerLevel level, BlockPos base) {
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

    private static void placeArcaneDetails(ServerLevel level, BlockPos base) {
        level.setBlockAndUpdate(base.offset(6, 0, 6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(base.offset(-6, 0, 6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(base.offset(6, 0, -6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        level.setBlockAndUpdate(base.offset(-6, 0, -6), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
    }

    private static void placeRitualChest(ServerLevel level, BlockPos base) {
        BlockPos chestPos = base.offset(0, 0, 0);
        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        addDiaryToChest(level, chestPos);
    }

    private static void addDiaryToChest(Level level, BlockPos chestPos) {
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest)
            chest.setItem(13, new ItemStack(ArcanaMod.DIARY_KALIASTRUS.get()));
    }
}
