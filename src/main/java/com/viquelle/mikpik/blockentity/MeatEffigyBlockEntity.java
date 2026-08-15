package com.viquelle.mikpik.blockentity;

import com.viquelle.mikpik.ModAttachments;
import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.ghost.HealthPenailtyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;

import javax.annotation.Nullable;
import java.util.UUID;

import static com.viquelle.mikpik.block.meateffigy.MeatEffigyBlock.ROTATION;

public class MeatEffigyBlockEntity extends BlockEntity {
    private boolean powered = false;

    @Nullable
    private UUID ownerUUID = null;

    public MeatEffigyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MEAT_EFFIGY.get(), pos, state);
    }

    public boolean isPowered() {
        return powered;
    }

    @Nullable
    public UUID getOwner() {
        return ownerUUID;
    }

    public boolean isOwnedBy(Player player) {
        return ownerUUID != null && ownerUUID.equals(player.getUUID());
    }

    public void bind(Player player) {
        this.powered = true;
        this.ownerUUID = player.getUUID();
        setChanged();
        sync();
    }

    public void resetAndRemove() {
        Level level = this.level;
        if (level == null || level.isClientSide || isRemoved()) return;

        level.playSound(
                null,
                worldPosition,
                SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR,
                SoundSource.BLOCKS,
                0.4F,
                0.8F
        );

        // onRemove блока сам удалит верхнюю половину
        level.removeBlock(worldPosition, false);
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.putBoolean("Powered", powered);

        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        powered = tag.getBoolean("Powered");

        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        } else {
            ownerUUID = null;
        }
    }

    private static AttachmentType<GlobalPos> boundEffigyType() {
        return ModAttachments.BOUND_EFFIGY.get();
    }

    public static void setBinding(Player player, Level level, BlockPos pos) {
        player.setData(
                boundEffigyType(),
                GlobalPos.of(level.dimension(), pos.immutable())
        );
    }

    public static void clearBinding(Player player) {
        player.removeData(boundEffigyType());
    }

    public static boolean hasBinding(Player player) {
        return player.hasData(boundEffigyType());
    }

    @Nullable
    public static GlobalPos getBinding(Player player) {
        if (!player.hasData(boundEffigyType())) {
            return null;
        }

        return player.getData(boundEffigyType());
    }

    @Nullable
    public static MeatEffigyBlockEntity getValidEffigy(ServerPlayer player) {
        GlobalPos globalPos = getBinding(player);
        if (globalPos == null) {
            return null;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return null;
        }

        ServerLevel level = server.getLevel(globalPos.dimension());
        if (level == null) {
            return null;
        }

//        if (!level.isLoaded(globalPos.pos())) {
//            return null;
//        }

        BlockEntity blockEntity = level.getBlockEntity(globalPos.pos());

        if (blockEntity instanceof MeatEffigyBlockEntity effigy
                && effigy.isPowered()
                && effigy.isOwnedBy(player)) {
            return effigy;
        }

        return null;
    }

    public static void tryReviveAtEffigy(ServerPlayer player) {
        if (!GhostManager.isGhost(player)) return;

        GlobalPos globalPos = getBinding(player);
        if (globalPos == null) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerLevel level = server.getLevel(globalPos.dimension());
        if (level == null) return;

        BlockEntity blockEntity = level.getBlockEntity(globalPos.pos());

        if (!(blockEntity instanceof MeatEffigyBlockEntity effigy)
                || !effigy.isPowered()
                || !effigy.isOwnedBy(player)) {
            clearBinding(player);
            return;
        }

        BlockPos pos = effigy.getBlockPos();
        float rotation = effigy.getBlockState().getValue(ROTATION) * 22.5f + 180;
        player.teleportTo(
                level,
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D,
                rotation,
                0f
        );
        player.setYBodyRot(rotation);

        effigy.resetAndRemove();
        clearBinding(player);

        GhostManager.revive(player);
    }
}