package com.viquelle.mikpik.mixin;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.MikpikModClient;
import com.viquelle.mikpik.coloredlights.ColoredLightScanner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public class LevelChunkMixin {

    @Shadow
    @Final
    private Level level;

    @Inject(method = "setBlockState",at = @At("TAIL"))
    private void mikpik$onBlockChanged(
            BlockPos pos,
            BlockState newState,
            boolean moved,
            CallbackInfoReturnable<BlockState> cir
    ) {

        if (!level.isClientSide) return;
        BlockState oldState = cir.getReturnValue();
        MikpikMod.LOGGER.info("{} {}", oldState, newState);
        if (oldState == null) {
            return;
        }

        if (oldState != newState) {
            ColoredLightScanner.onBlockChanged(pos, oldState, newState);
        }
    }
}