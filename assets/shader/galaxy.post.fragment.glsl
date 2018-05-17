#version 330 core

uniform sampler2D tex_accum;
uniform sampler2D tex_reveal;

in vec2 TexCoord;
out vec4 FragColor;

void main() {
    vec4 accum = texture2D(tex_accum, TexCoord);
    float r = texture2D(tex_reveal, TexCoord).r;
    if(r == 1.0)
        discard;
    FragColor = vec4(accum.rgb / max(accum.a, 0.00001), 1.0 - r);
}
