#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

attribute vec4 a_position;
attribute vec4 a_color;

uniform mat4 u_projModelView;
uniform vec2 u_viewport;
uniform vec3 u_parentPos;

varying vec4 v_col;

void main() {
   vec4 pos = vec4(a_position.xyz - u_parentPos, a_position.w);
   gl_Position = u_projModelView * pos;
   v_col = a_color;
   //v_col.a *= 0.6;
}
