#version 330 core

#ifdef GL_ES
	#define PRECISION mediump
	precision PRECISION float;
#else
	#define PRECISION
#endif

attribute vec4 a_position;
attribute vec2 a_texCoord0;

out vec2 TexCoord;

void main()
{
	TexCoord = a_texCoord0;
	gl_Position = a_position;
}