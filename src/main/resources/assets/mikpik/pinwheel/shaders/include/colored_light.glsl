vec4 getBlockLightColor(vec3 position, int count, vec4 LightData[64], vec4 LightColor[64]) {
    vec3 coloredLight = vec3(0.0);
    float coloredIntensity = 0.0;

    for (int i = 0; i < count; i++) {
        vec4 lightPosRad = LightData[i];
        vec4 lightColorIntensity = LightColor[i];

        vec3 delta = lightPosRad.xyz - position;
        float dist = length(delta);

        if (dist < lightPosRad.w) {
            float falloff = 1.0 - (dist / lightPosRad.w);
            falloff = smoothstep(0.0, 1.0, falloff);
            falloff *= falloff;

            float intensity = falloff * lightColorIntensity.a;

            coloredLight += lightColorIntensity.rgb * intensity;
            coloredIntensity += intensity;
        }
    }

    vec4 result = vec4(0.0);
    if (coloredIntensity > 0.0001) {
        float maxChannel = max(coloredLight.r, max(coloredLight.g, coloredLight.b));
        if (maxChannel > 1.0) {
            coloredLight /= maxChannel;
        }
        result = vec4(coloredLight,1);
    }
    return result;
}