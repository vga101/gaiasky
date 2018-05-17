#version 330 core

uniform float u_ar;
uniform float u_alpha;
uniform int u_blending = 1;

in vec4 v_col;
out vec4 FragColor;

void main() {
    vec2 uv = vec2(gl_PointCoord.s, gl_PointCoord.t);
    uv.y = uv.y / u_ar;
    float dist = distance(vec2(0.5), uv) * 2.0;
    if(dist > 0.9){
        discard;
    }
    float profile = pow(1.0 - dist, 4.0);
    if(u_blending == 0) {
        // Default blending
        FragColor = vec4(v_col.rgb, v_col.a * u_alpha * profile);
        // FragColor = vec4(v_col.rgb, 1.0);
    } else {
        // Additive blending
        FragColor = v_col * u_alpha * profile;
    }
}
