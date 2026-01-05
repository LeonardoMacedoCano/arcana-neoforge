package com.example.arcana.systems.loot;

import com.example.arcana.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class DesertPyramidLootSystem {
    private static final ResourceLocation DESERT_PYRAMID = ResourceLocation.withDefaultNamespace("chests/desert_pyramid");

    public static boolean isDesertPyramid(ResourceLocation id) {
        return id.equals(DESERT_PYRAMID);
    }

    public static LootPool createStrangeTotemPool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.15f))
                .add(LootItem.lootTableItem(ModItems.STRANGE_TOTEM.get()).setWeight(1))
                .build();
    }

}
