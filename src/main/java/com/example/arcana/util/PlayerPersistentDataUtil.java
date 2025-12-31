package com.example.arcana.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

public class PlayerPersistentDataUtil {

    private static CompoundTag getPersistent(ServerPlayer player) {
        var data = player.getPersistentData();

        if (!data.contains(ServerPlayer.PERSISTED_NBT_TAG)) {
            CompoundTag persisted = new CompoundTag();
            data.put(ServerPlayer.PERSISTED_NBT_TAG, persisted);
            return persisted;
        }

        return data.getCompound(ServerPlayer.PERSISTED_NBT_TAG);
    }

    public static boolean getBoolean(ServerPlayer player, String key) {
        return getPersistent(player).getBoolean(key);
    }

    public static void setBoolean(ServerPlayer player, String key, boolean value) {
        var data = player.getPersistentData();
        var persistent = getPersistent(player);
        persistent.putBoolean(key, value);
        data.put(ServerPlayer.PERSISTED_NBT_TAG, persistent);
    }
}
