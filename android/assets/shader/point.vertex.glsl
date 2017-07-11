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

// VARYINGs
varying vec4 v_col;

float lint2(float x, float x0, float x1, float y0, float y1) {
    return mix(y0, y1, (x - x0) / (x1 - x0));
}

float lint(float x, float x0, float x1, float y0, float y1) {
    return y0 + (y1 - y0) * smoothstep(x, x0, x1);
}

void main() {
    vec3 pos = a_position.xyz - u_camPos;

    // Proper motion
    vec3 pm = a_pm.xyz * u_t / 1000.0;     
    pos = pos + pm;
  
    float viewAngleApparent = atan((a_size * u_alphaSizeFovBr.w) / length(pos)) / u_alphaSizeFovBr.z;
    float opacity = pow(lint2(viewAngleApparent, 0.0, u_thAnglePoint, u_pointAlpha.x, u_pointAlpha.y), 1.2);
    //float opacity = pow(lint(viewAngleApparent, 0.0, u_thAnglePoint, u_pointAlpha.x, u_pointAlpha.y), 6.0);
    
    v_col = vec4(a_color.rgb, a_color.a * opacity * u_alphaSizeFovBr.x * smoothstep(u_thAnglePoint * 50.0, u_thAnglePoint, viewAngleApparent / 2.0));

    gl_Position = u_projModelView * vec4(pos, 0.0);
    gl_PointSize = u_alphaSizeFovBr.y;
}
