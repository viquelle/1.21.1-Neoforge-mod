#version 330 core

uniform sampler2D DiffuseSampler0;
uniform float u_sanity;
uniform float u_time;

in vec2 texCoord;
out vec4 fragColor;

float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898,78.233))) * 43758.5453123);
}

void main()
{
    vec2 uv = texCoord;
    vec4 color = texture(DiffuseSampler0, uv);

    float sanity = clamp(u_sanity, 0.0, 1.0);
    float effectIntensity = clamp((0.8 - sanity) / 0.8, 0.0, 1.0);
    float distortionIntensity = clamp((0.8 - sanity) / 0.8, 0.0, 1.0);
    // Постеризация
    float levels = mix(256.0, 32.0, effectIntensity);
    color.rgb = floor(color.rgb * levels) / levels;

    // Ghost
    vec2 center = uv - 0.5;
    float edgemask = smoothstep(0., 0.8, length(center));
    vec2 offset = vec2(
    sin(u_time) * 0.05,
    cos(u_time) * 0.04
) * edgemask * distortionIntensity;

    vec4 ghostColor = texture(DiffuseSampler0, uv + offset);
    ghostColor.rgb = floor(ghostColor.rgb * levels) / levels;
    float ghostAlpha = 1. * edgemask * 1.5 * distortionIntensity;
    vec3 finalColor = mix(color.rgb, ghostColor.rgb, ghostAlpha);

    float desaturate = mix(1.0f, 0.3f, effectIntensity);
    float luma = dot(finalColor.rgb, vec3(0.299, 0.587, 0.114));

    finalColor.rgb = mix(vec3(luma), finalColor.rgb, desaturate );

    float noise = random((floor((uv) * 256.) / 256.) * u_time / 1000.) * 0.15 * effectIntensity;
    finalColor.rgb *= vec3(noise + 1.);
    finalColor = clamp(finalColor, 0.0, 1.0);
    fragColor = vec4(finalColor, 1.0);
}