#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float u_alpha;

varying vec4 v_col;
void main() {
    float alpha = v_col.a * u_alpha;
    gl_FragColor = vec4(clamp(v_col.rgb + vec3(0.5), 0.0, 1.0), alpha);
}