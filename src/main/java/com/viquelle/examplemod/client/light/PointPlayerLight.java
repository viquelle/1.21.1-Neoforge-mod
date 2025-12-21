package com.viquelle.examplemod.client.light;

import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import foundry.veil.api.client.render.light.renderer.LightRenderHandle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class PointPlayerLight extends AbstractPlayerLight{

    private LightRenderHandle<PointLightData> handle;
    private PointLightData lightData;

    @Override
    public PointPlayerLight create() {
        lightData = new PointLightData();
        lightData.setBrightness(1.0f)
                .setRadius(6f)
                .setColor(color);

        handle = VeilRenderSystem.renderer()
                .getLightRenderer()
                .addLight(lightData);

        active = true;
        return this;
    }

    @Override
    public void destroy() {
        if (handle != null) {
            handle.free();
            handle = null;
        }
        active = false;
    }

    @Override
    public void tick(LocalPlayer player, float partialTick) {
        if (!isActive()) return;

        Vec3 pos = player.getEyePosition(partialTick);
        lightData.setPosition((float)pos.x,(float)pos.y,(float)pos.z);
    }
}
