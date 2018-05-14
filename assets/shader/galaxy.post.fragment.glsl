uniform sampler2D tex_accumulation;
uniform sampler2D tex_revealage;

varying vec2 v_texcoord;

void main(void)
{
    vec4 accum = texture2D(tex_accumulation, v_texcoord);
    float r = texture2D(tex_revealage, v_texcoord).r;
    if(r == 1.0)
        discard;
    gl_FragColor = vec4(accum.rgb / max(accum.a, 0.00001), 1-r);
}
