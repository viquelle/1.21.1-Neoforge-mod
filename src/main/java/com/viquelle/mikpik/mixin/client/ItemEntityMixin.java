package com.viquelle.mikpik.mixin.client;

import com.viquelle.mikpik.client.ClientHeartManager;
import com.viquelle.mikpik.item.items.HeartItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void mikpik$heartTick(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        if (entity.level().isClientSide) {
            ClientHeartManager.tickItemEntity(entity);
        }
    }
}
