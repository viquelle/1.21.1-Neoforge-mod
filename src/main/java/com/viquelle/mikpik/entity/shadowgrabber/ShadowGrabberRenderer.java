package com.viquelle.mikpik.entity.shadowgrabber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.entity.shadowgrabber.model.ShadowForearmModel;
import com.viquelle.mikpik.entity.shadowgrabber.model.ShadowHandModel;
import com.viquelle.mikpik.entity.shadowgrabber.model.ShadowPortalModel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ShadowGrabberRenderer extends EntityRenderer<ShadowGrabberEntity> {
    private static final ResourceLocation PORTAL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/shadow_grabber/portal.png");

    private static final ResourceLocation FOREARM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/shadow_grabber/forearm.png");

    private static final ResourceLocation HAND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/shadow_grabber/hand.png");

    private final ShadowPortalModel<ShadowGrabberEntity> portalModel;
    private final ShadowForearmModel<ShadowGrabberEntity> forearmModel;
    private final ShadowHandModel<ShadowGrabberEntity> handModel;

    public ShadowGrabberRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;

        this.portalModel = new ShadowPortalModel<>(context.bakeLayer(ShadowPortalModel.LAYER_LOCATION));
        this.forearmModel = new ShadowForearmModel<>(context.bakeLayer(ShadowForearmModel.LAYER_LOCATION));
        this.handModel = new ShadowHandModel<>(context.bakeLayer(ShadowHandModel.LAYER_LOCATION));
    }

    @Override
    public void render(
            ShadowGrabberEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();

        float age = entity.tickCount + partialTick;

        renderPortalRaw(entity, poseStack, buffer, age);
        renderForearmRaw(entity, poseStack, buffer, age);
        renderHandRaw(entity, poseStack, buffer, age);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void renderPortalRaw(
            ShadowGrabberEntity entity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            float age
    ) {
        poseStack.pushPose();

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(PORTAL_TEXTURE));

        portalModel.setupAnim(entity, 0.0f, 0.0f, age, 0.0f, 0.0f);
        portalModel.renderToBuffer(
                poseStack,
                vc,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }

    private void renderForearmRaw(
            ShadowGrabberEntity entity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            float age
    ) {
        poseStack.pushPose();

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(FOREARM_TEXTURE));

        forearmModel.setupAnim(entity, 0.0f, 0.0f, age, 0.0f, 0.0f);
        forearmModel.renderToBuffer(
                poseStack,
                vc,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }

    private void renderHandRaw(
            ShadowGrabberEntity entity,
            PoseStack poseStack,
            MultiBufferSource buffer,
            float age
    ) {
        poseStack.pushPose();

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(HAND_TEXTURE));

        handModel.setupAnim(entity, 0.0f, 0.0f, age, 0.0f, 0.0f);
        handModel.renderToBuffer(
                poseStack,
                vc,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ShadowGrabberEntity entity) {
        return HAND_TEXTURE;
    }
}