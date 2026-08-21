#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float Circular;
uniform vec2 MaskUvMin;
uniform vec2 MaskUvSize;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec2 maskUv = (texCoord0 - MaskUvMin) / MaskUvSize;
    if (Circular > 0.5 && distance(maskUv, vec2(0.5, 0.5)) > 0.5) {
        discard;
    }
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * ColorModulator;
}
