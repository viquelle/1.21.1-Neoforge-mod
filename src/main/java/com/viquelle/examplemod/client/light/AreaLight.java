package com.viquelle.examplemod.client.light;

import com.mojang.blaze3d.systems.RenderSystem;
import com.viquelle.examplemod.ExampleMod;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.AreaLightData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3dc;

public class AreaLight extends AbstractLight<AreaLightData>{
    protected Vec3 pos; // EYEPOS
    protected Quaternionf orientation; // PITCH AND YAW EYES
    protected float angle;

    public AreaLight(Builder builder) {
        super(builder);
        this.pos = builder.pos;
        this.orientation = builder.orientation;
        this.angle = builder.angle;
    }

    public void syncWithObj(Player p, float pt) {
        Quaternionf orientation = new Quaternionf().rotateXYZ(
                (float) -Math.toRadians(p.getXRot()),
                (float) Math.toRadians(p.getYRot()),
                0.0f
        );
        Vec3 eyePos = p.getEyePosition(pt);
        this.orientation = orientation;
        this.pos = eyePos;
        handle.getLightData().getOrientation().set(orientation);
        handle.getLightData().getPosition().set(
                pos.x,
                pos.y,
                pos.z
        );
    }

    public static class Builder extends AbstractLight.Builder<AreaLight.Builder> {
        private Vec3 pos;
        private Quaternionf orientation;
        private float angle;

        public Builder setPosition(Vec3 pos) {
            this.pos = pos;
            return this;
        }

        public Builder setPosition(double x, double y, double z) {
            this.pos = new Vec3(x,y,z);
            return this;
        }

        public Builder setOrientation(Quaternionf orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder setAngle(float angle) {
            this.angle = angle;
            return this;
        }

        @Override
        public AreaLight build() {
            return new AreaLight(this);
        }


    }

    @Override
    public void register() {
        VeilRenderSystem.renderThreadExecutor().execute(() -> {
            AreaLightData light = new AreaLightData();
            light.setBrightness(brightness)
                    .setColor(color)
                    .setDistance(32f)
                    .setAngle(0.6f)
                    .setSize(0.1f,0.1f);

            handle = VeilRenderSystem.renderer()
                    .getLightRenderer()
                    .addLight(light);

            registered = true;
        });

    }

    @Override
    public void tick(float partialTick) {
        Player p = getPlayer();
        if (!registered || p == null || handle == null) return;
        super.tick(partialTick, handle);
        if (brightness == 0f) return;
        syncWithObj(p, partialTick);
    }


}
