#version 120

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// v_texCoords are UV coordinates in [0..1]
varying vec2 v_texCoords;
varying vec4 v_color;

uniform sampler2D u_starTexture;
uniform float u_radius;
uniform float u_apparent_angle;
uniform float u_inner_rad;
uniform float u_time;
uniform float u_thpoint;
// Distance in u to the star
uniform float u_distance;
// Whether light scattering is enabled or not
uniform int u_lightScattering;


// Time multiplier
#define time u_time * 0.02

// Constants as a factor of the radius
#define model_const 172.4643429
#define rays_const 50000000.0

// Decays
#define corona_decay 0.2
#define light_decay 0.05



float core(float distance_center, float inner_rad){
	if(inner_rad == 0.0){
		return 0.0;
	}
	float core = 1.0 - step(inner_rad / 5.0, distance_center);
	float core_glow = smoothstep(inner_rad / 2.0, inner_rad / 5.0, distance_center);
	return core_glow + core;
}

float light(float distance_center, float decay) {
    float light = 1.0 - pow(distance_center, decay);
    return clamp(light, 0.0, 0.97);
}


float average(vec4 color){
	return (color.r + color.g + color.b) / 3.0;
}

float startex(vec2 tc){
	return clamp(average(texture2D(u_starTexture, tc)), 0.0, 1.0);
}


vec4 draw() {
    float dist = distance (vec2 (0.5), v_texCoords.xy) * 2.0;

	// level = 1 if distance == u_radius * model_const
	// level = 0 if distance == radius
	// level > 1 if distance > u_radius * model_const
	float level = (u_distance - u_radius) / ((u_radius * model_const) - u_radius);

	if(level >= 1.0){
		// We are far away from the star
		level = u_distance / (u_radius * rays_const);
		float light_level = smoothstep(u_thpoint, u_thpoint * 1.4, u_apparent_angle);

		if(u_lightScattering == 1){
			// Light scattering, simple star
			float core = core(dist, u_inner_rad);
			float light = light(dist, light_decay) * light_level;
			return vec4(v_color.rgb + vec3(core * 5.0), light + core);
		} else {
			// No light scattering, star rays
			level = min(level, 1.0);
			float corona = startex(v_texCoords);
	        float light = light(dist, light_decay * 2.0) * light_level;
	        float core = core(dist, u_inner_rad);

			return vec4(v_color.rgb + core, (corona * (1.0 - level) + light + core));
		}
	} else {
		// We are close to the star

		level = min(level, 1.0);
		float level_corona = u_lightScattering * level;

    	float corona = startex(v_texCoords);
    	float light = light(dist, light_decay * 2.0);
    	float core = core(dist, u_inner_rad);

		return vec4(v_color.rgb + core, (corona * (1.0 - level_corona) + light + level * core));
	}
}

void main() {
    gl_FragColor = clamp(draw(), 0.0, 1.0);
    // Debug! - visualise depth buffer
    //gl_FragColor = vec4(vec3(gl_FragCoord.z), 1.0f);
}
