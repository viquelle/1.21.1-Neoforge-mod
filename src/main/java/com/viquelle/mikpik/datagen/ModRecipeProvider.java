package com.viquelle.mikpik.datagen;

import com.viquelle.mikpik.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

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
