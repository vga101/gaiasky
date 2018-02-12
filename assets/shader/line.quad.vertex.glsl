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

#ifdef relativisticEffects
    uniform vec3 u_velDir; // Velocity vector
    uniform float u_vc; // Fraction of the speed of light, v/c

    <INCLUDE shader/lib_geometry.glsl>
    <INCLUDE shader/lib_relativity.glsl>
#endif // relativisticEffects

void main() {
    vec4 pos = vec4(a_position);
    
    #ifdef relativisticEffects
        pos.xyz = computeRelativisticAberration(pos.xyz, length(pos.xyz), u_velDir, u_vc);
    #endif // relativisticEffects
    
    gl_Position = u_projModelView * pos;
    v_col = a_color;
    v_uv = a_uv;
}
