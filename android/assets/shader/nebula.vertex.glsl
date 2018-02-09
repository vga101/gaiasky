#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

<INCLUDE shader/lib_math.glsl>
<INCLUDE shader/lib_geometry.glsl>

attribute vec4 a_position;
attribute vec4 a_normal;
attribute vec2 a_texCoord0;
attribute vec2 a_additional;

uniform mat4 u_projModelView;
uniform vec3 u_camPos;
uniform float u_alpha;

#ifdef relativisticEffects
    uniform vec3 u_velDir; // Velocity vector
    uniform float u_vc; // Fraction of the speed of light, v/c

    <INCLUDE shader/lib_relativity.glsl>
#endif // relativisticEffects

varying vec2 v_texCoords;
varying float v_alpha;
varying float v_textureNumber;

void main()
{
   // Tex coordinates
   v_texCoords = a_texCoord0;
   
   // Num texture
   v_textureNumber = a_additional.x;
 
   // Correct position with camera
   vec3 pos = a_position.xyz - u_camPos;
   
   // cross product to get angle
   float a = abs(dot(normalize(pos), a_normal.xyz));
   v_alpha = clamp(u_alpha * a * a_additional.y, 0.0, 1.0);
   
   #ifdef relativisticEffects
       pos = computeRelativisticAberration(pos, length(pos), u_velDir, u_vc);
   #endif // relativisticEffects
   
   // Position
   gl_Position = u_projModelView * vec4(pos, 0.0);
}
