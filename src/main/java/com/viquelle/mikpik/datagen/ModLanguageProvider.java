package com.viquelle.mikpik.datagen;

import com.viquelle.mikpik.MikpikMod;
import com.viquelle.mikpik.item.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    private final String locale;

    public ModLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        if (locale.equals("ru_ru")) {
            add_ru();
        } else {
            add_en();
        }
    }

    private void add_en() {
        add("itemGroup." + MikpikMod.MODID, "MIK PIK");

        // Предметы
        addItem(ModItems.FLOWER_CROWN, "Flower crown");
        addItem(ModItems.PIBBLE, "Pibble");
        addItem(ModItems.PLUSHY, "Plushy");

    }

    private void add_ru() {
        add("itemGroup." + MikpikMod.MODID, "МЫК ПЫК");

        // Предметы
        addItem(ModItems.FLOWER_CROWN, "Цветочный венок");
        addItem(ModItems.PIBBLE, "Пиббл");
        addItem(ModItems.PLUSHY, "Плюшик");

    }
}
