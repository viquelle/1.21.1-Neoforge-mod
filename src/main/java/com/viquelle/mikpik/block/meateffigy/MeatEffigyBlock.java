package com.viquelle.mikpik.block.meateffigy;

import com.mojang.serialization.MapCodec;
import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.blockentity.MeatEffigyBlockEntity;
import com.viquelle.mikpik.ghost.GhostManager;
import com.viquelle.mikpik.item.items.HeartItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class MeatEffigyBlock extends BaseEntityBlock {
    public static final MapCodec<MeatEffigyBlock> CODEC = simpleCodec(MeatEffigyBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final VoxelShape SHAPE =
            Block.box(
                    4.0D,
                    0.0D,
                    4.0D,
                    12.0D,
                    16.0D,
                    12.0D
            );

    public MeatEffigyBlock(Properties properties) {
        super(properties);

        registerDefaultState(
                stateDefinition.any()
                        .setValue(ROTATION, 0)
                        .setValue(HALF, DoubleBlockHalf.LOWER)
        );
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // Рисовать будем через BlockEntityRenderer
        return RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state,
            net.minecraft.world.level.BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER)
            return null;

        return new MeatEffigyBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {

        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        if (pos.getY() >= level.getMaxBuildHeight() - 1)
            return null;

        if (!level.getBlockState(pos.above()).canBeReplaced(context))
            return null;

        return defaultBlockState()
                .setValue(ROTATION, RotationSegment.convertToSegment(context.getRotation()))
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, HALF);
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction direction,
                                  BlockState neighbourState,
                                  LevelAccessor level,
                                  BlockPos pos,
                                  BlockPos neighbourPos) {

        DoubleBlockHalf half = state.getValue(HALF);

        if (direction.getAxis() == Direction.Axis.Y) {

            if (half == DoubleBlockHalf.LOWER
                    && direction == Direction.UP
                    && !neighbourState.is(this)) {

                return Blocks.AIR.defaultBlockState();
            }

            if (half == DoubleBlockHalf.UPPER
                    && direction == Direction.DOWN
                    && !neighbourState.is(this)) {

                return Blocks.AIR.defaultBlockState();
            }
        }

        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    protected void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState newState,
            boolean movedByPiston
    ) {
        if (!state.is(newState.getBlock())) {
            if (!level.isClientSide) {
                BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER
                        ? pos
                        : pos.below();

                if (level.getBlockEntity(lowerPos) instanceof MeatEffigyBlockEntity effigy
                        && effigy.getOwner() != null
                        && level.getServer() != null) {

                    ServerPlayer owner = level.getServer()
                            .getPlayerList()
                            .getPlayer(effigy.getOwner());

                    if (owner != null) {
                        MeatEffigyBlockEntity.clearBinding(owner);
                    }
                }
            }

            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos otherPos = half == DoubleBlockHalf.LOWER
                    ? pos.above()
                    : pos.below();

            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(this)) {
                level.removeBlock(otherPos, false);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        return handleUse(state, level, pos, player, stack)
                ? ItemInteractionResult.SUCCESS
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!player.getMainHandItem().isEmpty()) {
            return InteractionResult.PASS;
        }

        return handleUse(state, level, pos, player, ItemStack.EMPTY)
                ? InteractionResult.SUCCESS
                : InteractionResult.PASS;
    }

    private boolean handleUse(BlockState state, Level level, BlockPos pos, Player player, ItemStack stack) {
        if (GhostManager.isGhost(player)) return true;

        BlockPos lowerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
        if (!(level.getBlockEntity(lowerPos) instanceof MeatEffigyBlockEntity effigy)) return false;

        if (level.isClientSide) return true;

        if (!effigy.isPowered()) {
            if (HeartItem.isCharged(stack)) {
                effigy.bind(player);
                MeatEffigyBlockEntity.setBinding(player, level, lowerPos);

                stack.shrink(1);

                level.playSound(
                        null,
                        lowerPos.getX(), lowerPos.getY(), lowerPos.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER,
                        SoundSource.BLOCKS,
                        1.0F,
                        0.7F
                );

                player.displayClientMessage(
                        Component.translatable("message." + MikpikMod.MODID + ".effigy_bound"),
                        true
                );

                return true;
            }

            player.displayClientMessage(
                    Component.translatable("message." + MikpikMod.MODID + ".effigy_needs_charged_heart"),
                    true
            );

            return true;
        }

        if (effigy.isOwnedBy(player)) {
            player.displayClientMessage(
                    Component.translatable("message." + MikpikMod.MODID + ".effigy_is_yours")
                            .withColor(0x00FF00),
                    true
            );
        } else {
            player.displayClientMessage(
                    Component.translatable("message." + MikpikMod.MODID + ".effigy_is_someone_elses"),
                    true
            );
        }

        return true;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
               return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
        }

        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

}