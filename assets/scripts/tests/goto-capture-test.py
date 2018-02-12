# This script tests the go-to and capture frame commands. To be run asynchronously.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

gs.setRotationCameraSpeed(40)
gs.setTurningCameraSpeed(30)
gs.setCameraSpeed(30)

gs.configureRenderOutput(1280, 720, 60, '/home/tsagrista/.gaiasky/frames', 'scripting-test')
gs.setFrameOutput(True)

gs.goToObject("Sol", -1, 2.5)

gs.setHeadlineMessage("Sun")
gs.setSubheadMessage("This is the Sun, our star")

gs.sleepFrames(120)
gs.clearAllMessages()

gs.goToObject("Earth", -1, 2.5)

gs.setHeadlineMessage("Earth")
gs.setSubheadMessage("This is the Earth, our home")

gs.sleepFrames(60)
gs.clearAllMessages()

gs.setCameraFocus("Sol")
gs.gs.sleep(4)

gs.setFrameOutput(False)


gs.enableInput()
gs.maximizeInterfaceWindow()