package com.viquelle.mikpik.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import net.minecraft.world.phys.Vec3;

public class PointLightHandle extends LightHandle<PointLightData> {
    public PointLightHandle(float radius, float brightness, int color, boolean occlusion) {
        super(new PointLightData());
        data.setRadius(radius)
                .setBrightness(brightness)
                .setColor(color)
                .setOcclusionEnabled(occlusion);
    }

    public void setRadius(float radius) {
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setRadius(radius));
        }
    }

    @Override
    protected void updatePositionInData(Vec3 pos) {
        data.setPosition((float) pos.x, (float) pos.y, (float) pos.z);
    }
}