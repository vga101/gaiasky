# Test script. Tests GUI position commands.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()
gs.maximizeInterfaceWindow()

gs.setGuiPosition(0, 0)
gs.sleep(1)
gs.minimizeInterfaceWindow()
gs.setGuiPosition(0, 0)
gs.sleep(1)
gs.maximizeInterfaceWindow()
gs.setGuiPosition(0.5, 0.5)
gs.sleep(1)
gs.setGuiPosition(1, 1)
gs.sleep(1)
gs.setGuiPosition(0, 1)

gs.enableInput()