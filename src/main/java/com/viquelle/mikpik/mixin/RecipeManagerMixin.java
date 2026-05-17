package com.viquelle.mikpik.mixin;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Inject(
            method = "apply",
            at = @At("HEAD")
    )
    private void mikpik$removeTorchRecipes(
            Map<ResourceLocation, JsonElement> prepared,
            ResourceManager resourceManager,
            ProfilerFiller profilerFiller,
            CallbackInfo ci
    ) {
        prepared.remove(ResourceLocation.fromNamespaceAndPath("minecraft","torch"));
        prepared.remove(ResourceLocation.fromNamespaceAndPath("minecraft","soul_torch"));
    }
}
