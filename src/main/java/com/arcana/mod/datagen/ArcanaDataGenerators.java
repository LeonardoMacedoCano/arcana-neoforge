package com.arcana.mod.datagen;

import com.arcana.mod.ArcanaMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class ArcanaDataGenerators {

    public static void onGatherData(GatherDataEvent event) {
        PackOutput output = event.getGenerator().getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        event.getGenerator().addProvider(
                event.includeServer(),
                new ArcanaRecipeProvider(output, lookupProvider)
        );

        event.getGenerator().addProvider(
                event.includeServer(),
                new ArcanaLootTableProvider(output, lookupProvider)
        );
    }
}
