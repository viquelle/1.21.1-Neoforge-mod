#version 330 core

uniform sampler2D DiffuseSampler0;
uniform float u_sanity;
uniform float u_time;

in vec2 texCoord;
out vec4 fragColor;

void main()
{
    vec2 uv = texCoord;
    vec4 color = texture(DiffuseSampler0, uv);

    float sanity = clamp(u_sanity, 0.0, 1.0);
    // 0 при sanity >= 0.7, плавно растёт, достигает 1.0 при sanity <= 0.2
    float effectIntensity = clamp((0.7 - sanity) / 0.5, 0.0, 1.0);

    // 0 при sanity >= 0.5, плавно растёт, достигает 1.0 при sanity <= 0.2
    float distortionIntensity = clamp((0.5 - sanity) / 0.3, 0.0, 1.0);

    // Постеризация зависит от общей интенсивности
    float levels = mix(256.0, 16.0, effectIntensity);
    color.rgb = floor(color.rgb * levels) / levels;

    vec2 center = uv - 0.5;
    float edgemask = smoothstep(0.0, 1., length(center));

    // ghost-эффект зависит от интенсивности искажения
    vec2 offset = vec2(
    sin(u_time * 0.20) * 0.025,
    cos(u_time * 0.16) * 0.02
    ) * edgemask * distortionIntensity;

    vec4 ghostColor = texture(DiffuseSampler0, uv + offset);
    ghostColor.rgb = floor(ghostColor.rgb * levels) / levels;

    float ghostAlpha = 0.8 * edgemask * distortionIntensity;
    vec3 finalColor = mix(color.rgb, ghostColor.rgb, ghostAlpha);

    fragColor = vec4(finalColor, 1.0);
}