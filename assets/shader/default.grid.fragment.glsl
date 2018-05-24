// UNIFORMS

// Diffuse color
uniform vec4 u_diffuseColor;

// VARYINGS

// Coordinate of the texture
varying vec2 v_texCoords0;
// Opacity
varying float v_opacity;
// Color
varying vec4 v_color;

#define PI 3.141592

vec4 spherical(vec2 tc) {
    vec2 coord = tc;
    coord.x *= 36.0 * 2.0;
    coord.y *= 18.0 * 2.0;
    
    // Normalize in 1..0..1
    vec2 norm = abs((tc - 0.5) * 2.0);
    
    float highlight = (smoothstep(0.001, 0.0, norm.x) + smoothstep(0.999, 1.0, norm.x) + smoothstep(0.001, 0.0, norm.y)) * 0.35;
    
    coord = cos(PI * coord);
    vec4 result = u_diffuseColor * smoothstep(0.998, 1.0, max(coord.x, coord.y));
    result = clamp(result + highlight, 0.0, 1.0);
    result.a *= pow(1.0 - abs((v_texCoords0.y * 2.0) - 1.0), 0.25) * v_opacity;
    return result;
}

void main() {
    gl_FragColor = spherical(v_texCoords0);
}