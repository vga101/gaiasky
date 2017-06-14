# Test script. Tests visibility commands.
# Created by Toni Sagrista

from time import sleep
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()

gs.setVisibility("element.ecliptic", True)
sleep(4)
gs.setVisibility("element.ecliptic", False)


gs.enableInput()
