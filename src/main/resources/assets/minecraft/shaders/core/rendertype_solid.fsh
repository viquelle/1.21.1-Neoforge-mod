#version 450 core

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
    vec4 baseColor = albedo * vertexColor;
    vec3 finalColor = mix(baseColor.rgb, blockLightColor, 0.5);
    finalColor = min(finalColor, vec3(1.0));
    fragColor = linear_fog(vec4(finalColor,baseColor.a), vertexDistance, FogStart, FogEnd, FogColor);
}
