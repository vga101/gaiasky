#version 120

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float u_ar;

varying vec4 v_col;
void main() {
    //vec2 uv = vec2(gl_PointCoord.s, gl_PointCoord.t);
    //uv.y = uv.y / u_ar;
    //float dist = min(1.0, distance(vec2(0.5), uv) * 2.0);
    //gl_FragColor = vec4(v_col.rgb + pow(1.0 - dist, 3.0), 1.0) * v_col.a * pow(1.0 - dist, 0.8);
   float alpha = v_col.a; 
    gl_FragColor = vec4(v_col.rgb * alpha, alpha);
}
