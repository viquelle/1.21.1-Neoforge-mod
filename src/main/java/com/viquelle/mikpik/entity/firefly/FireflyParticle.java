package com.viquelle.mikpik.entity.firefly;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;

public class FireflyParticle extends TextureSheetParticle {
    private final double startX, startY, startZ;
    private final float phaseOffset;
    private final float radiusVar;
    private final float speedVar;

    protected FireflyParticle(ClientLevel level, double x, double y, double z, int phase) {
        super(level, x, y, z);
        this.startX = x;
        this.startY = y;
        this.startZ = z;

        float random1 = level.random.nextFloat();
        float random2 = level.random.nextFloat();

        this.phaseOffset = (phase * 2.094f) + (random1 * 6.283f);
        this.radiusVar = random2;
        this.speedVar = 0.6f + (random1 * 0.4f);

        this.lifetime = 100;

        this.setColor(195/255f, 255/255f, 173/255f);
        this.setAlpha(0);
        this.setSize(0.1f, 0.1f);

        // Начальная позиция (небольшой разброс вокруг якоря)
        float startRadius = 0.3f + (this.radiusVar * 0.4f);
        this.x = this.startX + Mth.cos(this.phaseOffset) * startRadius;
        this.y = this.startY;
        this.z = this.startZ + Mth.sin(this.phaseOffset) * startRadius;

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        // Основное медленное вращение вокруг якоря
        float mainAngle = this.phaseOffset + (this.age * 0.02f * this.speedVar);
        float mainRadius = 0.8f + (Mth.sin(this.phaseOffset * 3.0f) * 0.3f);

        // Малое вращение вокруг своей оси
        float localAngle = (this.phaseOffset * 5.0f) + (this.age * 0.08f);
        float localRadius = 0.2f + (this.radiusVar * 0.15f);

        // Плавный полет вверх + легкое вертикальное покачивание
        float upwardDrift = this.age * 0.015f; // Подъем примерно на 1.5 блока за время жизни
        float bob = Mth.sin(this.age * 0.06f + this.phaseOffset) * 0.15f;

        // Суммируем все смещения
        double newX = this.startX + (Mth.cos(mainAngle) * mainRadius) + (Mth.cos(localAngle) * localRadius);
        double newY = this.startY + upwardDrift + bob;
        double newZ = this.startZ + (Mth.sin(mainAngle) * mainRadius) + (Mth.sin(localAngle) * localRadius);

        this.setPos(newX, newY, newZ);

        if (this.age < 20) {
            this.alpha = this.age / 20f;
        } else if (this.age > this.lifetime - 20) {
            this.alpha = (this.lifetime - this.age) / 20f;
        } else {
            this.alpha = 1.0f;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }
}