#version 450 core

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

layout(std430, binding = 0) buffer LightsPosBuffer {
    vec4 u_LightPosRadius[];
};

layout(std430, binding = 1) buffer LightsColorBuffer {
    vec4 u_LightColorIntensity[];
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
out vec3 blockLightColor;

void main() {
    vec3 worldPos = Position + ChunkOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(worldPos, 1.0);

    ivec2 LightUV = ivec2(UV2.x & 0xF0, UV2.y & 0xF0);
    vec4 lightmapSample = minecraft_sample_lightmap(Sampler2, LightUV);

    vec3 totalColored = vec3(0.0);

    for (int i = 0; i < u_light_count; i++) {
        vec3 lightPos = u_LightPosRadius[i].xyz;
        float radius = u_LightPosRadius[i].w;

        vec3 color = u_LightColorIntensity[i].rgb;
        float intensity = u_LightColorIntensity[i].a;

        vec3 delta = lightPos - worldPos;
        float dist = length(delta);

        if (dist < radius) {
            float falloff = 1.0 - (dist / radius);
            falloff = smoothstep(0.0, 1.0, falloff);
            float contribution = falloff * intensity;

            totalColored += color * contribution;
        }
    }

    blockLightColor = totalColored;

    vertexDistance = fog_distance(worldPos, FogShape);
    texCoord0 = UV0;
    vertexColor = Color * lightmapSample;
    vertexColor = vec4(worldPos,1);
}