package com.example.arcana.systems.dreams.types;

import com.example.arcana.systems.dreams.DreamMessagesUtil;
import com.example.arcana.systems.dreams.DreamType;
import com.example.arcana.systems.diary.DiaryWorldData;
import com.example.arcana.util.common.ArcanaLog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

import static com.example.arcana.systems.diary.DiaryPersistenceHandler.isDiaryBondActive;

public class DiaryGuideDream implements DreamType {
    private static final String MODULE = "DIARY_GUIDE_DREAM";
    private static final String FILE = "diary_guide_dream";

    private static final Map<UUID, List<Integer>> ORDER = new HashMap<>();
    private static final Random RNG = new Random();

    @Override
    public String getName() {
        return "Diary Guide Dream";
    }

    @Override
    public boolean shouldTrigger(ServerPlayer player, ServerLevel level) {
        ArcanaLog.debug(MODULE, "Processing diary trigger condition for {}", player.getName().getString());
        DiaryWorldData data = DiaryWorldData.get(level);

        if (!data.hasSpawned()) {
            ArcanaLog.debug(MODULE, "Diary structure has not spawned yet");
            return false;
        }

        if (isDiaryBondActive(player)) {
            ArcanaLog.debug(MODULE, "Player {} already owns the diary", player.getName().getString());
            return false;
        }

        boolean ok = data.getStructurePos() != null;
        ArcanaLog.debug(MODULE, "Can diary dream trigger? {}", ok);
        return ok;
    }

    @Override
    public void runDream(ServerPlayer player, ServerLevel level) {
        ArcanaLog.debug(MODULE, "Processing diary dream for {}", player.getName().getString());
        var msgs = DreamMessagesUtil.load(FILE, player);

        BlockPos pos = DiaryWorldData.get(level).getStructurePos();
        String coords = "%d %d %d".formatted(pos.getX(), pos.getY(), pos.getZ());

        boolean first = !ORDER.containsKey(player.getUUID());
        String msg;

        if (first) {
            msg = msgs.first().formatted(coords);
            ORDER.put(player.getUUID(), shuffled(msgs.rest().size()));
        } else {
            List<Integer> order = ORDER.get(player.getUUID());
            if (order.isEmpty()) order.addAll(shuffled(msgs.rest().size()));

            msg = msgs.rest().get(order.removeFirst()).formatted(coords);
        }

        player.sendSystemMessage(
                Component.literal(msg)
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
        );

        ArcanaLog.debug(MODULE, "Diary dream successfully delivered to {}", player.getName().getString());
    }

    private List<Integer> shuffled(int size) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < size; i++) list.add(i);
        Collections.shuffle(list, RNG);
        return list;
    }
}
