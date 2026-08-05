package com.viquelle.mikpik.block.meateffigy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.viquelle.mikpik.MikpikMod;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class MeatEffigyModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "meat_effigy"), "main");
    private final ModelPart root;
    private final ModelPart body_group;

    public MeatEffigyModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root.getChild("root");
        this.body_group = this.root.getChild("body_group");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 36.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 3.1416F, 0.0F, -3.1416F));

        PartDefinition body_group = root.addOrReplaceChild("body_group", CubeListBuilder.create().texOffs(8, 0).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(28, 0).addBox(-2.0F, -13.0F, 0.5F, 4.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, -4.0F));

        PartDefinition head_r1 = body_group.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(8, 16).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.0F, 3.0F, -0.3054F, 0.0F, 0.0F));

        PartDefinition r_arm_r1 = body_group.addOrReplaceChild("r_arm_r1", CubeListBuilder.create().texOffs(8, 28).addBox(0.0F, -8.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 5.5F, 2.0F, 0.0F, 0.0F, 0.3054F));

        PartDefinition l_arm_r1 = body_group.addOrReplaceChild("l_arm_r1", CubeListBuilder.create().texOffs(20, 28).addBox(-3.0F, -8.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 5.5F, 2.0F, 0.0F, 0.0F, -0.3054F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int i1, int i2) {
        root.render(poseStack,vertexConsumer,i,i1,i2);
    }
}