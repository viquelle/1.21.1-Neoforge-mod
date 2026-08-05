package com.viquelle.mikpik.block.meateffigy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.blockentity.MeatEffigyBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class MeatEffigyBlockRenderer implements BlockEntityRenderer<MeatEffigyBlockEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    MikpikMod.MODID,
                    "textures/entity/meat_effigy.png"
            );

    private final MeatEffigyModel model;

    public MeatEffigyBlockRenderer(BlockEntityRendererProvider.Context ctx) {
        model = new MeatEffigyModel(
                ctx.bakeLayer(MeatEffigyModel.LAYER_LOCATION)
        );
    }

    @Override
    public void render(
            MeatEffigyBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {

        poseStack.pushPose();

        poseStack.translate(0.5, 0.0, 0.5);

        int facing =
                blockEntity.getBlockState()
                        .getValue(MeatEffigyBlock.ROTATION);

        poseStack.mulPose(
                Axis.YP.rotationDegrees(180f -facing * 22.5F)
        );

        model.renderToBuffer(
                poseStack,
                buffer.getBuffer(model.renderType(TEXTURE)),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                -1
        );

        poseStack.popPose();
    }
}