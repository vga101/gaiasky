# Test script. Tests simulation time commands.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()

# Jan 1st, 2017  14:23:58:000
gs.setSimulationTime(2017, 1, 1, 14, 23, 58, 0)


gs.enableInput()
