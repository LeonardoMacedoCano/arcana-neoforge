package com.example.arcana.persistence;

import com.example.arcana.ArcanaMod;
import com.example.arcana.util.DelayedMessageHandler;
import com.example.arcana.util.DelayedMessageQueue;
import com.example.arcana.util.PlayerPersistentDataUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = ArcanaMod.MODID)
public class DiaryPersistenceHandler {

    private static final String PLAYER_BOUND_KEY = "arcana.diary_bound";
    private static final Map<UUID, Deque<Integer>> PLAYER_MESSAGE_ORDER = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static final Set<UUID> NEEDS_DIARY_RETURN = new HashSet<>();
    private static final Map<UUID, Long> SCHEDULED_RETURNS = new HashMap<>();
    private static final int RETURN_DELAY_TICKS = 100;

    private static final Map<UUID, TrackedDrop> TRACKED_DROPS = new HashMap<>();

    private record TrackedDrop(UUID owner, UUID entityUuid, long expireTick) {}

    private static final Component[] MESSAGES = new Component[]{
            Component.literal("Oi… você se afastou e me deixou sozinho.\nAinda tenho histórias para te contar.").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.literal("Ei… não importa onde estou, espero que não me perca agora.").withStyle(ChatFormatting.DARK_PURPLE),
            Component.literal("Estamos conectados de alguma forma.\nNão me deixe para trás.").withStyle(ChatFormatting.DARK_PURPLE),
            Component.literal("Mesmo que eu esteja longe, continuo com você.\nSempre voltarei.").withStyle(ChatFormatting.LIGHT_PURPLE),
            Component.literal("Não dá para fugir de mim tão fácil.\nAinda temos muito a compartilhar.").withStyle(ChatFormatting.DARK_PURPLE)
    };

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {

        if (!(event.getPlayer() instanceof ServerPlayer player))
            return;

        ItemEntity entity = event.getEntity();
        ItemStack stack = entity.getItem();

        if (!isDiary(stack))
            return;

        long expireTick = player.serverLevel()
                .getServer()
                .getTickCount() + RETURN_DELAY_TICKS;

        TRACKED_DROPS.put(
                entity.getUUID(),
                new TrackedDrop(
                        player.getUUID(),
                        entity.getUUID(),
                        expireTick
                )
        );

        log(player, "Jogador jogou o diário — item registrado para rastreamento");
    }

    @SubscribeEvent
    public static void onPlayerDeathDrops(net.neoforged.neoforge.event.entity.living.LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        event.getDrops().removeIf(drop -> {
            if (!isDiary(drop.getItem())) return false;
            NEEDS_DIARY_RETURN.add(player.getUUID());
            drop.discard();
            log(player, "Removendo diário na morte");
            return true;
        });
    }

    @SubscribeEvent
    public static void onPlayerRespawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!NEEDS_DIARY_RETURN.remove(player.getUUID())) return;

        log(player, "Agendando diário após respawn");

        player.serverLevel().getServer().execute(() -> {
            scheduleDiaryReturn(player);
            sendDiaryReminderMessage(player);
        });
    }

    @SubscribeEvent
    public static void onContainerClose(net.neoforged.neoforge.event.entity.player.PlayerContainerEvent.Close event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isDiaryBondActive(player)) return;

        event.getContainer().slots.forEach(slot -> {
            if (slot.hasItem() && isDiary(slot.getItem()) && !(slot.container instanceof Inventory)) {
                slot.set(ItemStack.EMPTY);
                slot.setChanged();
                scheduleDiaryReturn(player);
                sendDiaryReminderMessage(player);
                log(player, "Removendo diário do container fechado");
            }
        });
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long tick = event.getServer().getTickCount();

        processTrackedDrops(event, tick);
        processScheduledReturns(event, tick);
        checkDiaryBondForAllPlayers(event);
    }

    private static void processTrackedDrops(ServerTickEvent.Post event, long tick) {

        TRACKED_DROPS.values().removeIf(drop -> {

            var level = event.getServer().overworld();
            ItemEntity entity = (ItemEntity) level.getEntity(drop.entityUuid());

            if (entity == null) {
                ServerPlayer owner = event.getServer()
                        .getPlayerList()
                        .getPlayer(drop.owner());

                if (owner != null && !playerHasDiary(owner)) {
                    giveDiary(owner);
                    sendDiaryReminderMessage(owner);
                    log(owner, "Diário voltou porque foi destruído");
                }

                return true;
            }

            if (drop.expireTick() > tick)
                return false;

            entity.discard();

            ServerPlayer owner = event.getServer()
                    .getPlayerList()
                    .getPlayer(drop.owner());

            if (owner != null && !playerHasDiary(owner)) {
                giveDiary(owner);
                sendDiaryReminderMessage(owner);
                log(owner, "Diário voltou após tempo expirar");
            }

            return true;
        });
    }

    private static void processScheduledReturns(ServerTickEvent.Post event, long tick) {
        SCHEDULED_RETURNS.entrySet().removeIf(e -> {
            if (e.getValue() > tick) return false;

            ServerPlayer p = event.getServer()
                    .getPlayerList()
                    .getPlayer(e.getKey());

            if (p != null && !playerHasDiary(p)) {
                giveDiary(p);
            }

            return true;
        });
    }

    private static void checkDiaryBondForAllPlayers(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ensureDiaryBond(player);
        }
    }

    private static void ensureDiaryBond(ServerPlayer player) {
        if (!isDiaryBondActive(player) && playerHasDiary(player)) {
            PlayerPersistentDataUtil.setBoolean(player, PLAYER_BOUND_KEY, true);
            log(player, "Jogador foi vinculado ao diário ao possuir o item no inventário");
        }
    }

    public static boolean isDiaryBondActive(ServerPlayer player) {
        return PlayerPersistentDataUtil.getBoolean(player, PLAYER_BOUND_KEY);
    }

    private static boolean isDiary(ItemStack stack) {
        return stack.getItem() == ArcanaMod.DIARY_KALIASTRUS.get();
    }

    private static void giveDiary(ServerPlayer player) {
        player.getInventory().add(new ItemStack(ArcanaMod.DIARY_KALIASTRUS.get()));
        PlayerPersistentDataUtil.setBoolean(player, PLAYER_BOUND_KEY, true);
        log(player, "Jogador recebeu diário no inventário");
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

        int msgIndex = order.removeFirst();
        DelayedMessageQueue queue = new DelayedMessageQueue(player, 20);
        queue.addMessage(MESSAGES[msgIndex]);
        DelayedMessageHandler.addQueue(queue);

        log(player, "Mensagem de lembrança enviada");
    }

    private static Deque<Integer> generateMessageOrder() {
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4);
        Collections.shuffle(list, RANDOM);
        return new ArrayDeque<>(list);
    }

    private static void scheduleDiaryReturn(ServerPlayer player) {
        long targetTick = player.serverLevel().getServer().getTickCount() + RETURN_DELAY_TICKS;
        SCHEDULED_RETURNS.put(player.getUUID(), targetTick);
        log(player, "Diário agendado para retorno");
    }

    private static void log(Player player, String text) {
        ArcanaMod.LOGGER.debug("[Diary] {} {}", text, player.getName().getString());
    }
}
