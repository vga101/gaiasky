#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// v_texCoords are UV coordinates in [0..1]
varying vec2 v_texCoords;
varying vec4 v_color;

uniform sampler2D u_noiseTexture;

uniform float u_th_dist_up;
uniform float u_apparent_angle;
uniform float u_inner_rad;
uniform float u_time;
// Distance in km to the star
uniform float u_distance;
// Is a star
uniform float u_star;
// Whether to draw stray light or not
uniform int u_strayLight;

#define time u_time * 0.001
// Angle threshold. If angle is smaller, we don't draw core. To avoid flickering
#define ang_th 0.00000001

float noise(float t){
    return texture2D(u_noiseTexture, vec2(t, .0)).x;
}

vec3 cc(vec3 color, float factor,float factor2) // color modifier
{
    float w = color.x+color.y+color.z;
    return mix(color,vec3(w)*factor,w*factor2);
}

vec4 draw_star_rays(vec2 uv, vec2 pos, float distanceCenter) {
    float ang = atan (uv.x, uv.y);

    float dist = length(uv);
    dist = pow(dist,.1);

    float f0 = 1.0 / (length (uv) * 16.0 + 1.0);

    float idx = mod((ang/3.1415 + 2.0)/4.0 + time * 0.2, 0.5) ;
    f0 = f0 + f0 * (sin(noise(idx) * 16.0) * 0.1 + dist * 0.1 + 0.4);

    vec3 c = vec3 (0.0);
    c = c * 1.3 - vec3 (length (uv) * 0.05);
    c += vec3 (f0);

    vec3 color = v_color.rgb * c / 2.0;
    color -= 0.015;
    color = cc (color, .2, .1);
    return vec4 (color, ((1.0 + sin(time * 80.0)) * 0.08 + 0.6) * v_color.a * (1.0 - distanceCenter) * (color.r + color.g + color.b) / 3.0);
}

vec4 draw_simple_star(float distanceCenter) {
    // Distance from the center of the image to the border, in [0, 1]
    float fac = 1.0 - pow(distanceCenter, 0.15);
    if(u_strayLight < 0){
    	fac = 0.0;
    }
    float core = step(ang_th, u_apparent_angle) * smoothstep(u_inner_rad, 0.0, distanceCenter);

    vec4 col = vec4 (v_color.rgb + core, v_color.a * (fac + core));
    if(u_star < 0.0){
		col.r = clamp(col.r, 0, 0.6);
		col.g = clamp(col.g, 0, 0.6);
		col.b = clamp(col.b, 0, 0.6);
		col.a = clamp(col.a, 0, 0.6);
	}
	return col;
}

vec4 draw_circle(float distanceCenter){
	float core = step(distanceCenter, u_inner_rad);
	return vec4 (v_color.rgb + core, v_color.a * core);
}

vec4
draw_star() {
    float dist = distance (vec2 (0.5), v_texCoords.xy) * 2.0;
    vec2 uv = v_texCoords - 0.5;
    if (u_distance < u_th_dist_up * 10000.0) {
        // Level is 0 when dist <= dist_down and 1 when dist >= dist_up
        float level = min((u_distance) / (u_th_dist_up * 10000.0), 1.0);
		
		if(u_strayLight < 0){
			return  draw_circle(dist);
		}else{
	        vec4 c = draw_star_rays(uv, vec2(0.5), dist);
	        vec4 s = draw_simple_star(dist);
	
	        return c  * (1.0 - level) + s;
	    }
    } else {
        return draw_simple_star(dist);
    }
}


void
main() {
    gl_FragColor = draw_star();
}
