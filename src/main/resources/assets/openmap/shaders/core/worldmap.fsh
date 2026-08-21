#version 150

uniform sampler2D Sampler0;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    fragColor = texture(Sampler0, texCoord0);
    if (fragColor.rgb == vec3(0.0, 0.0, 0.0)){
        discard;
    }
    fragColor.a = 1.0;
}
