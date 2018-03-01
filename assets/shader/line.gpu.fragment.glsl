#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float u_alpha;

varying vec4 v_col;
void main() {
    float alpha = v_col.a * u_alpha;
    gl_FragColor = vec4(v_col.rgb + pow(alpha, 4.0) * 2.0, alpha);
}