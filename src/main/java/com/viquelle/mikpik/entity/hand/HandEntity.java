package com.viquelle.mikpik.entity.hand;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;

public class HandEntity extends Entity {
    private LivingEntity target;
//    private Vec3 idleTarget;
//    private int idleTimer;
//    private int idleDuration;
//    private boolean idleWaiting = true;

    private static final double REACH_DISTANCE = 2.5;
    private static final double BREAK_DISTANCE = REACH_DISTANCE * 2;

    public enum State {
        IDLE,
        REACHING,
        ATTACHED,
        RETURNING
    }

    private static final EntityDataAccessor<Integer> STATE =
            SynchedEntityData.defineId(HandEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Vector3f> ORIGIN =
            SynchedEntityData.defineId(HandEntity.class, EntityDataSerializers.VECTOR3);

    public HandEntity(EntityType<? extends HandEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(STATE, 0);
        builder.define(ORIGIN, new Vector3f(0, 0, 0));
    }

    public State getState() {
        return State.values()[entityData.get(STATE)];
    }

    public void setState(State state) {
        entityData.set(STATE, state.ordinal());
    }

    public void setOrigin(Vec3 pos) {
        entityData.set(ORIGIN, pos.toVector3f());
    }

    public Vec3 getOrigin() {
        return new Vec3(entityData.get(ORIGIN));
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) return;

        switch (getState()) {
            case IDLE -> idleTick();
            case REACHING -> reachingTick();
            case ATTACHED -> attachedTick();
            case RETURNING -> returningTick();
        }
        updateRotation();
    }

    private void updateRotation() {
        Vec3 origin = getOrigin();

        Vec3 dir = position().subtract(origin);

        if (dir.lengthSqr() < 1e-6) return;

        float yaw = (float) Math.toDegrees(Math.atan2(-dir.x, dir.z));

        setYRot(yaw);
        setYBodyRot(yaw);
        setYHeadRot(yaw);
    }

    private void applyMotion(Vec3 dir, double speed) {
        if (dir.lengthSqr() < 1e-6) return;

        Vec3 desired = dir.normalize().scale(speed);
        Vec3 next = getDeltaMovement().lerp(desired, 0.25);

        setDeltaMovement(next);
        move(MoverType.SELF, next);
    }


    private void idleTick() {
        findTarget();

        if (target != null && target.isAlive()) {
            setState(State.REACHING);
            return;
        }
    }

    private void reachingTick() {
        if (target == null || !target.isAlive()) {
            setState(State.RETURNING);
            return;
        }

        Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.3, 0);

        applyMotion(targetPos.subtract(position()), 0.5);

        if (position().distanceToSqr(targetPos) < 0.5 * 0.5) {
            setState(State.ATTACHED);
        }

        if (getOrigin().distanceToSqr(targetPos) > BREAK_DISTANCE * BREAK_DISTANCE) {
            setState(State.RETURNING);
        }
    }

    private void attachedTick() {
        if (target == null || !target.isAlive()) {
            setState(State.RETURNING);
            return;
        }

        Vec3 origin = getOrigin();
        Vec3 me = position();
        if (origin.distanceToSqr(me) > BREAK_DISTANCE * BREAK_DISTANCE) {
            setState(State.RETURNING);
            return;
        }

        Vec3 center  = target.position().add(0, target.getBbHeight() * 0.3, 0);
        Vec3 toHand = position().subtract(center);
        double distSqr = toHand.lengthSqr();

        if (distSqr < 1e-6) {
            toHand = new Vec3(1,0,0); // Защита от ошибка в normalize
        }
        Vec3 dir = toHand.normalize();
        Vec3 attachPoint = center.add(dir.scale(target.getBbWidth() * 0.5));

        Vec3 toTarget = attachPoint.subtract(position());
        double dist = toTarget.length();

        double speed = Math.min(0.6, dist * 0.4 + 0.05);

        applyMotion(toTarget, speed);

        target.addDeltaMovement(getOrigin().subtract(center).normalize().scale(0.02));
        target.hurtMarked = true;
    }

    private void returningTick() {
        Vec3 origin = getOrigin();

        applyMotion(origin.subtract(position()), 0.15);

        if (position().distanceToSqr(origin) < 0.2 * 0.2) {
            setState(State.IDLE);
        }
    }

    private void findTarget() {
        if (target != null && target.isAlive()) return;

        double radius = BREAK_DISTANCE + 2;

        List<LivingEntity> list = level().getEntitiesOfClass(
                LivingEntity.class,
                getBoundingBox().inflate(radius),
                e -> e instanceof Player && e.isAlive()
        );

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (LivingEntity e : list) {
            double d = distanceToSqr(e);
            if (d < bestDist) {
                best = e;
                bestDist = d;
            }
        }

        if (best != null) {
            target = best;
            setState(State.REACHING);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        Vec3 o = getOrigin();
        tag.put("Origin", newDoubleList(o.x, o.y, o.z));
        tag.putInt("State", getState().ordinal());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Origin", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Origin", Tag.TAG_DOUBLE);
            if (list.size() == 3) {
                setOrigin(new Vec3(
                        list.getDouble(0),
                        list.getDouble(1),
                        list.getDouble(2)
                ));
            }
        }

        if (tag.contains("State", Tag.TAG_INT)) {
            setState(State.values()[tag.getInt("State")]);
        }
    }

    @Override public boolean isNoGravity() { return true; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean shouldRender(double x, double y, double z) { return true; }
    @Override public boolean shouldRenderAtSqrDistance(double d) { return d < 96 * 96; }
}