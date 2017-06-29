#version 120

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float u_ar;
varying vec4 v_col;

void main() {
	vec2 uv = vec2(gl_PointCoord.s, gl_PointCoord.t);
	uv.y = uv.y / u_ar;
	float dist = 1.0 - distance(vec2(0.5), uv) * 2.0;
    gl_FragColor = v_col * v_col.a * max(pow(dist, 10.0), dist / 150.0 );
    //gl_FragColor *= 0.95;
}
