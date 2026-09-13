#version 330

uniform sampler2D Sampler0;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    float color = texture(Sampler0, texCoord0).r / 256;
    fragColor.rgb = vec3(color, color, color);
    if (fragColor.rgb == vec3(0.0, 0.0, 0.0)){
        discard;
    }
    fragColor.a = 1.0;
}
