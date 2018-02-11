# This script tests the go-to commands. To be run asynchronously.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()

gs.setRotationCameraSpeed(20)
gs.setTurningCameraSpeed(20)
gs.setCameraSpeed(20)

gs.setPlanetariumMode(True)

gs.goToObject("Earth", 20.0, 4.5)
gs.sleep(4.0)

gs.setPlanetariumMode(False)


gs.enableInput()
