package com.viquelle.mikpik.entity.shadowgrabber;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShadowGrabberEntity extends Entity {
    public final AnimationState portalSpawnAnimationState = new AnimationState();
    public final AnimationState portalIdleAnimationState = new AnimationState();
    public final AnimationState forearmGrowingAnimationState = new AnimationState();

    private boolean animationsStarted = false;

    private static final EntityDataAccessor<Float> DIR_X =
            SynchedEntityData.defineId(ShadowGrabberEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Y =
            SynchedEntityData.defineId(ShadowGrabberEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DIR_Z =
            SynchedEntityData.defineId(ShadowGrabberEntity.class, EntityDataSerializers.FLOAT);

    private static final EntityDataAccessor<Float> LENGTH =
            SynchedEntityData.defineId(ShadowGrabberEntity.class, EntityDataSerializers.FLOAT);

    public ShadowGrabberEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DIR_X, 0.0f);
        builder.define(DIR_Y, 0.0f);
        builder.define(DIR_Z, 1.0f);
        builder.define(LENGTH, 2.0f);
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        Vec3 root = this.position();
        Vec3 hand = root.add(this.getDebugHandLocalPos());

        double minX = Math.min(root.x, hand.x) - 2.0;
        double minY = Math.min(root.y, hand.y) - 2.0;
        double minZ = Math.min(root.z, hand.z) - 2.0;

        double maxX = Math.max(root.x, hand.x) + 2.0;
        double maxY = Math.max(root.y, hand.y) + 2.0;
        double maxZ = Math.max(root.z, hand.z) + 2.0;

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void tick() {
        super.tick();

        this.noPhysics = true;
        this.setNoGravity(true);

        if (!animationsStarted) {
            animationsStarted = true;

            portalSpawnAnimationState.start(this.tickCount);
            portalIdleAnimationState.start(this.tickCount);
            forearmGrowingAnimationState.start(this.tickCount);
        }
    }

    public Vec3 getDebugDirection() {
        Vec3 dir = new Vec3(
                this.entityData.get(DIR_X),
                this.entityData.get(DIR_Y),
                this.entityData.get(DIR_Z)
        );

        if (dir.lengthSqr() < 0.0001) {
            return new Vec3(0.0, 0.0, 1.0);
        }

        return dir.normalize();
    }

    public void setDebugDirection(Vec3 dir) {
        if (dir.lengthSqr() < 0.0001) {
            dir = new Vec3(0.0, 0.0, 1.0);
        }

        dir = dir.normalize();

        this.entityData.set(DIR_X, (float) dir.x);
        this.entityData.set(DIR_Y, (float) dir.y);
        this.entityData.set(DIR_Z, (float) dir.z);
    }

    public float getDebugLength() {
        return this.entityData.get(LENGTH);
    }

    public void setDebugLength(float length) {
        this.entityData.set(LENGTH, Math.max(0.0f, length));
    }

    public Vec3 getDebugHandLocalPos() {
        return getDebugDirection().scale(getDebugLength());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setDebugDirection(new Vec3(
                tag.getFloat("DirX"),
                tag.getFloat("DirY"),
                tag.getFloat("DirZ")
        ));
        setDebugLength(tag.getFloat("Length"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        Vec3 dir = getDebugDirection();

        tag.putFloat("DirX", (float) dir.x);
        tag.putFloat("DirY", (float) dir.y);
        tag.putFloat("DirZ", (float) dir.z);
        tag.putFloat("Length", getDebugLength());
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 96 * 96;
    }
}