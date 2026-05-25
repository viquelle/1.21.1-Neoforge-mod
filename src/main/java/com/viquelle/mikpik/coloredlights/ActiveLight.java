package com.viquelle.mikpik.coloredlights;

public record ActiveLight(
        double x,
        double y,
        double z,
        float radius,
        float r,
        float g,
        float b,
        float intensity
) {}