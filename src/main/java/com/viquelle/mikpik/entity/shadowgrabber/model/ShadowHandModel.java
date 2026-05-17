package com.viquelle.mikpik.entity.shadowgrabber.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.viquelle.mikpik.MikpikMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ShadowHandModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(MikpikMod.MODID, "shadow_grabber_hand"), "main");
	private final ModelPart hand;
	private final ModelPart bone8;
	private final ModelPart bone;
	private final ModelPart bone5;
	private final ModelPart bone3;
	private final ModelPart bone2;
	private final ModelPart bone6;
	private final ModelPart bone7;
	private final ModelPart bone4;

	public ShadowHandModel(ModelPart root) {
		this.hand = root.getChild("hand");
		this.bone8 = this.hand.getChild("bone8");
		this.bone = this.bone8.getChild("bone");
		this.bone5 = this.bone8.getChild("bone5");
		this.bone3 = this.bone8.getChild("bone3");
		this.bone2 = this.bone8.getChild("bone2");
		this.bone6 = this.bone8.getChild("bone6");
		this.bone7 = this.bone8.getChild("bone7");
		this.bone4 = this.bone8.getChild("bone4");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition hand = partdefinition.addOrReplaceChild("hand", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -3.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition bone8 = hand.addOrReplaceChild("bone8", CubeListBuilder.create(), PartPose.offset(5.5F, 1.0F, 4.9913F));

		PartDefinition bone = bone8.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 6).addBox(1.0F, 0.0F, -5.4913F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(2, 6).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 3.6314F, -4.8291F, 0.829F, 0.0F, 0.0F));

		PartDefinition bone5 = bone8.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(0, 6).addBox(1.0F, 0.0F, -5.4913F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 0.6545F, 0.0F, 0.0F));

		PartDefinition cube_r2 = bone5.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(2, 6).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 3.6314F, -4.8291F, 0.829F, 0.0F, 0.0F));

		PartDefinition bone3 = bone8.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 6).addBox(1.0F, 0.0F, -5.4913F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = bone3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(2, 6).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 3.6314F, -4.8291F, 0.829F, 0.0F, 0.0F));

		PartDefinition bone2 = bone8.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(2, 6).addBox(1.0F, 0.0F, -5.4913F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, 0.0F, 0.0F, 0.3655F, -0.147F, 0.3655F));

		PartDefinition cube_r4 = bone2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(2, 6).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 3.6314F, -4.8291F, 0.829F, 0.0F, 0.0F));

		PartDefinition bone6 = bone8.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(0, 6).addBox(1.0F, 0.0F, -5.4913F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.5F, 0.0F, 0.0F));

		PartDefinition cube_r5 = bone6.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 6).addBox(-0.5F, -0.0416F, -0.4533F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.5F, 3.625F, -4.8913F, 0.829F, 0.0F, 0.0F));

		PartDefinition bone7 = bone8.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(0, 6).addBox(-0.3488F, -0.4253F, -5.4913F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.25F, -1.5F, 0.0F, 0.0F, 0.0F, -0.48F));

		PartDefinition cube_r6 = bone7.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(2, 6).addBox(-0.5797F, -0.1059F, -0.447F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1512F, 3.3247F, -4.9913F, 0.7895F, 0.286F, -0.2729F));

		PartDefinition bone4 = bone8.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(2, 6).addBox(-0.5F, -0.171F, -5.0214F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, 0.0F, -0.5F, 0.3491F, 0.0F, 0.0F));

		PartDefinition cube_r7 = bone4.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 6).addBox(-0.5F, -0.1997F, -0.4779F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.579F, -4.5214F, 0.829F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		hand.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

}