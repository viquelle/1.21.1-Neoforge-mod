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
in vec4 blockLightColor;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (baseColor.a < 0.5) {
        discard;
    }

    if (blockLightColor != vec4(0)) {
        baseColor.rgb = baseColor.rgb * (1.75 * blockLightColor.rgb + vec3(1));
    }
    fragColor = linear_fog(baseColor, vertexDistance, FogStart, FogEnd, FogColor);
}
