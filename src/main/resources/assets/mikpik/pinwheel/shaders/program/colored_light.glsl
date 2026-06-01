vec4 getBlockLightColor(vec3 position, int count, vec4 LightData[256], vec4 LightColor[256]) {
    vec3 coloredLight = vec3(0.0);

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

        coloredLight += lightColorIntensity.rgb * intensity;
    }

    float maxChannel = max(coloredLight.r, max(coloredLight.g, coloredLight.b));
    if (maxChannel > 1.0) {
        coloredLight /= maxChannel;
    }
    return vec4(coloredLight,1.0);
}