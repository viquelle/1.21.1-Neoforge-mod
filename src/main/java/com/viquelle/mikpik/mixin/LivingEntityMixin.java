package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.ghost.GhostManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// I do not understand how it works but it doesnt work if i dont patch both together.
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(
            method = "pushEntities",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ghostNoPushEntities(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self.level().isClientSide()) return;

        if (self instanceof Player player && GhostManager.isGhost(player)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "doPush",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ghostNoPush(Entity entity, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self.level().isClientSide()) return;

        if (entity instanceof Player player && GhostManager.isGhost(player)) {
            ci.cancel();
        }
    }
}