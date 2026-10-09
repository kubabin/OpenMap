#version 150

uniform sampler2D Sampler0;
uniform float DayTime;

in vec2 texCoord0;

out vec4 fragColor;

void main() {
    fragColor = texture(Sampler0, texCoord0);
    if (fragColor.rgb == vec3(0.0, 0.0, 0.0)){
        discard;
    }
    float night = clamp(DayTime, 0.0, 1.0);
    float blockLight = clamp(fragColor.a, 0.0, 1.0);

    // Ambient brightness decreases at night
    float ambient = 1.0 - 0.65 * night;

    // Block light becomes more important at night
    float illumination = blockLight * night * 0.8;

    float brightness = min(1.0, ambient + illumination);

    fragColor.rgb *= brightness;
    fragColor.a = 1.0;
}
