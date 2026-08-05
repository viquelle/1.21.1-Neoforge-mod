package com.viquelle.mikpik.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import net.minecraft.world.phys.Vec3;

public class PointLightHandle extends LightHandle<PointLightData> {
    private float radius;
    private float brightness;
    private int color;
    private boolean occlusion;
    private float inscatter;

    public PointLightHandle(float radius, float brightness, int color, boolean occlusion, boolean countsAsLight, float inscatter) {
        super(new PointLightData(), brightness, color, countsAsLight);
        this.radius = radius;
        this.brightness = brightness;
        this.color = color;
        this.occlusion = occlusion;
        this.affectDarkness = countsAsLight;
        this.inscatter = inscatter;

        data.setRadius(radius)
                    .setBrightness(brightness)
                    .setColor(color)
                    .setInscatteringStrength(inscatter)
                    .setOcclusionEnabled(occlusion);
    }

    public void setRadius(float radius) {
        this.radius = radius;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setRadius(radius));
        }
    }
    public float getRadius() {
        return this.radius;
    }

    @Override
    protected void updatePositionInData(Vec3 pos) {
        data.getPositionMutable().set(pos.x, pos.y, pos.z);
    }

    public void setInscattering(float value) {
        this.inscatter = value;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setInscatteringStrength(inscatter));
        }
    }
}