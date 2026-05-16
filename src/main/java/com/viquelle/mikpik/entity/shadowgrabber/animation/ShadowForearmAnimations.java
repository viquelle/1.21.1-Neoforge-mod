package com.viquelle.mikpik.entity.shadowgrabber.animation;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class ShadowForearmAnimations {
	public static final AnimationDefinition Idle = AnimationDefinition.Builder.withLength(2.75F).looping()
		.build();

	public static final AnimationDefinition growing = AnimationDefinition.Builder.withLength(1.0F)
		.addAnimation("bone", new AnimationChannel(AnimationChannel.Targets.SCALE,
			new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
			new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
		))
		.build();
}