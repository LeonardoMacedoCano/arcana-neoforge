package com.arcana.mod.datagen;

import com.arcana.mod.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ArcanaRecipeProvider extends RecipeProvider {

    public ArcanaRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.DIORITE_PEDESTAL.get())
                .define('D', Items.DIORITE)
                .define('S', Items.DIORITE_SLAB)
                .pattern("DSD")
                .pattern(" D ")
                .pattern("SSS")
                .unlockedBy("has_diorite", has(Items.DIORITE))
                .save(output);
    }
}
