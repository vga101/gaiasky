# This script tests the star size commands.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface

from gaia.cu9.ari.gaiaorbit.event import EventManager
from gaia.cu9.ari.gaiaorbit.event import Events


gs = EventScriptingInterface.instance()

gs.maximizeInterfaceWindow()

gs.setMinStarOpacity(100.0)
gs.sleep(2)
gs.setMinStarOpacity(70.0)
gs.sleep(2)
gs.setMinStarOpacity(50.0)
gs.sleep(2)
gs.setMinStarOpacity(30.0)
gs.sleep(2)
gs.setMinStarOpacity(12.0)
gs.sleep(2)
gs.setMinStarOpacity(0.0)