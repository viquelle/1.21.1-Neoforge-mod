package com.viquelle.mikpik;

import com.viquelle.mikpik.sanity.ClientSanityData;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.platform.VeilEventPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class ShaderHandler {
    private static final ResourceLocation PIPELINE =
            ResourceLocation.fromNamespaceAndPath("mikpik", "sanity_pipeline");

    public static void enable() {

        var renderer = VeilRenderSystem.renderer();

        if (renderer == null) {
            return;
        }

        PostProcessingManager manager = renderer.getPostProcessingManager();

        if (manager != null && !manager.isActive(PIPELINE)) {
            manager.add(PIPELINE);
            System.out.println("Pipeline enabled");
        }
    }

    public static void init() {

        VeilEventPlatform.INSTANCE.preVeilPostProcessing((pipelineName, pipeline, context) -> {

            if (!PIPELINE.equals(pipelineName)) {
                return;
            }

            var sanityUniform = pipeline.getUniform("u_sanity");
            if (sanityUniform != null) {
                sanityUniform.setFloat(ClientSanityData.get() / 100f);
            }

            var timeUniform = pipeline.getUniform("u_time");
            if (timeUniform != null) {

                Minecraft mc = Minecraft.getInstance();

                if (mc.level != null) {
                    float time = mc.level.getGameTime();
                    timeUniform.setFloat(time);
                }
            }
        });
    }

}