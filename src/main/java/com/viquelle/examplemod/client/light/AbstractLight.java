package com.viquelle.examplemod.client.light;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.data.LightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

public abstract class AbstractLight<T extends LightData> implements IAbstractLight{
    public int color;
    public float brightness;
    private final Player player;
    protected boolean registered = false; // need for known inited in Veil or doesn't
    protected LightRenderHandle<T> handle = null;


    protected AbstractLight(Builder builder) {
        this.color = builder.color;
        this.brightness = builder.brightness;
        this.player = builder.player;
    }

    protected static class Builder<B extends Builder<B>> {
        private int color = 0xFFFFFF;
        private float brightness = 1.0f;
        private Player player;

        @SuppressWarnings("unchecked")
        public B setColor(int color) {
            this.color = color;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B setColor(int R, int G, int B) {
            this.color =
                    Mth.clamp(R, 0, 255) << 16 |
                    Mth.clamp(G, 0, 255) << 8 |
                    Mth.clamp(B, 0, 255);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B setBrightness(float brightness) {
            this.brightness = brightness;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B setPlayer(Player player) {
            this.player = player;
            return (B) this;
        }

        @ApiStatus.OverrideOnly
        public AbstractLight<?> build() {return null;}

    }

    private void syncBrightness(LightRenderHandle<?> handle) {
        handle.getLightData().setBrightness(brightness);
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    private void syncColor(LightRenderHandle<?> handle){
        handle.getLightData().setColor(color);
    }

    public void setColor(int color) {
        this.color = color;
    }
    public void setColor(int R, int G, int B) {
        this.color =
                Mth.clamp(R, 0, 255) << 16 |
                Mth.clamp(G, 0, 255) << 8 |
                Mth.clamp(B, 0, 255);
    }
    protected void tick(float pT, LightRenderHandle<?> handle) {
        if (!registered || handle == null) return;

        syncBrightness(handle);
        //syncColor(handle);
    }

    public Player getPlayer() {
        return this.player;
    }

    public void unregister() {
        VeilRenderSystem.renderThreadExecutor().execute(() -> {
            if (handle == null) return;
            handle.free();
            handle = null;
            registered = false;
        });
    }


}