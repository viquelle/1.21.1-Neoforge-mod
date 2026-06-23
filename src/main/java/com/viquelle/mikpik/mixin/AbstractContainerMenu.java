package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.ghost.GhostManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.world.inventory.AbstractContainerMenu.class)
public class AbstractContainerMenu {
    @Inject(
            method = "clicked",
            at = @At("HEAD"),
            cancellable = true
    )
    private void ghostNoInventory(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        if (GhostManager.isGhost(player)) {
            ci.cancel();
        }
    }
}
