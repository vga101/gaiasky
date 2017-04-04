# This script tests the go-to commands. To be run asynchronously.
# Created by Toni Sagrista

from time import sleep
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

gs.setRotationCameraSpeed(20)
gs.setTurningCameraSpeed(20)
gs.setCameraSpeed(20)

gs.goToObject("Sol", -1, 2.5)

gs.setHeadlineMessage("Sun")
gs.setSubheadMessage("This is the Sun, our star")

gs.sleep(5)
gs.clearAllMessages()

gs.goToObject("Earth", -1, 2.5)

gs.setHeadlineMessage("Earth")
gs.setSubheadMessage("This is the Earth, our home")

gs.sleep(5)
gs.clearAllMessages()

gs.setCameraFocus("Sol")
gs.sleep(4)

gs.enableInput()
gs.maximizeInterfaceWindow()