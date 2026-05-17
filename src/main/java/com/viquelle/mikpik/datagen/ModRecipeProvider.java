package com.viquelle.mikpik.datagen;

import com.viquelle.mikpik.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.FLOWER_CROWN.get())
                .define('I', Ingredient.of(ItemTags.SMALL_FLOWERS))
                .pattern("III")
                .pattern("I I")
                .pattern("III")
                .unlockedBy("has_small_flowers", has(ItemTags.FLOWERS))
                .save(recipeOutput);

    }
}
