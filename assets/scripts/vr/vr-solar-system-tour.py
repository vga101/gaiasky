# Tour through the solar system
# Starts at Earth, goes to Saturn and ends with a view of the asteroids in motion
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface
gs = EventScriptingInterface.instance()

# Camera params
gs.setCameraSpeed(3.0)
gs.setRotationCameraSpeed(42.0)
gs.setTurningCameraSpeed(4.0)
gs.setCinematicCamera(True)

# Visibility
gs.setVisibility("element.planets", True)
gs.setVisibility("element.atmospheres", True)
gs.setVisibility("element.stars", True)
gs.setVisibility("element.moons", True)
gs.setVisibility("element.satellites", True)
gs.setVisibility("element.galaxies", True)
gs.setVisibility("element.milkyway", True)

gs.setVisibility("element.asteroids", False)
gs.setVisibility("element.orbits", False)
gs.setVisibility("element.labels", False)
gs.setVisibility("element.constellations", False)
gs.setVisibility("element.boundaries", False)
gs.setVisibility("element.equatorial", False)
gs.setVisibility("element.ecliptic", False)
gs.setVisibility("element.galactic", False)
gs.setVisibility("element.clusters", False)
gs.setVisibility("element.meshes", False)
gs.setVisibility("element.titles", False)

gs.setCrosshairVisibility(False)

# Start at Earth
gs.setSimulationTime(2018, 4, 25, 12, 0, 0, 0)
gs.setCameraFocusInstantAndGo("Earth")
gs.sleep(3.0)
gs.goToObject("Earth", 49.0, 0.0)

# Rotate slowly
gs.sleep(5.0)
gs.setVisibility("element.orbits", True)
gs.sleep(5.0)

gs.goToObject("Earth", 1.0)

gs.cameraStop()

gs.sleep(2.0)

gs.stopSimulationTime()

gs.goToObject("Saturn", 40.0, 0.0)

gs.cameraRotate(0.6, 0.0)
gs.setSimulationPace(5000.0)
gs.startSimulationTime()

gs.sleep(15.0)

gs.cameraStop()

gs.setVisibility("element.asteroids", True)
gs.setVisibility("element.orbits", False)
gs.goToObject("Sol", 0.16, 0.0)

gs.cameraStop()


gs.startSimulationTime()

initime = 4000.0
endtime = 4000000.0

def frange(x, y, jump):
    while x < y:
        yield x
        x += jump

# 10 seconds, in steps of 0.1 is 100 steps
step = (endtime - initime) / 200.0
gs.setSimulationPace(initime)
for t in frange(initime, endtime, step):
    gs.setSimulationPace(t)
    gs.sleep(0.05)

	
gs.sleep(10.0)

gs.cameraRotate(0.0, -1.0)
gs.setVisibility("element.orbits", True)

gs.sleep(10.0)


gs.goToObject("Sol", 0.01, 0.0)

gs.stopSimulationTime()
gs.cameraStop()


