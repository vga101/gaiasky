# Test script. Tests getObject() and getObjectRadius()
# Created by Toni Sagrista

import math
from time import sleep
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()

# Use the object with the following name
obj = "Betelgeuse"

sleep(1)
gs.goToObject(obj, 3.5)

rad = gs.getObjectRadius(obj)

print("%s radius: %f Km" % (obj, rad))

sleep(1)

body = gs.getObject(obj)
absmag = body.absmag
appmag = body.appmag
colbv = body.colorbv
radec = body.posSph

print("Absmag: %f, appmag: %f, B-V: %f, RA: %f, DEC: %f" % (absmag, appmag, colbv, radec.x, radec.y))


# Now stop at a certain distance of earth
dist = 15000 #Km
earthrad = gs.getObjectRadius("Earth")
anglerad = math.acos((earthrad*2)/dist)

gs.goToObject("Earth", math.degrees(anglerad))


gs.enableInput()
