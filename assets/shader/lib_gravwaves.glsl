// Computes 
vec3 computeGravitationalWaves(vec3 pos, vec2 gw, float t, float omgw, vec4 hterms) {
    float hpluscos = hterms.x;
    float hplussin = hterms.y;
    float htimescos = hterms.z;
    float htimessin = hterms.w;
    
    float agw = gw.x;
    float dgw = gw.y;
    
    float cosagw = cos(agw);
    float cosdgw = cos(dgw);
    float sinagw = sin(agw);
    float sindgw = sin(dgw);
    
    vec3 p = vec3(cosagw * cosdgw, sinagw * cosdgw, sindgw);
    mat3 eplus = mat3(1,0,0, 0,-1,0, 0,0,0);
    mat3 etimes = mat3(0,1,0, 1,0,0, 0,0,0);
    
    mat3 P = mat3(-sinagw, -cosagw * sindgw, cosagw * cosdgw, cosagw, -sinagw * sindgw, sinagw * cosdgw, 0, cosdgw, sindgw);
    mat3 pepluspt = P * eplus * transpose(P);
    mat3 petimespt = P * etimes * transpose(P);
    
    float plusphase = hpluscos * cos(omgw * t) + hplussin * sin(omgw * t);
    float timesphase = htimescos * cos(omgw * t) + htimessin * sin(omgw * t);
    
    pepluspt = pepluspt * plusphase;
    petimespt = petimespt * timesphase;
    
    mat3 pept = pepluspt + petimespt;
    
    // Backpu distance to pos
    float poslen = length(pos);
    // Normalize pos
    pos = normalize(pos);
    
    vec3 huu = pept * pos * pos;
    vec3 hu = 0.5 * pept * pos;
    
    vec3 deltau = ((pos + p) / (2 * (1 + pos * p))) * huu - hu;
    
    // Apply shift, compute new position using backup distance
    pos = pos + deltau;
    pos = normalize(pos) * poslen;
    return pos;
}