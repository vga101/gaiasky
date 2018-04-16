# Tour of Gaia, the hyades and proper motions
# Starts at Earth, moves to Gaia, sees it in movement, enables
# clusters, moves to the Hyades and demonstrates proper motions

from time import sleep
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface
from gaia.cu9.ari.gaiaorbit.util import GlobalConf

gs = EventScriptingInterface.instance()


gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()
gs.setFov(60)
gs.setRotationCameraSpeed(20)
gs.setTurningCameraSpeed(20)
gs.setCameraSpeed(20)

gs.setSimulationPace(0)
gs.setStarBrightness(27.0)
gs.setStarSize(8.0)
gs.setBrightnessLevel(0)
gs.setContrastLevel(1)
gs.setAmbientLight(0)
gs.setBloom(0)
gs.setMotionBlur(False)


gs.setRotationCameraSpeed(40)
gs.setTurningCameraSpeed(30)
gs.setCameraSpeed(30)
gs.setVisibility('element.labels',False)
gs.setVisibility('element.constellations',False)
gs.setVisibility("element.orbits", True)


gs.setSimulationTime(2018,4,25,10,0,0,0)
initialTime=gs.getSimulationTime();
gs.setCameraFocus("Earth")
gs.setCameraPosition([-79512530.4015463, -34466201.483790025, -123027319.20420773])
gs.setCameraDirection([-0.05362818839866128, -0.7694543330834683, -0.6364464209249313])
gs.setCameraUp([-0.2819369721261508, 0.6230989993308738, -0.7295609506965115])
gs.setCrosshairVisibility(False)
gs.sleep(5)


gs.setCinematicCamera(True)
gs.sleep(5)

gs.setCameraSpeed(10)

gs.goToObject("Earth", 10.0, 10)
gs.sleep(3)

gs.setCinematicCamera(True)


gs.setTurningCameraSpeed(0.01)
gs.setRotationCameraSpeed(1)


gs.setCameraFocus("Gaia")

dgaia=gs.getDistanceTo("Gaia")

gs.sleep(2)
gs.goToObject("Gaia", 60, 1)


from gaia.cu9.ari.gaiaorbit.util import GlobalConf


au_to_km = 149597900.0

gs.disableInput()
gs.minimizeInterfaceWindow()

gs.setRotationCameraSpeed(20)
gs.setTurningCameraSpeed(20)
gs.setCameraSpeed(30)
gs.setVisibility('element.labels',False)



gs.setVisibility('element.labels',False)
gs.setVisibility('element.planets',True)
gs.setVisibility('element.moons',True)
gs.setCinematicCamera(True)

gs.setCameraFocus("Gaia")
gs.setCameraLock(True)

gs.sleep(5)
gs.setCameraOrientationLock(False)


gs.setSimulationPace(1000.0)
gs.startSimulationTime()

gs.sleep(5)

gs.sleep(5)
gs.setCinematicCamera(True)

gs.setRotationCameraSpeed(40)
gs.cameraRotate(1,0)
gs.sleep(15)
gs.cameraStop()
gs.sleep(10)

gs.setCinematicCamera(True)
gs.setTargetTime(2019,4,25,10,0,0,0)
gs.setSimulationPace(1000000.0)
gs.setCinematicCamera(True)
gs.sleep(15)
gs.setVisibility("element.orbits", False)
gs.sleep(20)


gs.stopSimulationTime()
gs.setSimulationPace(0)


gs = EventScriptingInterface.instance()
gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

gs.setCinematicCamera(True)


gs.setRotationCameraSpeed(20)
gs.setTurningCameraSpeed(20)
gs.setCameraSpeed(20)
gs.setVisibility('element.labels',False)
gs.setSimulationPace(1)
gs.setVisibility('element.labels',False)
gs.setVisibility('element.planets',True)
gs.setVisibility('element.moons',True)


gs.startSimulationTime()
gs.setTurningCameraSpeed(0.1)
gs.setCameraSpeed(5)

gs.setTurningCameraSpeed(1)

gs.setVisibility("element.clusters", True)


gs.goToObject("78The2Tau", 0.000004, 25)
gs.setVisibility("element.clusters", False)

gs.setCameraLock(True)

gs.cameraStop()
gs.setCameraLock(False)
gs.setCameraFree()
gs.setVisibility("element.propermotions", True)
gs.sleep(5)

gs.setSimulationPace(1e12)
gs.sleep(5)
gs.setVisibility("element.propermotions", False)
gs.sleep(5)
pace=-3e12
gs.setSimulationPace(pace)
gs.setTargetTime(initialTime)
gs.sleep(8)


gs.setSimulationPace(0)
gs.startSimulationTime()
gs.sleep(1)




au_to_km = 149597900.0

gs.setTurningCameraSpeed(20)
gs.setCameraSpeed(20)


gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

gs.setSimulationTime(initialTime)

gs.setRotationCameraSpeed(20)
gs.setTurningCameraSpeed(20)


gs.setCinematicCamera(True)


gs.setSimulationPace(0)
gs.setCameraSpeed(5)


gs.sleep(3)

gs.goToObject("78The2Tau", 0.0000000007, 40)
              
gs.setCameraFocus("78The2Tau")
gs.setCameraLock(True)
gs.sleep(5)

gs.startSimulationTime()
gs.setCinematicCamera(True)
gs.cameraRotate(0.3,0)
gs.sleep(1)


gs.enableInput()
gs.maximizeInterfaceWindow()
gs.setCameraSpeed(20)
gs.setSimulationPace(1)
gs.setStarBrightness(27.0)
gs.setStarSize(8.0)
gs.setRotationCameraSpeed(40)
gs.setTurningCameraSpeed(24)
gs.setCameraSpeed(40)
gs.setBrightnessLevel(0)
gs.setContrastLevel(1)
gs.setAmbientLight(0)
gs.setBloom(0)
gs.setMotionBlur(False)
gs.setFov(60)

from java.util import Date
now = Date()
gs.setSimulationTime(now.getTime())



gs.setVisibility('element.labels',True)
gs.setVisibility('element.planets',True)
gs.setVisibility('element.moons',True)



