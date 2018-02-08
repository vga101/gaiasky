#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

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
uniform int u_relativsiticAberration; // Relativistic aberration flag
uniform vec3 u_velDir; // Velocity vector
uniform float u_vc; // Fraction of the speed of light, v/c

varying vec4 v_col;

<INCLUDE shader/lib_math.glsl>

<INCLUDE shader/lib_geometry.glsl>

void main() {
    vec3 pos = a_position.xyz - u_camPos;
    float dist = length(pos);
    
    if(u_relativsiticAberration == 1) {
        // Relativistic aberration
        // Current cosine of angle cos(th_s) cos A = DotProduct(v1, v2) / (Length(v1) * Length(v2))
        vec3 cdir = u_velDir * -1.0;
        float costh_s = dot(cdir, pos) / dist;
        float th_s = acos(costh_s);
        float costh_o = (costh_s - u_vc) / (1 - u_vc * costh_s);
        float th_o = acos(costh_o);
        pos = rotate_vertex_position(pos, normalize(cross(cdir, pos)), th_o - th_s);
    }
    
    float distNorm = dist / 800000000000.0;

    v_col = vec4(a_color.rgb, a_color.a);

    gl_Position = u_projModelView * vec4(pos, 0.0);
    gl_PointSize = a_additional.x / distNorm;
}
