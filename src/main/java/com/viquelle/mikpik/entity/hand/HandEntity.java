package com.viquelle.mikpik.entity.hand;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class HandEntity extends Entity {
    public enum State {
        IDLE,
        REACHING,
        ATTACHED,
        RETURNING
    }

    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(HandEntity.class, EntityDataSerializers.INT);

    public State getState() {
        return State.values()[entityData.get(STATE)];
    }

    public void setState(State state) {
        entityData.set(STATE, state.ordinal());
    }

    private static final EntityDataAccessor<Vector3f> ORIGIN =
            SynchedEntityData.defineId(HandEntity.class, EntityDataSerializers.VECTOR3);

    public void setOrigin(Vec3 pos) {
        entityData.set(ORIGIN, pos.toVector3f());
    }

    public Vec3 getOrigin() {
        return new Vec3(entityData.get(ORIGIN));
    }

    public HandEntity(EntityType<? extends HandEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setInvulnerable(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STATE, 0);
        builder.define(ORIGIN, new Vector3f(0, 0, 0));
    }

    private boolean initialized = false;
    @Override
    public void tick() {
        super.tick();

        switch (getState()) {
            case IDLE -> idleTick();
            case REACHING -> reachingTick();
            case ATTACHED -> attachedTick();
            case RETURNING -> returningTick();
        }
    }

    public void initialize() {
        setOrigin(position());
    }
    public void idleTick() {}
    public void reachingTick() {}
    public void attachedTick() {}
    public void returningTick() {}

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        Vec3 origin = getOrigin();
        tag.put("Origin", newDoubleList(origin.x,origin.y,origin.z));

        tag.putInt("State", getState().ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Origin", tag.TAG_LIST)) {
            ListTag list = tag.getList("Origin", ListTag.TAG_DOUBLE);

            if (list.size() == 3) {
                setOrigin(new Vec3(
                        list.getDouble(0),
                        list.getDouble(1),
                        list.getDouble(2)
                ));
            }
        }

        if (tag.contains("State", ListTag.TAG_INT)) {
            setState(State.values()[tag.getInt("State")]);
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
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
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 96 * 96;
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling().inflate(8.0);
    }
}
