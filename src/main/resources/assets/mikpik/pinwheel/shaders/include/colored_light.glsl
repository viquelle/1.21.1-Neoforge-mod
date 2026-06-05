vec4 getBlockLightColor(vec3 position, int count, vec4 LightData[256], vec4 LightColor[256]) {
    vec4 coloredLight = vec4(0.0);

    for (int i = 0; i < count; i++) {
        vec4 lightPosRad = LightData[i];
        vec4 lightColorIntensity = LightColor[i];

        vec3 delta = lightPosRad.xyz - position;

        float radius = lightPosRad.w;
        float radiusSq = radius * radius;
        float distSq = dot(delta,delta);

        if (distSq > radiusSq) {
            continue;
        }

        float falloff = 1.0 - (distSq / radiusSq);
        falloff *= falloff;

        float intensity = falloff * lightColorIntensity.a;

        coloredLight.rgb += lightColorIntensity.rgb * intensity;
        coloredLight.a += intensity;
    }

    return vec4(coloredLight.rgb, coloredLight.a);
}