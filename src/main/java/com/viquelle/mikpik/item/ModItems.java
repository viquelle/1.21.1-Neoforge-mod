package com.viquelle.mikpik.item;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MikpikMod.MODID);

    public static final Supplier<Item> PIBBLE =
            ITEMS.register("pibble", () -> new PibbleItem(new Item.Properties())
    );

    public static final Supplier<Item> PLUSHY =
            ITEMS.register("plushy", () -> new PlushyItem(new Item.Properties()));

    public static final Supplier<Item> FLOWER_CROWN =
            ITEMS.register("flower_crown", () -> new FlowerCrownItem(
                    ModArmorMaterials.FLOWER_CROWN,
                    ArmorItem.Type.HELMET,
                    new Item.Properties()
                            .stacksTo(1)
                            .durability(20*60*16)
            ));

    public static final Supplier<Item> MAGNETLAMPE =
            ITEMS.register("magnetlampe", () -> new Magnetlampe(
                    new Item.Properties()
            ));

    public static final Supplier<Item> HEART =
            ITEMS.register("heart", () -> new HeartItem(
                    new Item.Properties().stacksTo(1)
            ));

    public static final Supplier<Item> PENALTY_REMOVER =
            ITEMS.register("penalty_remover", () -> new PenaltyRemoverItem(
                    new Item.Properties().stacksTo(1)
            ));
}
