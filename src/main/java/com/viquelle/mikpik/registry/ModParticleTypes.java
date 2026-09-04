package com.viquelle.mikpik.registry;

import com.viquelle.mikpik.MikpikMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, MikpikMod.MODID);

    public static final Supplier<SimpleParticleType> FIREFLY =
            PARTICLE_TYPES.register("firefly", () -> new SimpleParticleType(false));

}
