# This script tests the go-to commands. To be run asynchronously.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

gs.setRotationCameraSpeed(20)
gs.setTurningCameraSpeed(20)
gs.setCameraSpeed(20)

gs.goToObject("Sol", 20.0, 4.5)

gs.setHeadlineMessage("Sun")
gs.setSubheadMessage("This is the Sun, our star")

gs.gs.sleep(4)
gs.clearAllMessages()

gs.goToObject("Sol", 5.5)

gs.setHeadlineMessage("Sun")
gs.setSubheadMessage("We are now zooming out a bit")

gs.gs.sleep(4)
gs.clearAllMessages()

gs.goToObject("Earth", 20.0, 6.5)

gs.setHeadlineMessage("Earth")
gs.setSubheadMessage("This is the Earth, our home")

gs.gs.sleep(4)
gs.clearAllMessages()

gs.goToObject("Earth", 2.5, 1.5)

gs.setHeadlineMessage("Earth")
gs.setSubheadMessage("Zooming out here...")

gs.gs.sleep(4)
gs.clearAllMessages()

gs.setCameraFocus("Sol")
gs.gs.sleep(4)

gs.enableInput()
gs.maximizeInterfaceWindow()