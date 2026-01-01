package com.example.arcana.systems.diary;

import com.example.arcana.item.DiaryItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class DiaryManager {

    public static boolean hasActiveDiaryBond(ServerPlayer player) {
        return DiaryPersistenceHandler.isDiaryBondActive(player);
    }

    public static boolean isDiary(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.getItem() instanceof DiaryItem;
    }

    public static boolean isDiaryOwnedBy(ItemStack stack, ServerPlayer player) {
        if (!isDiary(stack)) {
            return false;
        }

        UUID boundPlayer = DiaryItem.getBoundPlayer(stack);
        return boundPlayer != null && boundPlayer.equals(player.getUUID());
    }

    public static boolean hasPlayerDiaryInInventory(ServerPlayer player) {
        Inventory inv = player.getInventory();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (isDiaryOwnedBy(stack, player)) {
                return true;
            }
        }

        return false;
    }
}