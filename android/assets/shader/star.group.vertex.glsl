#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// ATTRIBUTES
attribute vec4 a_position;
attribute vec4 a_pm;
attribute vec4 a_color;
attribute float a_size;


// UNIFORMS
uniform float u_t; // time in ms since J2000
uniform mat4 u_projModelView;
uniform vec3 u_camPos;
uniform vec2 u_pointAlpha;
uniform float u_thAnglePoint;
// 0 - alpha
// 1 - point size
// 2 - fov factor
// 3 - star brightness
uniform vec4 u_alphaSizeFovBr;

// VARYINGS
varying vec4 v_col;
varying float v_discard;

#define len0 170000.0
#define len1 len0 * 100.0

float lint2(float x, float x0, float x1, float y0, float y1) {
    return mix(y0, y1, (x - x0) / (x1 - x0));
}

float lint(float x, float x0, float x1, float y0, float y1) {
    return y0 + (y1 - y0) * smoothstep(x, x0, x1);
}

void main() {
    vec3 pos = a_position.xyz - u_camPos;

    float dist = length(pos);
    float fadeout = smoothstep(dist, len0, len1);
    if(dist < len0) {
        v_discard = 1.0;
    } else {
        v_discard = -1.0;
    }
    
    // Proper motion
//    vec3 pm = a_pm.xyz * u_t / 1000.0;
//    pos = pos + pm;

    float viewAngleApparent = atan((a_size * u_alphaSizeFovBr.w) / dist) / u_alphaSizeFovBr.z;
    float opacity = pow(lint2(viewAngleApparent, 0.0, u_thAnglePoint, u_pointAlpha.x, u_pointAlpha.y), 1.2);
    //float opacity = pow(lint(viewAngleApparent, 0.0, u_thAnglePoint, u_pointAlpha.x * 2.0, u_pointAlpha.y * 2.0), 6.0);

    v_col = vec4(a_color.rgb, opacity * u_alphaSizeFovBr.x * fadeout);

    gl_Position = u_projModelView * vec4(pos, 0.0);
    gl_PointSize = u_alphaSizeFovBr.y;
}
