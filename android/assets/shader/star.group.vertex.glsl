#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// ATTRIBUTES
attribute vec3 a_position;
attribute vec3 a_pm;
attribute vec4 a_color;
attribute float a_size;


// UNIFORMS
uniform int u_t; // time in days since epoch
uniform mat4 u_projModelView;
uniform vec3 u_camPos;

uniform vec2 u_pointAlpha;
uniform float u_thAnglePoint;

#ifdef relativistcEffects
    uniform vec3 u_velDir; // Velocity vector
    uniform float u_vc; // Fraction of the speed of light, v/c
#endif // relativistcEffects

// 0 - alpha
// 1 - point size
// 2 - fov factor
// 3 - star brightness
uniform vec4 u_alphaSizeFovBr;
// Fov observation
uniform int u_fovcam; // 0.0 if regular camera, >0 if fov (1.0, 2.0 or 3.0)
uniform float u_fovcam_angleedge;
uniform vec3 u_fovcam_dir;

// VARYINGS
varying vec4 v_col;
varying float v_discard;

#define len0 170000.0
#define len1 len0 * 100.0
#define day_to_year 1.0 / 365.25


<INCLUDE shader/lib_math.glsl>
<INCLUDE shader/lib_geometry.glsl>

void main() {
    vec3 pos = a_position - u_camPos;
    // Proper motion
    pos = pos + a_pm * float(u_t) * day_to_year;
    
    // Distance to star
    float dist = length(pos);
    
    #ifdef relativistcEffects
        // Relativistic aberration
        // Current cosine of angle cos(th_s) cos A = DotProduct(v1, v2) / (Length(v1) * Length(v2))
        vec3 cdir = u_velDir * -1.0;
        float costh_s = dot(cdir, pos) / dist;
        float th_s = acos(costh_s);
        float costh_o = (costh_s - u_vc) / (1 - u_vc * costh_s);
        float th_o = acos(costh_o);
        pos = rotate_vertex_position(pos, normalize(cross(cdir, pos)), th_o - th_s);
    #endif // relativisticEffects
    
    
    // Compute fov observation if necessary (only Fov1, Fov2)
    float observed = 1.0;
    if(u_fovcam > 0) {
        observed = in_view(pos, u_fovcam_dir, dist, u_fovcam_angleedge);
    }
    
    // Discard vertex if too close or Gaia Fov1or2 and not observed    
    if(dist < len0 || observed < 0.0) {
        v_discard = 1.0;
    } else {
        v_discard = -1.0;
    }
    
    float viewAngleApparent = atan((a_size * u_alphaSizeFovBr.w) / dist) / u_alphaSizeFovBr.z;
    float opacity = pow(lint2(viewAngleApparent, 0.0, u_thAnglePoint, u_pointAlpha.x, u_pointAlpha.y), 1.2);

    float fadeout = smoothstep(dist, len0, len1);
    v_col = vec4(a_color.rgb, opacity * u_alphaSizeFovBr.x * fadeout);

    gl_Position = u_projModelView * vec4(pos, 0.0);
    gl_PointSize = u_alphaSizeFovBr.y;
}
