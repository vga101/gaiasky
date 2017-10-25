#version 120

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

/**
 * UNIFORMS
 */
uniform float u_ar;

/**
 * VARYINGS
 */
varying vec4 v_col;
varying float v_discard;

float programmatic(vec2 uv) {
    float dist_center = 1.0 - clamp(distance(vec2(0.5, 0.5), uv) * 2.0, 0.0, 1.0);
    return pow(dist_center, 6.0) * 0.4 + dist_center * 0.05;
}


void main() {
    if(v_discard > 0.0) {
        discard;
    }
    vec2 uv = vec2(gl_PointCoord.s, gl_PointCoord.t);
    uv.y = uv.y / u_ar;
    gl_FragColor = v_col * v_col.a * programmatic(uv);
    //gl_FragColor *= 0.95;
}
