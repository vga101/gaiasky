#version 130

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

// v_texCoords are UV coordinates in [0..1]
in vec2 v_texCoords;
in vec4 v_color;

uniform sampler2D u_texture0;
// Distance in u to the star
uniform float u_distance;
// Apparent angle in deg
uniform float u_apparent_angle;
// Component alpha (galaxies)
uniform float u_alpha;

uniform float u_time;

uniform vec4 u_sliders;

#define distfac 3.24e-8 / 60000.0
#define distfacinv 60000.0 / 3.23e-8
#define light_decay 0.5




vec4 galaxyTexture(vec2 tc){
	return texture2D(u_texture0, tc);
}

float light(float distance_center, float decay) {
    return 1.0 - pow(distance_center, decay);
}

vec4 drawSimple(vec2 tc) {
	float dist = distance (vec2 (0.5), tc) * 2.0;
	float light = light(dist, light_decay);
	return vec4(v_color.rgb, light);
}




/**
 * SUPER GALAXY SHADER HERE
 **/
#define R(p, a) p=cos(a)*p+sin(a)*vec2(p.y, -p.x)
#define pi 3.14159265


const vec4
    colCenter = vec4(1.2, 1.5,1.5,.25),
	colEdge = vec4(.1,.1,.2,.5),
	colEdge2 = vec4(.7,.54,.3,.23),
    colEdge3 = vec4(.6,1.,1.3,.25);

vec4 sliderVal;

vec2 min2(vec2 a, vec2 b) {
    return a.x<b.x ? a  : b;
}

float hash(const vec3 p ) {
	float h = dot(p,vec3(127.1,311.7,758.5453123));
    return fract(sin(h)*43758.5453123);
}

float noise( in vec3 x )
{
    vec3 p = floor(x);
    vec3 f = fract(x);
    f = f*f*(3.0-2.0*f);

    return mix(mix(mix( hash(p+vec3(0,0,0)),
                        hash(p+vec3(1,0,0)),f.x),
                   mix( hash(p+vec3(0,1,0)),
                        hash(p+vec3(1,1,0)),f.x),f.y),
               mix(mix( hash(p+vec3(0,0,1)),
                        hash(p+vec3(1,0,1)),f.x),
                   mix( hash(p+vec3(0,1,1)),
                        hash(p+vec3(1,1,1)),f.x),f.y),f.z);
}


// ratio: ratio of hight/low frequencies
float fbmdust(vec3 p, float ratio) {
    return mix(noise(p*3.), noise(p*20.), ratio);
}

vec2 spiralArm(vec3 p, float thickness, float blurAmout, float blurStyle) {
    float dephase = 2.2, loop = 4.;
    float a = atan(p.x,p.z),  // angle
		  r = length(p.xz), lr = log(r), // distance to center
    	  th = (.1-.25*r), // thickness according to distance
    	  d = fract(.5*(a-lr*loop)/pi); //apply rotation and scaling.
    d = (.5/dephase - abs(d-.5))*2.*pi*r;
  	d *= (1.-lr)/thickness;  // space fct of distance
    // Perturb distance field
    float radialBlur = blurAmout*fbmdust(vec3(r*4.,10.*d,10.-5.*p.y),blurStyle);
    return vec2(sqrt(d*d+10.*p.y*p.y/thickness)-th*r*.2-radialBlur);
}

vec2 dfGalaxy(vec3 p, float thickness, float blurAmout, float blurStyle) {
	return min2(spiralArm(p,                  thickness, blurAmout, blurStyle),
    			spiralArm(vec3(p.z,p.y,-p.x), thickness, blurAmout, blurStyle));
}

vec2 map(vec3 p) {
	R(p.xz, 0.0*.008*pi+u_time*.3);
    return dfGalaxy(p, clamp(10.*sliderVal.x,0.0,10.), sliderVal.y, sliderVal.z);
}

//--------------------------------------------------------------

// assign color to the media
vec4 computeColor(vec3 p, float density, float radius, float id) {
	// color based on density alone, gives impression of occlusion within
	// the media
	vec4 result = mix( vec4(1.,.9,.8,1.), vec4(.4,.15,.1,1.), density );
	// color added to the media
	result *= mix( colCenter,
                  mix(colEdge2,
                      mix(colEdge, colEdge3, step(.08,id)), step(-.05,id)),
                  smoothstep(.2,.8,radius) );
	return result;
}

// - Ray / Shapes Intersection -----------------------
bool sBox(vec3 ro, vec3 rd, vec3 rad, out float tN, out float tF)  {
    vec3 m = 1./rd, n = m*ro,
    	k = abs(m)*rad,
        t1 = -n - k, t2 = -n + k;
	tN = max( max( t1.x, t1.y ), t1.z );
	tF = min( min( t2.x, t2.y ), t2.z );
	return !(tN > tF || tF < 0.);
}

bool sSphere(vec3 ro, vec3 rd, float r, out float tN, out float tF) {
	float b = dot(rd, ro), d = b*b - dot(ro, ro) + r;
	if (d < 0.) return false;
	tN = -b - sqrt(d);
	tF = -tN-b-b;
	return tF > 0.;
}

vec4 sliderValuesInit(){
	return u_sliders;
}




// ---------------------------------------------------
// Based on "Dusty nebula 4" by Duke (https://www.shadertoy.com/view/MsVXWW)
vec4 galaxy(vec2 tc) {
	sliderVal = sliderValuesInit();
   // camera
    float a = sliderVal.w*pi;
    vec3 ro = vec3(0., 2.*cos(a), -4.5*sin(a)),
         ta = vec3(0.0, 0.0, 0.0);

    // camera tx
    vec3 cw = normalize( ta-ro ),
     	 cp = vec3( 0., 1., 0. ),
     	 cu = normalize( cross(cw,cp) ),
     	 cv = normalize( cross(cu,cw) );
    vec2 q = tc * 1.5,
     	 p = -2.+3.*q;
    p.x *= 1.0;

    vec3 rd = normalize( p.x*cu + p.y*cv + 2.5*cw );

	// ld, td: local, total density
	// w: weighting factor
	float ld=0., td=0., w=0.;

	// t: length of the ray
	// d: distance function
	float d=1., t=0.;

    const float h = 0.1;

	vec4 sum = vec4(0);

    float min_dist=0.,  max_dist=0.;
    float min_dist2=0., max_dist2=0.;

    if(sSphere(ro, rd, 4., min_dist, max_dist)) {
        if (sBox(ro, rd, vec3(4.,1.8,4.), min_dist2, max_dist2)) {
        	min_dist = max(0.1,max(min_dist, min_dist2));
            max_dist = min(max_dist, max_dist2);

            t = min_dist*step(t, min_dist) + .1*hash(rd+u_time);


            // raymarch loop
            vec4 col;
            for (int i=0; i<100; i++) {
                vec3 pos = ro + t*rd;

                // Loop break conditions.
                if(td > .9 || sum.a > .99 || t > max_dist) break;

                // evaluate distance function
                vec2 res = map(pos);
                d = max(res.x,.01);

                // point light calculations
                vec3 ldst = pos;
                ldst.y*=1.6;
                vec3 ldst2 = pos;
                ldst2.y*=3.6;
                float lDist = max(length(ldst),.1); //max(length(ldst), 0.001);
				float lDist2 = max(length(ldst2),.1);
                // star in center
                vec3 lightColor = (1.-smoothstep(3.,4.5,lDist*lDist))*
                    mix(.015*vec3(1.,.5,.25)/(lDist*lDist),
                        .02*vec3(.5,.7,1.)/(lDist2*lDist2),
                        smoothstep(.1,2.,lDist*lDist));
                sum.rgb += lightColor; //.015*lightColor/(lDist*lDist); // star itself and bloom around the light
                sum.a += .003/(lDist*lDist);;

                if (d<h) {
                    // compute local density
                    ld = h - d;
                    // compute weighting factor
                    w = (1. - td) * ld;
                    // accumulate density
                    td += w + 1./60.;
                    // get color of object (with transparencies)
                    col = computeColor(pos, td,lDist*2., res.y);
                    col.a *= td;
                    // colour by alpha
                    col.rgb *= col.a;
                    // alpha blend in contribution
                    sum += col*(1.0 - sum.a);
                }

                //float pitch = t/iResolution.x;
                //float dt = max(d * 0.25, .005); //pitch);
                // trying to optimize step size near the camera and near the light source
                t += max(d * .15 * max(min(length(ldst), length(ro)),1.0), 0.005);
                td += .1/70.;
                //t += dt;
            }
            // simple scattering
            sum *= 1. / exp( ld * .2 )*.8 ;
            sum = clamp( sum, 0., 1. );
    	}
    }

    //Apply slider overlay
    return vec4(sum.xyz, length(sum.xyz));

}


void main() {

	float factor = smoothstep(distfacinv/8.0, distfacinv, u_distance);
	if(factor == 0.0){
		// Galaxy
		//gl_FragColor = galaxyTexture(v_texCoords);
		gl_FragColor = galaxy(v_texCoords);
	}else if(factor == 1.0){
		gl_FragColor = drawSimple(v_texCoords);
	}else{
		gl_FragColor = drawSimple(v_texCoords) * factor + galaxy(v_texCoords) * (1.0 - factor);
	}
	gl_FragColor *= u_alpha;

    // Debug! - visualise depth buffer
    //gl_FragColor = vec4(vec3(gl_FragCoord.z), 1.0f);
}
