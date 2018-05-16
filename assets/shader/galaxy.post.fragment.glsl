uniform sampler2D tex_accum;
uniform sampler2D tex_reveal;

varying vec2 v_texcoord;

void main() {
    vec4 accum = texture2D(tex_accum, v_texcoord);
    float r = texture2D(tex_reveal, v_texcoord).r;
    if(r == 1.0)
        discard;
    gl_FragColor = vec4(accum.rgb / max(accum.a, 0.00001), 1.0 - r);
}
