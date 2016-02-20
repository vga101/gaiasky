#version 120

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 v_col;
void main() {
	float dist = distance(vec2(0.5), gl_PointCoord.st) * 2.0;
    gl_FragColor = vec4(v_col.rgb, v_col.a * pow(1.0 - dist, 4.0));
}