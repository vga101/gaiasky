# Test script. Tests visibility commands.
# Created by Toni Sagrista

from time import sleep
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()

gs.setVisibility("Ecliptic grid", True)
sleep(4)
gs.setVisibility("Ecliptic grid", False)


gs.enableInput()
