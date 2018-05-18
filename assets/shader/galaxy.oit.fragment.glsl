#version 120

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float u_ar;
uniform float u_alpha;

varying float v_depth;
varying vec4 v_col;

void main() {
    vec2 uv = vec2(gl_PointCoord.s, gl_PointCoord.t);
    uv.y = uv.y / u_ar;
    float dist = distance(vec2(0.5), uv) * 2.0;
    if(dist > 0.9){
        discard;
    }
    float profile = pow(1.0 - dist, 4.0) + (1.0 - dist) * 0.5;
    float alpha = v_col.a * u_alpha * profile;
    float w = alpha * pow(1.0 - v_depth, 3.0);
    // accum
    gl_FragData[0] = vec4(v_col.rgb, alpha);
    // reveal
    gl_FragData[1].r = alpha;
}
