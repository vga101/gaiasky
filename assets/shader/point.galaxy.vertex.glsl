#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

<INCLUDE shader/lib_math.glsl>
<INCLUDE shader/lib_geometry.glsl>

attribute vec4 a_position;
attribute vec4 a_color;
// x - size, y - th_angle_point
attribute vec4 a_additional;

uniform float u_pointAlphaMin;
uniform float u_pointAlphaMax;
uniform float u_fovFactor;
uniform float u_starBrightness;
uniform mat4 u_projModelView;
uniform vec3 u_camPos;

#ifdef relativisticEffects
    uniform vec3 u_velDir; // Velocity vector
    uniform float u_vc; // Fraction of the speed of light, v/c

    <INCLUDE shader/lib_relativity.glsl>
#endif // relativisticEffects

varying vec4 v_col;

void main() {
    vec3 pos = a_position.xyz - u_camPos;
    float dist = length(pos);
    
    #ifdef relativisticEffects
        pos = computeRelativisticAberration(pos, dist, u_velDir, u_vc);
    #endif // relativisticEffects
    
    float distNorm = dist / 800000000000.0;

    v_col = vec4(a_color.rgb, a_color.a);

    gl_Position = u_projModelView * vec4(pos, a_position.w);
    gl_PointSize = a_additional.x / distNorm;
}
