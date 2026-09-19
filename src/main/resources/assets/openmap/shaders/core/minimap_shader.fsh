#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float Circular;
uniform vec2 MaskUvMin;
uniform vec2 MaskUvSize;
uniform vec2 UVOffset;
uniform float angle;

in vec2 texCoord0;

out vec4 fragColor;

vec2 rotateUV(vec2 uv)
{
    float c = cos(angle);
    float s = sin(angle);

    // Move origin to center
    uv -= vec2(0.5);

    // Rotate
    uv = mat2(c, -s,
    s,  c) * uv;

    // Move origin back
    uv += vec2(0.5);

    return uv;
}

void main() {
    vec2 maskUv = (texCoord0 - MaskUvMin) / MaskUvSize;
    if (Circular > 0.5 && distance(maskUv, vec2(0.5, 0.5)) > 0.5) {
        discard;
    }
    vec2 texCoord = rotateUV(texCoord0) + UVOffset;
    texCoord = clamp(texCoord, vec2(0.0), vec2(1.0));
    vec4 color = texture(Sampler0, texCoord);
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * ColorModulator;
}
