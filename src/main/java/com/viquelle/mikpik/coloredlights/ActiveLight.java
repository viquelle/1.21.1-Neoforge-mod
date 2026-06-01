package com.viquelle.mikpik.coloredlights;

public record ActiveLight(
        long id,
        float x,
        float y,
        float z,
        float radius,
        float r,
        float g,
        float b,
        float intensity
) {
    public double distanceSq(double px, double py, double pz) {
        double dx = x - px;
        double dy = y - py;
        double dz = z - pz;

        return dx * dx + dy * dy + dz * dz;
    }
}