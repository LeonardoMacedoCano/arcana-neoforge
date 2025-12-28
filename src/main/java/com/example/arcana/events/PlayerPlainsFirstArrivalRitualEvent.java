package com.example.arcana.events;

import com.example.arcana.ArcanaMod;
import com.example.arcana.item.DiaryItem;
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

import static com.example.arcana.ArcanaMod.DIARY_KALIASTRUS;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class PlayerPlainsFirstArrivalRitualEvent {

    private static final String PLAYER_RECEIVED_RITUAL_KEY = "arcana.diary_received";
    private static final int SEARCH_RADIUS = 120;
    private static final int SEARCH_STEP = 6;
    private static final int AREA_MIN_OPEN_RADIUS = 12;
    private static final int CLEAR_RADIUS = 9;
    private static final int GROUND_RADIUS = 9;
    private static final int CIRCLE_RADIUS = 7;
    private static final int MIN_Y = 50;
    private static final int MAX_Y = 200;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (shouldIgnoreEvent(event)) return;
        ServerPlayer player = (ServerPlayer) event.getEntity();
        ArcanaMod.LOGGER.debug("Evento PlayerTick processado para: {}", player.getName().getString());

        boolean structureGenerated = generateRitualStructure(player.serverLevel(), player.blockPosition(), player);

        if (structureGenerated) {
            ArcanaMod.LOGGER.debug("Estrutura gerada com sucesso, enviando mensagens narrativas.");
            markAsReceived(player);
            sendNarrativeMessages(player);
        } else {
            ArcanaMod.LOGGER.debug("Estrutura não gerada, mensagens narrativas não enviadas.");
        }
    }

    private static boolean shouldIgnoreEvent(PlayerEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return true;
        }

        return !isInPlains(player) || hasPlayerReceivedRitualDiary(player);
    }

    public static boolean hasPlayerReceivedRitualDiary(ServerPlayer player) {
        return player.getPersistentData().getBoolean(PLAYER_RECEIVED_RITUAL_KEY);
    }

    private static void markAsReceived(ServerPlayer player) {
        player.getPersistentData().putBoolean(PLAYER_RECEIVED_RITUAL_KEY, true);
    }

    private static boolean isInPlains(ServerPlayer player) {
        return player.serverLevel().getBiome(player.blockPosition()).is(Biomes.PLAINS);
    }

    private static void sendNarrativeMessages(ServerPlayer player) {
        ArcanaMod.LOGGER.debug("Preparando mensagens narrativas do primeiro sonho para {}", player.getName().getString());
        DelayedMessageQueue queue = new DelayedMessageQueue(player, 40);
        queue.addMessage(Component.literal(player.getName().getString() + ",").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        queue.addMessage(Component.literal("eu não conquistei O MUNDO...").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        queue.addMessage(Component.literal("agora deixo meus últimos resquícios de existência contigo.").withStyle(ChatFormatting.DARK_PURPLE));
        queue.addMessage(Component.literal("ADEUS").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        DelayedMessageHandler.addQueue(queue);
        ArcanaMod.LOGGER.debug("Primeiro sonho enviado.");
    }

    private static boolean generateRitualStructure(ServerLevel level, BlockPos playerPos, ServerPlayer player) {
        ArcanaMod.LOGGER.debug("Tentando gerar estrutura ritualística.");
        DiaryWorldData state = DiaryWorldData.get(level);
        if (state.hasSpawned()) {
            ArcanaMod.LOGGER.debug("Ritual já foi gerado anteriormente. Abortando geração.");
            return false;
        }
        BlockPos base = findValidPlainsSpot(level, playerPos);
        if (base == null) {
            ArcanaMod.LOGGER.debug("Nenhum local válido encontrado para geração do ritual.");
            return false;
        }
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, base);
        if (surface.getY() < MIN_Y || surface.getY() > MAX_Y) {
            ArcanaMod.LOGGER.debug("Local encontrado fora da faixa de altura permitida: {}", surface.getY());
            return false;
        }
        ArcanaMod.LOGGER.debug("Local válido encontrado em {}", surface);
        clearAbove(level, surface);
        ArcanaMod.LOGGER.debug("Área acima limpa.");
        buildRitual(level, surface, player);
        ArcanaMod.LOGGER.debug("Estrutura ritualística construída.");
        state.setSpawned(surface);
        ArcanaMod.LOGGER.debug("Estado salvo informando que o ritual já foi gerado.");
        return true;
    }

    private static BlockPos findValidPlainsSpot(ServerLevel level, BlockPos origin) {
        ArcanaMod.LOGGER.debug("Iniciando busca de terreno para ritual.");
        for (int r = SEARCH_STEP; r <= SEARCH_RADIUS; r += SEARCH_STEP)
            for (int x = -r; x <= r; x += SEARCH_STEP)
                for (int z = -r; z <= r; z += SEARCH_STEP) {
                    BlockPos candidate = origin.offset(x, 0, z);
                    if (isGoodSpot(level, candidate)) {
                        ArcanaMod.LOGGER.debug("Spot encontrado em {}", candidate);
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

        ArcanaMod.LOGGER.debug("Detalhes posicionados no círculo ritualístico.");
    }

    private static void placeChest(ServerLevel level, BlockPos base, ServerPlayer player) {
        level.setBlockAndUpdate(base, Blocks.CHEST.defaultBlockState());
        ArcanaMod.LOGGER.debug("Bau posicionado no centro do ritual em {}", base);
        addDiary(level, base, player);
    }

    private static void addDiary(Level level, BlockPos chestPos, ServerPlayer player) {
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            ItemStack diary = DiaryItem.createBoundDiary(player, DIARY_KALIASTRUS.get());
            chest.setItem(13, diary);
            ArcanaMod.LOGGER.debug("Diário vinculado inserido no baú na posição {}", chestPos);
        } else {
            ArcanaMod.LOGGER.debug("Falha ao inserir diário: baú não encontrado em {}", chestPos);
        }
    }
}