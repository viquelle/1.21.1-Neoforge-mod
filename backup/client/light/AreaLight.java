package com.viquelle.mikpik.light;

import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.impl.client.render.light.VoxelShadowGrid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AreaLight extends AbstractLight<AreaLightData> {
    private Vector3f pos = new Vector3f();
    private Quaternionf orientation = new Quaternionf();
    private final float angle;
    private final float distance;
    private final boolean occlusion;

    public AreaLight(Builder builder) {
        super(builder);
        this.angle = builder.angle;
        this.distance = builder.distance;
        this.occlusion = builder.occlusion;
    }

    @Override
    public void tick(float deltaTick, float partialTick) {
        Player p = getPlayer();
        if (p == null || handle == null) return;

        super.tick(deltaTick, partialTick);

        syncPositionAndRotation(p, partialTick);
    }

    private void syncPositionAndRotation(Player p, float pt) {
        float yaw = (float) Math.toRadians(p.getYRot());
        float pitch = (float) Math.toRadians(p.getXRot());
        float roll = 0.0f;
        this.orientation.identity().rotationXYZ(-pitch,yaw,roll);

        Vec3 eyePos = p.getEyePosition(pt);
        this.pos.set((float)eyePos.x, (float)eyePos.y, (float)eyePos.z);

        AreaLightData data = handle.getLightData();
        data.getPosition().set(pos);
        data.getOrientation().set(orientation);
    }

    @Override
    public void register() {
        AreaLightData lightData = new AreaLightData();
        lightData.setAngle(angle)
                .setDistance(distance)
                .setBrightness(currentBrightness)
                .setColor(color)
                .setSize(0.01, 0.01)
                .setOcclusionEnabled(occlusion);

        VeilRenderSystem.renderThreadExecutor().execute(() -> {
            handle = VeilRenderSystem.renderer()
                    .getLightRenderer()
                    .addLight(lightData);
        });
    }

    public static class Builder extends AbstractLight.Builder<AreaLight.Builder> {
        private float angle = 0.6f;
        private float distance = 20f;
        private boolean occlusion = false;
        public Builder(Player player) { super(player); }

        public Builder setAngle(float angle) { this.angle = angle; return this; }
        public Builder setDistance(float distance) { this.distance = distance; return this; }
        public Builder setOcclusion(boolean occlusion) { this.occlusion = occlusion; return this; }

        @Override
        public AreaLight build() { return new AreaLight(this); }

    }
}