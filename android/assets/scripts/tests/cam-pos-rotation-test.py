# This script tests the positioning of the camera with relation to two objects.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

# Camera looks at sunny side of Earth
gs.setCameraPositionAndFocus("Earth", "Sol", 0, 30)
gs.sleep(3)

# Camera looks at 50% sunny, 50% shady
gs.setCameraPositionAndFocus("Earth", "Sol", 90, 30)
gs.sleep(3)

# Camera looks at shady side of Earth
gs.setCameraPositionAndFocus("Earth", "Sol", 180, 30)
gs.sleep(3)

# Camera looks at sunny side of Mars
gs.setCameraPositionAndFocus("Mars", "Sol", 0, 30)
gs.sleep(3)

# Camera looks at shady side of Mars
gs.setCameraPositionAndFocus("Mars", "Sol", 180, 30)
gs.sleep(3)


gs.enableInput()
