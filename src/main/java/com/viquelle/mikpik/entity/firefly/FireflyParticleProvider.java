package com.viquelle.mikpik.entity.firefly;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;

public class FireflyParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprite;

    public FireflyParticleProvider(SpriteSet sprite) { this.sprite = sprite; }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        FireflyParticle p = new FireflyParticle(level, x, y, z, (int) xSpeed);
        p.pickSprite(this.sprite);
        return p;
    }
}