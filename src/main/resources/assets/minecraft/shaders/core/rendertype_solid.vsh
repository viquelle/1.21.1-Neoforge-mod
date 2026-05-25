#version 450

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position; // Позиция вершины(в чанке)
in vec4 Color; // цвет хз от чего, биом?
in vec2 UV0; // Текстурные координаты
in ivec2 UV2; // координаты для LightMap
in vec3 Normal; // Вектор нормали

layout(std430, binding = 0) buffer LightsPosBuffer {
    vec4 u_LightPosRadius[];
};

layout(std430, binding = 1) buffer LightsColorBuffer {
    vec4 u_LightColorIntensity[];
};

uniform sampler2D Sampler2; // LightMap

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec3 ChunkOffset;
uniform int FogShape;
uniform int u_light_count;

out float vertexDistance; // расстояние для тумана
out vec4 vertexColor; // цвет с освещением
out vec2 texCoord0; // текстурные координаты
out vec3 blockLightColor; // подсветка от блоков

vec3 tonemap(vec3 c) {
    float l = dot(c, vec3(0.2126,0.7152,0.0722));
    vec3 cc = c / (c + 1.0);
    return mix(c / (l + 1.0), cc, cc);
}

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

    blockLightColor = u_LightColorIntensity[0].rgb;
    vertexDistance = fog_distance(worldPos, FogShape);
    texCoord0 = UV0;
    vertexColor = Color * lightmapSample * vec4(blockLightColor,1);
}
