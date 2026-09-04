package com.viquelle.mikpik.datagen;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.List;

public class ModConfig {

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue AMBIENT_BRIGHTNESS;
    public static final ModConfigSpec.BooleanValue ENABLE_SPOILING;
    public static final ModConfigSpec.IntValue DEFAULT_SPOIL_TIME;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_TIMES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CUSTOM_SPOIL_TRANSFORM;

    public static final ModConfigSpec.IntValue HAM_BAT_SPOIL_TIME;
    public static final ModConfigSpec.BooleanValue HAM_BAT_SPOILING;

    public static final ModConfigSpec.DoubleValue MULT_INVENTORY;
    public static final ModConfigSpec.DoubleValue MULT_GROUND;
    public static final ModConfigSpec.DoubleValue MULT_STORAGE;
    public static final ModConfigSpec.DoubleValue MIN_TOTAL_STORAGE_MULT;
    public static final ModConfigSpec.DoubleValue MULT_RAIN_WATER;
    public static final ModConfigSpec.DoubleValue MULT_SNOW;
    public static final ModConfigSpec.DoubleValue MULT_ICE;
    public static final ModConfigSpec.DoubleValue MULT_PACKED_ICE;
    public static final ModConfigSpec.DoubleValue MULT_BLUE_ICE;
    public static final ModConfigSpec.DoubleValue MULT_COLD_BIOME;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");

        AMBIENT_BRIGHTNESS = builder
                .comment("How bright is the eyes adaptation light (0.0 to 1.0)")
                .defineInRange("ambient_brightness", 0.3, 0.0, 1.0);

        ENABLE_SPOILING = builder
                .comment("Enable or disable the food spoiling mechanic.")
                .define("enable_spoiling", true);

        DEFAULT_SPOIL_TIME = builder
                .comment("Base spoil time for food items not explicitly defined in the config.")
                .defineInRange("default_spoil_time", 168000, 1, 10000000);

        BLACKLIST = builder
                .comment("List of food items that should NOT spoil (has priority).")
                .defineList("blacklist", List.of(
                        "minecraft:golden_apple",
                        "minecraft:enchanted_golden_apple",
                        "minecraft:rotten_flesh",
                        "minecraft:dried_kelp",
                        "minecraft:chorus_fruit",
                        "minecraft:honey_bottle"
                ), () -> "", item -> item instanceof String); // () -> "" enables "Add" button in config GUI

        CUSTOM_TIMES = builder
                .comment("Custom spoil times for specific food items. Format: 'modid:itemid=time_in_ticks'")
                .defineList("custom_times", List.of(
                        "minecraft:cod=48000",
                        "minecraft:salmon=48000",
                        "minecraft:tropical_fish=48000",
                        "minecraft:pufferfish=48000",
                        "minecraft:chicken=72000",
                        "minecraft:beef=96000",
                        "minecraft:porkchop=96000",
                        "minecraft:mutton=96000",
                        "minecraft:rabbit=96000",
                        "minecraft:cooked_beef=168000",
                        "minecraft:cooked_porkchop=168000",
                        "minecraft:cooked_chicken=144000",
                        "minecraft:cooked_salmon=144000",
                        "minecraft:cooked_cod=144000",
                        "minecraft:cooked_mutton=168000",
                        "minecraft:cooked_rabbit=168000",
                        "minecraft:potato=336000",
                        "minecraft:carrot=336000",
                        "minecraft:beetroot=336000",
                        "minecraft:baked_potato=168000",
                        "minecraft:apple=216000",
                        "minecraft:melon_slice=120000",
                        "minecraft:melon=240000",
                        "minecraft:sweet_berries=168000",
                        "minecraft:glow_berries=168000",
                        "minecraft:mushroom_stew=96000",
                        "minecraft:beetroot_soup=96000",
                        "minecraft:rabbit_stew=96000",
                        "minecraft:suspicious_stew=48000",
                        "minecraft:bread=216000",
                        "minecraft:cookie=168000",
                        "minecraft:pumpkin_pie=168000",
                        "minecraft:wheat=216000",
                        "minecraft:sugar=216000",
                        "minecraft:egg=216000",
                        "minecraft:red_mushroom=120000",
                        "minecraft:brown_mushroom=120000",
                        "minecraft:milk_bucket=72000",
                        "minecraft:cocoa_beans=168000",
                        "minecraft:pumpkin=336000"
                ), () -> "", item -> item instanceof String); // () -> "" enables "Add" button in config GUI

        CUSTOM_SPOIL_TRANSFORM = builder
                .comment("Список кастомной трансформации при сгнивании")
                .defineList("custom_spoil_transform", List.of(
                        "minecraft:milk_bucket=minecraft:bucket",
                        "minecraft:potato=minecraft:poisonous_potato"
                ), () -> "", item -> item instanceof String); // () -> "" enables "Add" button in config GUI

        builder.pop();

        builder.push("ham_bat");
        HAM_BAT_SPOIL_TIME = builder
                .comment("Spoil time for the ham bat item.")
                .defineInRange("spoil_time", 144000, 1, 10000000);

        HAM_BAT_SPOILING = builder
                .comment("If true, the Ham Bat will spoil over time.")
                .define("is_spoiling", false);
        builder.pop();

        builder.push("environment");
        MULT_INVENTORY = builder
                .comment("Spoil time multiplier for items in player inventory.")
                .defineInRange("inventory_multiplier", 1.0, 0.0, 10.0);

        MULT_GROUND = builder
                .comment("Spoil time multiplier for items dropped on the ground.")
                .defineInRange("ground_multiplier", 1.5, 0.0, 10.0);

        MULT_STORAGE = builder
                .comment("Spoil time multiplier for items in storage blocks (chests, etc.).")
                .defineInRange("storage_multiplier", 0.5, 0.0, 10.0);

        MIN_TOTAL_STORAGE_MULT = builder
                .comment("Minimum total multiplier applied to items in storage.")
                .defineInRange("min_total_storage_mult", 0.1, 0.0, 10.0);

        MULT_RAIN_WATER = builder
                .comment("Spoil time multiplier when in rain or water.")
                .defineInRange("rain_water_multiplier", 2, 0.0, 10.0);

        MULT_SNOW = builder
                .comment("Spoil time multiplier when in snow.")
                .defineInRange("snow_multiplier", 0.95, 0.0, 10.0);

        MULT_ICE = builder
                .comment("Spoil time multiplier when on ice.")
                .defineInRange("ice_multiplier", 0.85, 0.0, 10.0);

        MULT_PACKED_ICE = builder
                .comment("Spoil time multiplier when on packed ice.")
                .defineInRange("packed_ice_multiplier", 0.75, 0.0, 10.0);

        MULT_BLUE_ICE = builder
                .comment("Spoil time multiplier when on blue ice.")
                .defineInRange("blue_ice_multiplier", 0.65, 0.0, 10.0);

        MULT_COLD_BIOME = builder
                .comment("Spoil time multiplier when in a cold biome.")
                .defineInRange("cold_biome_multiplier", 0.85, 0.0, 10.0);

        builder.pop();

        SPEC = builder.build();
    }

    public static int getCustomTime(String itemId) {
        List<? extends String> list = CUSTOM_TIMES.get();

        int customTime = -1;
        for (String entry : list) {
            if (entry == null) continue;

            String[] parts = entry.split("=", 2);
            if (parts.length == 2 && parts[0].trim().equals(itemId)) {
                try {
                    customTime = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return customTime;
    }

    @Nullable
    public static Item getCustomSpoilTransform(String itemId) {
        List<? extends String> list = CUSTOM_SPOIL_TRANSFORM.get();

        for (String entry : list) {
            if (entry == null) continue;

            String[] parts = entry.split("=", 2);
            if (parts.length == 2 && parts[0].trim().equals(itemId)) {
                try {
                    ResourceLocation rl = ResourceLocation.parse(parts[1]);
                    Item item = BuiltInRegistries.ITEM.get(rl);
                    MikpikMod.LOGGER.info("{}",item);
                    if (item != Items.AIR) {
                        return item;
                    }
                } catch (Exception ignored) {

                }
            }
        }
        return null;
    }
}