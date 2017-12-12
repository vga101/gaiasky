# Test script. Tests brightness and contrast commands.
# Created by Toni Sagrista

from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface

gs = EventScriptingInterface.instance()

gs.disableInput()
gs.cameraStop()

print("Testing brightness")

gs.setBrightnessLevel(-1.0)
gs.sleep(1)
gs.setBrightnessLevel(-0.5)
gs.sleep(1)
gs.setBrightnessLevel(-0.0)
gs.sleep(1)
gs.setBrightnessLevel(0.5)
gs.sleep(1)
gs.setBrightnessLevel(1.0)
gs.sleep(1)
gs.setBrightnessLevel(0.0)
gs.sleep(1)

print("Testing contrast")

gs.setContrastLevel(0.0)
gs.sleep(1)
gs.setContrastLevel(0.5)
gs.sleep(1)
gs.setContrastLevel(1.0)
gs.sleep(1)
gs.setContrastLevel(1.5)
gs.sleep(1)
gs.setContrastLevel(2.0)
gs.sleep(1)
gs.setContrastLevel(1.0)
gs.sleep(10)

print("Check out of bounds parameter still works")
gs.setContrastLevel(4.0)
gs.sleep(1)
gs.setContrastLevel(1.0)

gs.enableInput()
