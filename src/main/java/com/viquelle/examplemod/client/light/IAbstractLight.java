package com.viquelle.examplemod.client.light;

public interface IAbstractLight {
    void tick(float partialTick);
    void unregister();
    void register();
}
