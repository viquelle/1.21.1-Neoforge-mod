package com.viquelle.mikpik.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.world.phys.Vec3;

public abstract class LightHandle<T extends LightData> {
    protected volatile LightRenderHandle<T> handle;
    protected final T data;
    protected float brightness;
    protected int color;
    protected boolean countsAsLight;
    protected Vec3 position = Vec3.ZERO;

    public LightHandle(T data, float brightness, int color, boolean countsAsLight) {
        this.data = data;
        this.brightness = brightness;
        this.color = color;
        this.countsAsLight = countsAsLight;
        data.setColor(color).setBrightness(brightness);
    }

    public void setPosition(Vec3 pos) {
        this.position = pos;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> updatePositionInData(pos));
        }
    }

    public void setPosition(float x, float y, float z) {
        setPosition(new Vec3(x,y,z));
    }

    public Vec3 getPosition() {
        return position;
    }

    protected abstract void updatePositionInData(Vec3 pos);

    public void setBrightness(float brightness) {
        this.brightness = brightness;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setBrightness(brightness));
        }
    }
    public float getBrightness() {
        return brightness;
    }

    public void setColor(int color) {
        this.color = color;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setColor(color));
        }
    }

    public void register() {
        VeilRenderSystem.renderThreadExecutor().execute(() -> {
            handle = VeilRenderSystem.renderer().getLightRenderer().addLight(data);
        });
    }

    public void unregister() {
        VeilRenderSystem.renderThreadExecutor().execute(() -> {
            if (handle != null) {
                handle.free();
                handle = null;
            }
        });
    }
}