package com.viquelle.mikpik.item;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.datagen.ModConfig;
import com.viquelle.mikpik.registry.ModDataComponents;
import com.viquelle.mikpik.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = MikpikMod.MODID)
public class FreshnessManager {

    private static final Map<ResourceKey<Level>, Map<BlockPos, Long>> CONTAINER_LAST_CHECK = new ConcurrentHashMap<>();
    private static int serverTickCounter = 0;
    private static final int CHECK_INTERVAL_TICKS = 40; // 2 секунд

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be instanceof Container) {
            setLastCheckTime((Level) event.getLevel(), event.getPos(), ((Level) event.getLevel()).getGameTime());
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        Map<BlockPos, Long> levelMap = CONTAINER_LAST_CHECK.get(((Level)event.getLevel()).dimension());
        if (levelMap != null) {
            levelMap.remove(event.getPos());
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel().isClientSide() || !(event.getChunk() instanceof LevelChunk chunk)) return;
        Level level = chunk.getLevel();
        long currentTick = level.getGameTime();

        for (BlockEntity be : chunk.getBlockEntities().values()) {
            if (be instanceof Container) {
                setLastCheckTime(level, be.getBlockPos(), currentTick);
            }
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel().isClientSide() || !(event.getChunk() instanceof LevelChunk chunk)) return;
        Map<BlockPos, Long> levelMap = CONTAINER_LAST_CHECK.get(event.getChunk().getLevel().dimension());
        if (levelMap != null) {
            for (BlockEntity be : chunk.getBlockEntities().values()) {
                if (be instanceof Container) {
                    levelMap.remove(be.getBlockPos());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        if (!ModConfig.ENABLE_SPOILING.get()) return;

        serverTickCounter++;
        if (serverTickCounter < CHECK_INTERVAL_TICKS) return;
        serverTickCounter = 0;

        for (Level level : event.getServer().getAllLevels()) {
            if (level.isClientSide()) continue;

            Map<BlockPos, Long> levelMap = CONTAINER_LAST_CHECK.get(level.dimension());
            if (levelMap == null || levelMap.isEmpty()) continue;

            long currentTick = level.getGameTime();
            var iterator = levelMap.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                BlockPos pos = entry.getKey();

                if (!level.hasChunkAt(pos)) {
                    iterator.remove();
                    continue;
                }

                BlockEntity be = level.getBlockEntity(pos);
                if (!(be instanceof Container container)) {
                    iterator.remove();
                    continue;
                }

                int deltaTicks = (int) (currentTick - entry.getValue());
                if (deltaTicks >= CHECK_INTERVAL_TICKS) {
                    float multiplier = calculateCoolingMultiplier(level, pos);
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        applySpoilageToContainerSlot(container, i, level, multiplier, deltaTicks);
                    }
                    entry.setValue(currentTick);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !ModConfig.ENABLE_SPOILING.get()) return;

        BlockEntity be = event.getLevel().getBlockEntity(event.getPos());
        if (be instanceof Container container) {
            long currentTick = event.getLevel().getGameTime();
            long lastCheck = getLastCheckTime(event.getLevel(), event.getPos());
            int deltaTicks = (int) (currentTick - lastCheck);

            if (deltaTicks > 0) {
                float multiplier = calculateCoolingMultiplier(event.getLevel(), event.getPos());
                for (int i = 0; i < container.getContainerSize(); i++) {
                    applySpoilageToContainerSlot(container, i, event.getLevel(), multiplier, deltaTicks);
                }
                setLastCheckTime(event.getLevel(), event.getPos(), currentTick);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre event) {
        if (!ModConfig.ENABLE_SPOILING.get() || event.getEntity().level().isClientSide()) return;
        Player player = event.getEntity();

        if (player.tickCount % 20 != 0) return;

        float multiplier = ModConfig.MULT_INVENTORY.get().floatValue();
        if (player.isInWater() || (player.level().isRaining() && player.level().canSeeSky(player.blockPosition()))) {
            multiplier *= ModConfig.MULT_RAIN_WATER.get().floatValue();
        }

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            applySpoilageToContainerSlot(player.getInventory(), i, player.level(), multiplier, 20);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!ModConfig.ENABLE_SPOILING.get() || event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ItemEntity itemEntity)) return;
        if (itemEntity.tickCount % 60 != 0) return;

        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) return;

        float multiplier = ModConfig.MULT_GROUND.get().floatValue();
        if (itemEntity.isInWater() || (itemEntity.level().isRaining() && itemEntity.level().canSeeSky(itemEntity.blockPosition()))) {
            multiplier *= ModConfig.MULT_RAIN_WATER.get().floatValue();
        }
        if (itemEntity.level().getBiome(itemEntity.blockPosition()).is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
            multiplier *= ModConfig.MULT_COLD_BIOME.get().floatValue();
        }

        if (applySpoilageToStack(stack, itemEntity.level(), multiplier, 60)) {
            itemEntity.setItem(getSpoiledResult(stack));
        }
    }

    private static void applySpoilageToContainerSlot(Container container, int slot, Level level, float multiplier, int deltaTicks) {
        ItemStack stack = container.getItem(slot);
        if (stack.isEmpty()) return;

        if (applySpoilageToStack(stack, level, multiplier, deltaTicks)) {
            container.setItem(slot, getSpoiledResult(stack));
        }
    }

    private static boolean applySpoilageToStack(ItemStack stack, Level level, float multiplier, int deltaTicks) {
        if (stack.isEmpty()) return false;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String idString = itemId.toString();

        if (ModConfig.BLACKLIST.get().contains(idString)) return false;

        int targetSpoilTime = ModConfig.getCustomTime(idString);
        if (targetSpoilTime <= 0) {
            if (idString.equals(ModItems.HAM_BAT.get().toString())) {
                if (ModConfig.HAM_BAT_SPOILING.get()) return false;
                targetSpoilTime = ModConfig.HAM_BAT_SPOIL_TIME.get();
            } else if (stack.has(DataComponents.FOOD)) {
                targetSpoilTime = ModConfig.DEFAULT_SPOIL_TIME.get();
            } else {
                return false;
            }
        }

        if (!stack.has(ModDataComponents.SPOIL_TIME.get())) {
            stack.set(ModDataComponents.SPOIL_TIME.get(), targetSpoilTime);
        }

        Float timeRemaining = stack.get(ModDataComponents.TIME_REMAINING.get());
        if (timeRemaining == null) {
            Integer spoilTime = stack.get(ModDataComponents.SPOIL_TIME.get());
            timeRemaining = spoilTime != null ? spoilTime.floatValue() : (float) targetSpoilTime;
        }

        float deduction = deltaTicks * multiplier;
        float newTimeRemaining = timeRemaining - deduction;
        stack.set(ModDataComponents.LAST_REDUCTION.get(), multiplier);

        if (newTimeRemaining <= 0) {
            return true;
        } else {
            stack.set(ModDataComponents.TIME_REMAINING.get(), newTimeRemaining);
            return false;
        }
    }

    private static float calculateCoolingMultiplier(Level level, BlockPos pos) {
        float multiplier = ModConfig.MULT_STORAGE.get().floatValue();

        if (level.getBiome(pos).is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
            multiplier *= ModConfig.MULT_COLD_BIOME.get().floatValue();
        }

        for (Direction dir : Direction.values()) {
            var state = level.getBlockState(pos.relative(dir));
            if (state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK)) multiplier *= ModConfig.MULT_SNOW.get().floatValue();
            else if (state.is(Blocks.ICE)) multiplier *= ModConfig.MULT_ICE.get().floatValue();
            else if (state.is(Blocks.PACKED_ICE)) multiplier *= ModConfig.MULT_PACKED_ICE.get().floatValue();
            else if (state.is(Blocks.BLUE_ICE)) multiplier *= ModConfig.MULT_BLUE_ICE.get().floatValue();
        }

        return Math.max(ModConfig.MIN_TOTAL_STORAGE_MULT.get().floatValue(), multiplier);
    }

    private static ItemStack getSpoiledResult(ItemStack original) {
        return new ItemStack(Blocks.DIRT, original.getCount());
    }

    private static long getLastCheckTime(Level level, BlockPos pos) {
        return CONTAINER_LAST_CHECK.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>())
                .getOrDefault(pos, level.getGameTime());
    }

    private static void setLastCheckTime(Level level, BlockPos pos, long tick) {
        CONTAINER_LAST_CHECK.computeIfAbsent(level.dimension(), k -> new ConcurrentHashMap<>())
                .put(pos, tick);
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.has(ModDataComponents.SPOIL_TIME.get())) {
            Float timeRemaining = stack.get(ModDataComponents.TIME_REMAINING.get());
            if (timeRemaining == null) {
                Integer spoilTime = stack.get(ModDataComponents.SPOIL_TIME.get());
                timeRemaining = spoilTime != null ? spoilTime.floatValue() : 0f;
            }

            float avgRed = stack.getOrDefault(ModDataComponents.LAST_REDUCTION.get(), 1f);
            float days = Math.max(0f, timeRemaining / 24000.0f / avgRed);
            String formattedDays = String.format(Locale.ROOT, "%.1f", days); // 1 знак после запятой

            Component spoilTooltip = Component.translatable("tooltip." + MikpikMod.MODID + ".spoils_in", formattedDays)
                    .withStyle(ChatFormatting.GRAY);

            event.getToolTip().add(spoilTooltip);
        }
    }
}