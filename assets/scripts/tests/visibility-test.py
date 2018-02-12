# Test script. Tests visibility commands.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()

gs.setVisibility("element.ecliptic", True)
gs.sleep(4)
gs.setVisibility("element.ecliptic", False)


gs.enableInput()
