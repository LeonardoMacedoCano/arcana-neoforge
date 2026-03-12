package com.arcana.mod.datagen;

import com.arcana.mod.registry.ModEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ArcanaLootTableProvider extends LootTableProvider {

    public ArcanaLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Set.of(), List.of(
                new SubProviderEntry(ArcanaEntityLoot::new, LootContextParamSets.ENTITY)
        ), lookupProvider);
    }

    private static class ArcanaEntityLoot extends EntityLootSubProvider {

        protected ArcanaEntityLoot(HolderLookup.Provider lookupProvider) {
            super(FeatureFlags.REGISTRY.allFlags(), lookupProvider);
        }

        @Override
        public void generate() {
            add(ModEntities.THE_FOOL.get(), LootTable.lootTable());
        }

        @Override
        protected @NotNull Stream<EntityType<?>> getKnownEntityTypes() {
            return Stream.of(ModEntities.THE_FOOL.get());
        }
    }
}
