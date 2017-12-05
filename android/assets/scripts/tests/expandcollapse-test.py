# Test script. Tests GUI expand and collapse.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()
gs.maximizeInterfaceWindow()

gs.sleep(1)
gs.expandGuiComponent("CameraComponent")
gs.sleep(1)
gs.expandGuiComponent("VisibilityComponent")
gs.sleep(1)
gs.collapseGuiComponent("CameraComponent")
gs.sleep(1)
gs.collapseGuiComponent("VisibilityComponent")
gs.sleep(1)

gs.enableInput()