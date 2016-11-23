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

// Time in seconds
varying float v_time;
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


#define time v_time * 0.003




void main() {
    // Perimeter is 1 when normal faces camera, 0 when normal is 90 degrees from view.
    float perimeter = dot(normalize(v_normal), vec3(v_viewVec));
    
    gl_FragColor = vec4(u_diffuseColor.rgb, u_diffuseColor.a * v_texCoords0.y);
}