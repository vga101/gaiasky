#version 120

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float u_ar;
uniform float u_alpha;
varying vec4 v_col;

void main() {
	vec2 uv = vec2(gl_PointCoord.s, gl_PointCoord.t);
	uv.y = uv.y / u_ar;
	float dist = distance(vec2(0.5), uv) * 2.0;
	if(dist > 0.9){
		discard;
	}
    gl_FragColor = v_col * u_alpha * pow(1.0 - dist, 6.0);
}
