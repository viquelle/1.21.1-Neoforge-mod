package com.viquelle.mikpik.entity.hand;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.viquelle.mikpik.MikpikMod;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class HandRenderer extends EntityRenderer<HandEntity> {
    private record TextureData(ResourceLocation texture, boolean mirror) {
    }

    private static final ResourceLocation FRONT = ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/hand/front.png");
    private static final ResourceLocation BACK = ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/hand/back.png");
    private static final ResourceLocation SIDE1 = ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/hand/side1.png");
    private static final ResourceLocation SIDE2 = ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/hand/side2.png");
    private static final ResourceLocation SIDE3 = ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "textures/entity/hand/side3.png");
    private static final ResourceLocation BLACK = ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID,"textures/black.png");

    public HandRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HandEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0f, entity.getBbHeight() / 2.0f, 0.0f);

        Vec3 attachLocal = entity.getOrigin().subtract(entity.getPosition(partialTick)).add(0,-0.5,0);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        renderForearm(poseStack, buffer, attachLocal, Vec3.ZERO, camera.getPosition().subtract(entity.position()), packedLight);

        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        TextureData texture = getTexture(entity, camera, partialTick);
        renderQuad(
                poseStack,
                buffer.getBuffer(RenderType.entityCutout(texture.texture)),
                packedLight,
                texture.mirror
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HandEntity entity) {
        return FRONT;
    }

    private TextureData getTexture(HandEntity entity, Camera cam, float partialTick) {
        if (cam == null) return new TextureData(FRONT, false);

        Vec3 handPos = entity.getPosition(partialTick);
        Vec3 camPos = cam.getPosition();

        Vec3 toCamera = camPos.subtract(handPos).normalize();

        Vec3 entityForward = new Vec3(
                -Math.sin(Math.toRadians(entity.getYRot())),
                0,
                Math.cos(Math.toRadians(entity.getYRot()))
        ).normalize();

        double dot = entityForward.dot(toCamera);
        double cross = entityForward.x * toCamera.z - entityForward.z * toCamera.x;

        float angle = (float)Math.toDegrees(Math.atan2(cross, dot));
        angle = (angle + 360f) % 360f;

        int dir = Math.round(angle / 45f) & 7;

        return switch (dir) {
            case 0 -> new TextureData(FRONT, false);
            case 1 -> new TextureData(SIDE1, false);
            case 2 -> new TextureData(SIDE2, false);
            case 3 -> new TextureData(SIDE3, false);
            case 4 -> new TextureData(BACK, false);
            case 5 -> new TextureData(SIDE3, true);
            case 6 -> new TextureData(SIDE2, true);
            default -> new TextureData(SIDE1, true);
        };
    }

    private void renderQuad(PoseStack poseStack, VertexConsumer consumer, int light, boolean mirror) {
        Matrix4f pose = poseStack.last().pose();
        float w = 0.5f;
        float h = 0.5F;
        int v = mirror ? 1 : 0;

        consumer.addVertex(pose, -w, -h, 0).setColor(255, 255, 255, 255).setUv(0 + v, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, w, -h, 0).setColor(255, 255, 255, 255).setUv(1 - v, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, w, h, 0).setColor(255, 255, 255, 255).setUv(1 - v, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
        consumer.addVertex(pose, -w, h, 0).setColor(255, 255, 255, 255).setUv(0 + v, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0, 0, 1);
    }

    private void renderForearm(PoseStack poseStack, MultiBufferSource buffer, Vec3 attach, Vec3 hand, Vec3 cameraLocal, int light) {
        Matrix4f pose = poseStack.last().pose();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(BLACK));

        Vec3 dir = attach.subtract(hand);
        double dist = dir.length();
        if (dist < 0.0001) return;

        dir = dir.normalize();

        Vec3 toCamera = cameraLocal.subtract(hand).normalize();
        Vec3 side = dir.cross(toCamera);

        if (side.lengthSqr() < 0.0001) {
            side = dir.cross(new Vec3(0, 1, 0));
        }

        float width = (float) (0.04 + dist * 0.02);
        side = side.normalize().scale(width);

        Vec3 normal = side.cross(dir).normalize();
        float nx = (float) normal.x;
        float ny = (float) normal.y;
        float nz = (float) normal.z;

        Vec3 p1 = hand.add(side);
        Vec3 p2 = hand.subtract(side);
        Vec3 p3 = attach.subtract(side);
        Vec3 p4 = attach.add(side);

        addForearmVertex(consumer, pose, p1, light, 0, 1, nx, ny, nz);
        addForearmVertex(consumer, pose, p2, light, 1, 1, nx, ny, nz);
        addForearmVertex(consumer, pose, p3, light, 1, 0, nx, ny, nz);
        addForearmVertex(consumer, pose, p4, light, 0, 0, nx, ny, nz);
    }

    private void addForearmVertex(VertexConsumer consumer, Matrix4f pose, Vec3 p, int light, int u, int v, float nx, float ny, float nz) {
        consumer.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setColor(255, 255, 255, 255)
                .setLight(light)
                .setNormal(nx, ny, nz);
    }
}