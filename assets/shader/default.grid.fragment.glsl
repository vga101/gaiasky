#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// UNIFORMS

// Diffuse base texture
uniform sampler2D u_diffuseTexture;
// Grayscale lookup table
uniform sampler2D u_normalTexture;
// Diffuse color
uniform vec4 u_diffuseColor;

// VARYINGS

// Ambient color (star color in this case)
varying vec3 v_lightDiffuse;
// The normal
varying vec3 v_normal;
// Coordinate of the texture
varying vec2 v_texCoords0;
// Opacity
varying float v_opacity;
// View vector
varying vec3 v_viewVec;
// Color
varying vec4 v_color;

#define PI 3.141592

void main() {
    vec2 coord = v_texCoords0;
    coord.x *= 36.0 * 2.0;
    coord.y *= 18.0 * 2.0;
    coord = cos(PI * coord);
    gl_FragColor = u_diffuseColor * smoothstep(0.997, 1.0, max(coord.x, coord.y));
    gl_FragColor.a *= pow(1.0 - abs((v_texCoords0.y * 2.0) - 1.0), 0.25) * v_opacity;
}