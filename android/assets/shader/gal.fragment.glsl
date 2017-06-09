#version 120

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// v_texCoords are UV coordinates in [0..1]
varying vec2 v_texCoords;
varying vec4 v_color;

uniform sampler2D u_starTexture;
uniform float u_time;
// Distance in u to the star
uniform float u_distance;
// Apparent angle in deg
uniform float u_apparent_angle;


#define light_decay 0.5


float light(float distance_center, float decay) {
    return 1.0 - pow(distance_center, decay);
}

vec4 startex(vec2 tc){
	return texture2D(u_starTexture, tc);
}


vec4 draw() {
	vec2 uv = vec2(v_texCoords.x, v_texCoords.y);
	if(u_apparent_angle > 2e-6){
		vec4 tex = startex(uv);
		return tex;
	}else{
		float dist = distance (vec2 (0.5), uv) * 2.0;
		float light = light(dist, light_decay);
		return vec4(v_color.rgb, light);
	}
}

void main() {
    gl_FragColor = draw();
    // Debug! - visualise depth buffer
    //gl_FragColor = vec4(vec3(gl_FragCoord.z), 1.0f);
}
