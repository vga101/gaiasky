# This script tests the star brightness commands.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface

from gaia.cu9.ari.gaiaorbit.event import EventManager
from gaia.cu9.ari.gaiaorbit.event import Events


gs = EventScriptingInterface.instance()

gs.setStarBrightness(100.0)
gs.sleep(2)
gs.setStarBrightness(70.0)
gs.sleep(2)
gs.setStarBrightness(50.0)
gs.sleep(2)
gs.setStarBrightness(30.0)
gs.sleep(2)
gs.setStarBrightness(12.0)
gs.sleep(2)