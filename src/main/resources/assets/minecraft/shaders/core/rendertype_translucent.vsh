#version 450 core

#moj_import <light.glsl>
#moj_import <fog.glsl>
#moj_import <colored.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

layout(std430, binding = 0) buffer LightsBuffer {
    vec4 u_LightData[128];
};

uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;
uniform int u_light_count;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec4 blockLightColor;

void main() {
    vec3 worldPos = Position + ChunkOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(worldPos, 1.0);

    ivec2 LightUV = ivec2(UV2.x & 0xF0, UV2.y & 0xF0);
    vec4 lightmapSample = minecraft_sample_lightmap(Sampler2, LightUV);

    vertexColor = Color * lightmapSample;

    vec3 coloredLight = vec3(0.0);
    float coloredIntensity = 0.0;

    for (int i = 0; i < u_light_count; i++) {
        vec4 lightPosRad = u_LightData[i * 2];
        vec4 lightColorIntensity = u_LightData[i * 2 + 1];

        vec3 delta = lightPosRad.xyz - worldPos;
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

    blockLightColor = vec4(0.0);
    if (coloredIntensity > 0.0001) {
        float maxChannel = max(coloredLight.r,max(coloredLight.g,coloredLight.b));
        if (maxChannel > 1.0) {
            coloredLight /= maxChannel;
        }
        blockLightColor.rgb = coloredLight.rgb;
    }


    vertexDistance = fog_distance(worldPos, FogShape);
    texCoord0 = UV0;
}