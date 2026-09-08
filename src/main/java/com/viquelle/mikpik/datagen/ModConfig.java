package com.viquelle.mikpik.datagen;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;

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

    public static final ModConfigSpec.IntValue MAX_CAMP_FUEL_TIME;
    public static final ModConfigSpec.IntValue INITIAL_CAMP_FUEL_TIME;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FUEL_VALUES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> FUEL_BLACKLIST;

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
                .comment("Custom spoil times for specific food items.",
                        "Syntax: item_id=spoil_ticks",
                        "Example: \"minecraft:salmon=48000\"")
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
                .comment(
                        "List of custom item transformations when spoiling.",
                        "Syntax: source_item_id=transformed_item_id",
                        "Example: \"minecraft:milk_bucket=minecraft:bucket\""
                )
                .defineList("custom_spoil_transform", List.of(
                        "minecraft:milk_bucket=minecraft:bucket",
                        "minecraft:potato=minecraft:poisonous_potato"
                ), () -> "", item -> item instanceof String);

        builder.pop();

        builder.push("ham_bat");
        HAM_BAT_SPOIL_TIME = builder
                .comment("Spoil time for the ham bat item.")
                .defineInRange("spoil_time", 144000, 1, 10000000);

        HAM_BAT_SPOILING = builder
                .comment("If true, the Ham Bat will spoil over time.")
                .define("is_spoiling", true);
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
                .defineInRange("rain_water_multiplier", 2.0, 0.0, 10.0);

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

        builder.push("campfire fuel");

        MAX_CAMP_FUEL_TIME = builder
                .comment("This value should be bigger than INITIAL_CAMP_FUEL_TIME")
                .defineInRange("max_camp_fuel_time", 18000, 1, Integer.MAX_VALUE);

        INITIAL_CAMP_FUEL_TIME = builder
                .comment("Initial campfire fuel duration in ticks.")
                .defineInRange("initial_camp_fuel_time", 18000, 0, 999999999);

        FUEL_VALUES = builder
                .comment(
                        "Defines fuel values for items and tags.",
                        "Syntax: item_id=ticks OR #tag_id=ticks",
                        "Examples: 'minecraft:coal=1600', '#minecraft:logs=300'",
                        "If an item matches multiple tags, the first match in this list is used."
                )
                .defineList("fuel_values", List.of(
                        "minecraft:coal_block=16000",
                        "minecraft:dried_kelp_block=4000",
                        "minecraft:blaze_rod=2400",
                        "minecraft:coal=1600",
                        "minecraft:charcoal=1600",
                        "#minecraft:logs=1600",
                        "#minecraft:bamboo_blocks=1600",
                        "minecraft:mangrove_roots=1600",
                        "minecraft:crafting_table=1600",
                        "minecraft:bookshelf=2800",
                        "minecraft:chiseled_bookshelf=2800",
                        "minecraft:note_block=3200",
                        "minecraft:jukebox=3200",
                        "minecraft:chest=3200",
                        "minecraft:trapped_chest=3200",
                        "#minecraft:wooden_fences=1600",
                        "#minecraft:fence_gates=1600",
                        "minecraft:lectern=2400",
                        "minecraft:barrel=2400",
                        "minecraft:smithing_table=2400",
                        "#minecraft:planks=400",
                        "minecraft:bamboo_mosaic=400",
                        "minecraft:daylight_detector=800",
                        "minecraft:loom=1600",
                        "minecraft:cartography_table=1600",
                        "minecraft:fletching_table=1600",
                        "minecraft:composter=2800",
                        "#minecraft:wooden_stairs=300",
                        "minecraft:bamboo_mosaic_stairs=300",
                        "#minecraft:wooden_doors=800",
                        "#minecraft:boats=2000",
                        "#minecraft:wooden_trapdoors=400",
                        "#minecraft:wooden_pressure_plates=400",
                        "#minecraft:wooden_slabs=200",
                        "minecraft:bamboo_mosaic_slab=200",
                        "#minecraft:wooden_pickaxes=1600",
                        "#minecraft:wooden_axes=1600",
                        "#minecraft:wooden_hoes=1000",
                        "#minecraft:wooden_swords=1000",
                        "#minecraft:wooden_shovels=600",
                        "minecraft:bow=1000",
                        "minecraft:crossbow=1600",
                        "minecraft:fishing_rod=800",
                        "minecraft:ladder=1000",
                        "#minecraft:signs=600",
                        "#minecraft:hanging_signs=800",
                        "#minecraft:banners=800",
                        "minecraft:stick=200",
                        "#minecraft:wool=400",
                        "#minecraft:wool_carpets=200",
                        "minecraft:azalea=200",
                        "minecraft:flowering_azalea=200",
                        "#minecraft:saplings=200",
                        "minecraft:bowl=200",
                        "minecraft:scaffolding=400",
                        "minecraft:dead_bush=100",
                        "minecraft:bamboo=100",
                        "#minecraft:wooden_buttons=100"
                ), () -> "", val -> val instanceof String);

        FUEL_BLACKLIST = builder
                .comment(
                        "Items that will NEVER act as fuel, even if they are in a burnable tag.",
                        "Syntax: item_id",
                        "Example: 'minecraft:stick' (if you want to save sticks from burning)"
                )
                .defineList("blacklist", List.of(
                ), () -> "", val -> val instanceof String);

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

    public static int getFuelValue(String itemId) {
        if (itemId == null) return -1;

        List<? extends String> blacklist = FUEL_BLACKLIST.get();
        for (String entry : blacklist) {
            if (entry == null) continue;

            if (entry.trim().equals(itemId)) {
                return 0;
            }
        }

        List<? extends String> list = FUEL_VALUES.get();
        for (String entry : list) {
            if (entry == null) continue;

            String[] parts = entry.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                boolean isMatch = false;

                if (key.equals(itemId)) {
                    isMatch = true;
                } else if (key.startsWith("#")) {
                    try {
                        ResourceLocation tagRl = ResourceLocation.parse(key.substring(1));
                        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagRl);

                        ResourceLocation itemRl = ResourceLocation.parse(itemId);
                        Item item = BuiltInRegistries.ITEM.get(itemRl);

                        if (item != Items.AIR) {
                            isMatch = new ItemStack(item).is(tagKey);
                        }
                    } catch (Exception ignored) {
                    }
                }

                if (isMatch) {
                    try {
                        return Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return -1;
    }

    public static int getFuelValue(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return -1;

        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (rl.toString().equals(Items.AIR.toString())) return -1;

        return getFuelValue(rl.toString());
    }
}