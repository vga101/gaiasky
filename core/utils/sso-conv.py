#!/usr/bin/env python

import json, io, re, datetime, jdcal

AU_TO_KM = 149598000 
N_MAX = 700
FMT = "%Y%m%d"

class SSO(object):
    def __init__(self, name, color, epoch, meananomaly, semimajoraxis, eccentricity, argofpericenter, ascendingnode, period, inclination):
        self.name = name
        self.color = color
        self.parent = "Sol"
        self.impl = "gaia.cu9.ari.gaiaorbit.scenegraph.Orbit"
        self.provider = "gaia.cu9.ari.gaiaorbit.data.orbit.OrbitalParametersProvider"
        self.ct = [ "Asteroids", "Orbits" ]
        self.transformFunction = "eclipticToEquatorial"
        self.orbit = {}
        self.orbit["epoch"] = epoch
        self.orbit["meananomaly"] = meananomaly
        self.orbit["semimajoraxis"] = semimajoraxis
        self.orbit["eccentricity"] = eccentricity
        self.orbit["argofpericenter"] = argofpericenter
        self.orbit["ascendingnode"] = ascendingnode
        self.orbit["period"] = period
        self.orbit["inclination"] = inclination

def to_json(line, idx):
    values = line.split(',')
    # Designation
    name = values[2]
    color = [0.4, 1.0, 0.4, 0.5]
    # Epoch in yyyymmdd, convert
    ymd = values[8]
    dt = datetime.datetime.strptime(ymd, FMT)
    epoch = sum(jdcal.gcal2jd(dt.year, dt.month, dt.day))
    # Mean anomaly [deg]
    meananomaly = float(values[9])
    # Semimajor axis [Km]
    semimajoraxis = float(values[14]) * AU_TO_KM
    # Eccentricity
    eccentricity = float(values[13])
    # Argument of pericenter [deg]
    argofpericenter = float(values[10])
    # Ascending node [deg]
    ascendingnode = float(values[11])
    # Period in years, no data
    period = 0.1
    # Inclination [deg]
    inclination = float(values[12])
    
    bean = SSO(name, color, epoch, meananomaly, semimajoraxis, eccentricity, argofpericenter, ascendingnode, period, inclination)
    
    jsonstr = json.dumps(bean.__dict__)
    
    return jsonstr
    

with open('/media/tsagrista/Daten/Gaia/data/sso/DR2_aux_sso_orbit.csv', 'r') as fr:
    lines = fr.readlines()
    with open('/tmp/orbits_asteroids_dr2.json', 'w') as fw:
        fw.write("{\"objects\" : [\n")
        N = min(N_MAX, len(lines))
        for idx, line in enumerate(lines[1:N]):
            if line.strip():
                jsonstring = to_json(line, idx)
                fw.write(jsonstring)
                if idx < N - 2:
                    fw.write(",")
                fw.write("\n")

        fw.write("]}")
