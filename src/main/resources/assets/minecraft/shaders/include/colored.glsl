#version 450

float luminance(vec3 color) {
    return dot(color, vec3(0.2126, 0.7152, 0.0722));
}

vec3 toTargetLuminance(vec3 color, float l) {
    float cl = luminance(color);
    color = color / cl * l;
    return color;
}