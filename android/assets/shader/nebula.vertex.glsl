#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

attribute vec4 a_position;
attribute vec4 a_normal;
attribute vec2 a_texCoord0;
attribute vec2 a_additional;

uniform mat4 u_projModelView;
uniform vec3 u_camPos;
uniform float u_alpha;
uniform int u_relativsiticAberration; // Relativistic aberration flag
uniform vec3 u_velDir; // Velocity vector
uniform float u_vc; // Fraction of the speed of light, v/c

varying vec2 v_texCoords;
varying float v_alpha;
varying float v_textureNumber;

<INCLUDE shader/lib_math.glsl>

<INCLUDE shader/lib_geometry.glsl>

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
   
   if(u_relativsiticAberration == 1) {
       // Relativistic aberration
       // Current cosine of angle cos(th_s) cos A = DotProduct(v1, v2) / (Length(v1) * Length(v2))
       vec3 cdir = u_velDir * -1.0;
       float costh_s = dot(cdir, pos) / length(pos);
       float th_s = acos(costh_s);
       float costh_o = (costh_s - u_vc) / (1 - u_vc * costh_s);
       float th_o = acos(costh_o);
       pos = rotate_vertex_position(pos, normalize(cross(cdir, pos)), th_o - th_s);
   }
   
   // Position
   gl_Position = u_projModelView * vec4(pos, 0.0);
}
