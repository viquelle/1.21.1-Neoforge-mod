#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;
in vec4 blockLightColor;

out vec4 fragColor;

void main() {
    vec4 baseColor = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (blockLightColor != vec4(0)) {
        baseColor.rgb = baseColor.rgb * (1.75 * blockLightColor.rgb + vec3(1));
    }
    fragColor = baseColor;
}
