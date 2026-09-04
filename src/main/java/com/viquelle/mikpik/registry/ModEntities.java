package com.viquelle.mikpik.registry;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.entity.eye.EyeEntity;
import com.viquelle.mikpik.entity.firefly.FireflyEntity;
import com.viquelle.mikpik.entity.hand.HandEntity;
import com.viquelle.mikpik.entity.shadowgrabber.ShadowGrabberEntity;
import com.viquelle.mikpik.entity.watcher.WatcherEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MikpikMod.MODID);

    public static final Supplier<EntityType<EyeEntity>> EYE =
            ENTITY_TYPES.register("eye", () -> EntityType.Builder
                    .of(EyeEntity::new, MobCategory.MISC)
                    .sized(EyeEntity.WIDTH, EyeEntity.HEIGHT)
                    .clientTrackingRange(8)
                    .updateInterval(3)
                    .noSave()
                    .build("eye")
            );


    public static final Supplier<EntityType<ShadowGrabberEntity>> SHADOW_GRABBER =
            ENTITY_TYPES.register("shadow_grabber", () -> EntityType.Builder
                    .of(ShadowGrabberEntity::new, MobCategory.MISC)
                    .sized(0.8f, 0.8f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .noSave()
                    .build("shadow_grabber"));


    public static final Supplier<EntityType<WatcherEntity>> WATCHER =
            ENTITY_TYPES.register("watcher", () -> EntityType.Builder
                    .of(WatcherEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .updateInterval(3)
                    .fireImmune()
                    .build("watcher"));

    public static final Supplier<EntityType<HandEntity>> HAND =
            ENTITY_TYPES.register("hand", () -> EntityType.Builder
                    .of(HandEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .fireImmune()
                    .build("hand"));

    public static final Supplier<EntityType<FireflyEntity>> FIREFLY =
            ENTITY_TYPES.register("firefly", () -> EntityType.Builder
                    .of(FireflyEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .updateInterval(3)
                    .build("firefly"));
}
