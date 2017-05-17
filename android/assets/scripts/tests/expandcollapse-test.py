# Test script. Tests GUI expand and collapse.
# Created by Toni Sagrista

from time import sleep
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()
gs.maximizeInterfaceWindow()

sleep(1)
gs.expandGuiComponent("CameraComponent")
sleep(1)
gs.expandGuiComponent("VisibilityComponent")
sleep(1)
gs.collapseGuiComponent("CameraComponent")
sleep(1)
gs.collapseGuiComponent("VisibilityComponent")
sleep(1)

gs.enableInput()