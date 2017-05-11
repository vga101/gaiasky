# This script tests the go-to commands. To be run asynchronously.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface

from gaia.cu9.ari.gaiaorbit.event import EventManager
from gaia.cu9.ari.gaiaorbit.event import Events


gs = EventScriptingInterface.instance()

gs.setStarBrightness(70.0)