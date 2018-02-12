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

varying vec2 v_texCoords;
varying float v_alpha;
varying float v_textureNumber;

float lint(float x, float x0, float x1, float y0, float y1) {
    return mix(y0, y1, (x - x0) / (x1 - x0));
}

void main()
{
   // Tex coordinates
   v_texCoords = a_texCoord0;
   
   // Num texture
   v_textureNumber = a_additional.x;
 
   // Correct position with camera
   vec4 pos = a_position;
   pos -= vec4(u_camPos, 0.0);
   
   // cross product to get angle
   float a = abs(dot(normalize(pos), a_normal));
   v_alpha = clamp(u_alpha * a * a_additional.y, 0.0, 1.0);
   
   // Position
   gl_Position = u_projModelView * pos;
}
