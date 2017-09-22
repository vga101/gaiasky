# This script tests the speed limit and the orientation lock API calls.
# Created by Toni Sagrista

from time import sleep
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface


gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

# Camera speed limit

gs.setCameraSpeedLimit(0)
sleep(1)
gs.setCameraSpeedLimit(1)
sleep(1)
gs.setCameraSpeedLimit(3)
sleep(1)
gs.setCameraSpeedLimit(4)
sleep(1)
gs.setCameraSpeedLimit(5)
sleep(1)
gs.setCameraSpeedLimit(6)
sleep(1)
gs.setCameraSpeedLimit(7)
sleep(1)
gs.setCameraSpeedLimit(8)
sleep(1)
gs.setCameraSpeedLimit(9)
sleep(1)
gs.setCameraSpeedLimit(10)
sleep(1)
gs.setCameraSpeedLimit(11)
sleep(1)
gs.setCameraSpeedLimit(12)
sleep(1)
gs.setCameraSpeedLimit(13)
sleep(2)

# Orientation lock
gs.setCameraOrientationLock(True)
sleep(1)
gs.setCameraOrientationLock(False)
sleep(1)



gs.enableInput()
