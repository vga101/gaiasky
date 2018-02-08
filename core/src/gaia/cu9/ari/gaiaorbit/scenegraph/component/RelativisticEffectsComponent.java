package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.override.RelativisticEffectFloatAttribute;
import gaia.cu9.ari.gaiaorbit.util.override.Vector3Attribute;

public class RelativisticEffectsComponent {
    private Vector3 aux;

    public RelativisticEffectsComponent() {
        super();
        aux = new Vector3();
    }

    public void doneLoading(Map<String, Material> materials) {
        Set<String> keys = materials.keySet();
        for (String key : keys) {
            setUpRelativisticEffectsMaterial(materials.get(key));
        }
    }

    private void setUpRelativisticEffectsMaterial(Material mat) {
        mat.set(new RelativisticEffectFloatAttribute(RelativisticEffectFloatAttribute.Vc, 0f));
        mat.set(new Vector3Attribute(Vector3Attribute.VelDir, new Vector3()));
    }

    public void updateRelativisticEffectsMaterial(Material material, ICamera camera) {

        // v/c
        if (material.get(RelativisticEffectFloatAttribute.Vc) != null)
            ((RelativisticEffectFloatAttribute) material.get(RelativisticEffectFloatAttribute.Vc)).value = (float) (camera.getSpeed() / Constants.C_KMH);

        // Velocity direction
        if (material.get(Vector3Attribute.VelDir) != null) {
            if (camera.getVelocity() == null || camera.getVelocity().len() == 0) {
                aux.set(1, 0, 0);
            } else {
                camera.getVelocity().put(aux).nor();
            }
            ((Vector3Attribute) material.get(Vector3Attribute.VelDir)).value.set(aux);
        }

    }

}
