package com.viquelle.mikpik.entity.watcher;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import com.viquelle.mikpik.MikpikMod;

public class WatcherRenderer extends EntityRenderer<WatcherEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            MikpikMod.MODID, "textures/entity/watcher_eyes.png"
    );

    // Размер quad в блоках
    private static final float SIZE = 0.5F;

    // UV координаты для текстуры 64x16 (4 кадра по 16x16 в ряд)
    private static final float[][] FRAME_UVS = {
            {0.0F, 0.0F, 0.25F, 1.0F},    // Кадр 0: открытые
            {0.25F, 0.0F, 0.5F, 1.0F},    // Кадр 1: закрываются
            {0.5F, 0.0F, 0.75F, 1.0F},    // Кадр 2: закрыты
            {0.75F, 0.0F, 1.0F, 1.0F}     // Кадр 3: открываются
    };

    // Максимальная яркость (свечение в темноте)
    private static final int FULL_BRIGHT = 0xF000F0;

    public WatcherRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F; // Нет тени под entity
        this.shadowStrength = 0.0F;
    }

    @Override
    public void render(WatcherEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        int frame = entity.getBlinkFrame();
        float[] uv = FRAME_UVS[frame];

        poseStack.pushPose();
        poseStack.translate(0.0, 0.25,0.0);
        // Billboard: поворачиваем quad к камере
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        float halfSize = SIZE / 2;

        // Рисуем quad из 2 треугольников
        // Левый-верхний
        consumer.addVertex(pose, -halfSize, -halfSize, 0)
                .setColor(255, 255, 255, 255)
                .setUv(uv[0], uv[3])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, 1);

        // Левый-нижний
        consumer.addVertex(pose, -halfSize, halfSize, 0)
                .setColor(255, 255, 255, 255)
                .setUv(uv[0], uv[1])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, 1);

        // Правый-нижний
        consumer.addVertex(pose, halfSize, halfSize, 0)
                .setColor(255, 255, 255, 255)
                .setUv(uv[2], uv[1])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, 1);

        // Правый-верхний
        consumer.addVertex(pose, halfSize, -halfSize, 0)
                .setColor(255, 255, 255, 255)
                .setUv(uv[2], uv[3])
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(FULL_BRIGHT)
                .setNormal(0, 0, 1);

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(WatcherEntity entity) {
        return TEXTURE;
    }

    @Override
    public boolean shouldRender(WatcherEntity entity, net.minecraft.client.renderer.culling.Frustum frustum,
                                double camX, double camY, double camZ) {
        return true;
    }
}