#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

<INCLUDE shader/lib_math.glsl>
<INCLUDE shader/lib_geometry.glsl>

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

#ifdef relativisticEffects
    uniform vec3 u_velDir; // Velocity vector
    uniform float u_vc; // Fraction of the speed of light, v/c

    <INCLUDE shader/lib_relativity.glsl>
#endif // relativisticEffects


void main() {
    vec3 pos = a_position.xyz - u_camPos;

    // Proper motion
    vec3 pm = a_pm.xyz * u_t / 1000.0;     
    pos = pos + pm;
    
    float dist = length(pos);
    
    #ifdef relativisticEffects
        pos = computeRelativisticAberration(pos, dist, u_velDir, u_vc);
    #endif // relativisticEffects
  
    float viewAngleApparent = atan((a_size * u_alphaSizeFovBr.w) / dist) / u_alphaSizeFovBr.z;
    float opacity = pow(lint2(viewAngleApparent, 0.0, u_thAnglePoint, u_pointAlpha.x, u_pointAlpha.y), 1.2);
    //float opacity = pow(lint(viewAngleApparent, 0.0, u_thAnglePoint, u_pointAlpha.x, u_pointAlpha.y), 6.0);
    
    v_col = vec4(a_color.rgb, a_color.a * opacity * u_alphaSizeFovBr.x * smoothstep(u_thAnglePoint * 50.0, u_thAnglePoint, viewAngleApparent / 2.0));

    gl_Position = u_projModelView * vec4(pos, a_position.w);
    gl_PointSize = u_alphaSizeFovBr.y;
}
