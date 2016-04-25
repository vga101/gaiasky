#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

varying vec4 v_col;
void main() {
	float dist = distance(vec2(0.5), gl_PointCoord.st) * 2.0;
	if(dist > 0.9){
		discard;
	}
    gl_FragColor = vec4(v_col.rgb, v_col.a * pow(1.0 - dist, 6.0));
}