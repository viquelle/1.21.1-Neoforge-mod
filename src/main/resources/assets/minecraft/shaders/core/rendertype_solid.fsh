#version 450

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 blockLightColor;

out vec4 fragColor;

void main() {
    vec4 albedo = texture(Sampler0, texCoord0);
    vec4 color;
    color = albedo * vertexColor;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
