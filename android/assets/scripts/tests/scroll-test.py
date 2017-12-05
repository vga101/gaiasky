# Test script. Tests GUI scroll movement commands.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()

gs.setGuiScrollPosition(20.0)
gs.sleep(1)
gs.setGuiScrollPosition(40.0)
gs.sleep(1)
gs.setGuiScrollPosition(60.0)
gs.sleep(1)
gs.setGuiScrollPosition(80.0)
gs.sleep(1)
gs.setGuiScrollPosition(100.0)
gs.sleep(1)

gs.enableInput()
