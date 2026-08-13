package com.viquelle.mikpik.datagen;

import com.viquelle.mikpik.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
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

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.HEART.get())
                .define('A', Items.LEATHER)
                .define('B', Items.STRING)
                .define('M', Items.MAGMA_BLOCK)
                .define('C', Items.HAY_BLOCK)
                .pattern("ABA")
                .pattern("CMC")
                .pattern("ABA")
                .unlockedBy("has_magma_block", has(Items.MAGMA_BLOCK))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ModItems.LIFE_INJECTOR.get())
                .requires(Items.SUGAR)
                .requires(Items.FERMENTED_SPIDER_EYE)
                .requires(Items.DANDELION)
                .unlockedBy("has_sugar", has(Items.SUGAR))
                .save(recipeOutput);
    }
}
