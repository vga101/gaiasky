#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

attribute vec4 a_position;
attribute vec4 a_color;
attribute vec2 a_uv;

uniform mat4 u_projModelView;
uniform vec2 u_viewport;

varying vec4 v_col;
varying vec2 v_uv;

uniform int u_relativsiticAberration; // Relativistic aberration flag
uniform vec3 u_velDir; // Velocity vector
uniform float u_vc; // Fraction of the speed of light, v/c

<INCLUDE shader/lib_geometry.glsl>

void main() {
    vec4 pos = vec4(a_position);
    if(u_relativsiticAberration == 1) {
        // Relativistic aberration
        // Current cosine of angle cos(th_s) cos A = DotProduct(v1, v2) / (Length(v1) * Length(v2))
        vec3 cdir = u_velDir * -1.0;
        float costh_s = dot(cdir, pos.xyz) / length(pos.xyz);
        float th_s = acos(costh_s);
        float costh_o = (costh_s - u_vc) / (1.0 - u_vc * costh_s);
        float th_o = acos(costh_o);
        pos.xyz = rotate_vertex_position(pos.xyz, normalize(cross(cdir, pos.xyz)), th_o - th_s);
    }
    
    gl_Position = u_projModelView * pos;
    v_col = a_color;
    v_uv = a_uv;
}
