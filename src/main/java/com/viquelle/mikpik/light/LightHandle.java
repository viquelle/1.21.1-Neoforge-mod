package com.viquelle.mikpik.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.world.phys.Vec3;

public abstract class LightHandle<T extends LightData> {
    protected LightRenderHandle<T> handle;
    protected final T data;
    protected float brightness;
    protected int color;
    protected Vec3 position = Vec3.ZERO;

    public LightHandle(T data) {
        this.data = data;
        data.setColor(color).setBrightness(brightness);
    }

    public void setPosition(Vec3 pos) {
        this.position = pos;
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> updatePositionInData(pos));
        }
    }

    protected abstract void updatePositionInData(Vec3 pos);

    public void setBrightness(float brightness) {
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setBrightness(brightness));
        }
    }

    public void setColor(int color) {
        if (handle != null) {
            VeilRenderSystem.renderThreadExecutor().execute(() -> data.setColor(color));
        }
    }

    public void register() {
        VeilRenderSystem.renderThreadExecutor().execute(() -> {
            handle = VeilRenderSystem.renderer().getLightRenderer().addLight(data);
            // после регистрации применяем текущую позицию
            updatePositionInData(position);
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