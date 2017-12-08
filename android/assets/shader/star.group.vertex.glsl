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

float lint2(float x, float x0, float x1, float y0, float y1) {
    return mix(y0, y1, (x - x0) / (x1 - x0));
}

float lint(float x, float x0, float x1, float y0, float y1) {
    return y0 + (y1 - y0) * smoothstep(x, x0, x1);
}

// Returns >=0 if visible, <0 if not visible 
float in_view(vec3 pos, vec3 dir, float dist, float angle_edge) {
    return angle_edge - acos(dot(pos, dir) / dist);
}

void main() {
    vec3 pos = a_position - u_camPos;

    float dist = length(pos);
    
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
    
    // Proper motion
    pos = pos + a_pm * float(u_t) * day_to_year;

    float viewAngleApparent = atan((a_size * u_alphaSizeFovBr.w) / dist) / u_alphaSizeFovBr.z;
    float opacity = pow(lint2(viewAngleApparent, 0.0, u_thAnglePoint, u_pointAlpha.x, u_pointAlpha.y), 1.2);

    float fadeout = smoothstep(dist, len0, len1);
    v_col = vec4(a_color.rgb, opacity * u_alphaSizeFovBr.x * fadeout);

    gl_Position = u_projModelView * vec4(pos, 0.0);
    gl_PointSize = u_alphaSizeFovBr.y;
}
