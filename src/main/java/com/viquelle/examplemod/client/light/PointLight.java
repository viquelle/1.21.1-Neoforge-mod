package com.viquelle.examplemod.client.light;

import com.mojang.blaze3d.systems.RenderSystem;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3dc;

public class PointLight extends AbstractLight<PointLightData>{
    private Vec3 pos;
    private float radius;

    protected PointLight(Builder builder) {
        super(builder);
        this.pos = builder.pos;
        this.radius = builder.radius;
    }

    public static class Builder extends AbstractLight.Builder<PointLight.Builder> {
        private Vec3 pos;
        private float radius = 6f;
        public Builder setPosition(Vec3 pos) {
            this.pos = pos;
            return this;
        }

        public Builder setPosition(double x, double y, double z) {
            this.pos = new Vec3(x,y,z);
            return this;
        }

        public Builder setRadius(float radius) {
            this.radius = radius;
            return this;
        }

        @Override
        public PointLight build() {
            return new PointLight(this);
        }

    }
    @Override
    public void tick(float partialTick) {
        Player p = getPlayer();
        if (!registered || p == null || handle == null) return;
        super.tick(partialTick, handle);
        syncWithObj(p, partialTick);
    }

    public void syncWithObj(Player p, float pt) {
        this.pos = p.getEyePosition(pt);
        handle.getLightData().setPosition(
                pos.x,
                pos.y,
                pos.z
        );
    }

    @Override
    public void register() {
        VeilRenderSystem.renderThreadExecutor().execute(() -> {
            PointLightData light = new PointLightData();
            light.setBrightness(brightness)
                    .setColor(color)
                    .setRadius(radius);

            handle = VeilRenderSystem.renderer()
                    .getLightRenderer()
                    .addLight(light);

            registered = true;
        });

    }
}

