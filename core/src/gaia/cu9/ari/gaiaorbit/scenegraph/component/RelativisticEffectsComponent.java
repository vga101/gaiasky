package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.gravwaves.GravitationalWavesManager;
import gaia.cu9.ari.gaiaorbit.util.override.Matrix3Attribute;
import gaia.cu9.ari.gaiaorbit.util.override.RelativisticEffectFloatAttribute;
import gaia.cu9.ari.gaiaorbit.util.override.Vector3Attribute;
import gaia.cu9.ari.gaiaorbit.util.override.Vector4Attribute;

public class RelativisticEffectsComponent {
    private Vector3 aux;

    public RelativisticEffectsComponent() {
        super();
        aux = new Vector3();
    }

    public void doneLoading(Map<String, Material> materials, boolean rel, boolean grav) {
        Set<String> keys = materials.keySet();
        for (String key : keys) {
            Material mat = materials.get(key);
            if (rel)
                setUpRelativisticEffectsMaterial(mat);
            if (grav)
                setUpGravitationalWavesMaterial(mat);
        }
    }

    public void setUpRelativisticEffectsMaterial(Array<Material> materials) {
        for (Material material : materials) {
            setUpRelativisticEffectsMaterial(material);
        }
    }

    public void setUpRelativisticEffectsMaterial(Material mat) {
        mat.set(new RelativisticEffectFloatAttribute(RelativisticEffectFloatAttribute.Vc, 0f));
        mat.set(new Vector3Attribute(Vector3Attribute.VelDir, new Vector3()));
    }

    public void removeRelativisticEffectsMaterial(Array<Material> materials) {
        for (Material material : materials) {
            removeRelativisticEffectsMaterial(material);
        }
    }

    public void removeRelativisticEffectsMaterial(Material mat) {
        mat.remove(RelativisticEffectFloatAttribute.Vc);
        mat.remove(Vector3Attribute.VelDir);
    }

    public void setUpGravitationalWavesMaterial(Array<Material> materials) {
        for (Material material : materials) {
            setUpGravitationalWavesMaterial(material);
        }
    }

    public void setUpGravitationalWavesMaterial(Material mat) {
        mat.set(new Vector4Attribute(Vector4Attribute.Hterms, new float[4]));
        mat.set(new Vector3Attribute(Vector3Attribute.Gw, new Vector3()));
        mat.set(new Matrix3Attribute(Matrix3Attribute.Gwmat3, new Matrix3()));
        mat.set(new RelativisticEffectFloatAttribute(RelativisticEffectFloatAttribute.Ts, 0f));
        mat.set(new RelativisticEffectFloatAttribute(RelativisticEffectFloatAttribute.Omgw, 0f));
    }

    public void removeUpGravitationalWavesMaterial(Array<Material> materials) {
        for (Material material : materials) {
            removeUpGravitationalWavesMaterial(material);
        }
    }

    public void removeUpGravitationalWavesMaterial(Material mat) {
        mat.remove(Vector4Attribute.Hterms);
        mat.remove(Vector3Attribute.Gw);
        mat.remove(Matrix3Attribute.Gwmat3);
        mat.remove(RelativisticEffectFloatAttribute.Ts);
        mat.remove(RelativisticEffectFloatAttribute.Omgw);
    }

    public void updateRelativisticEffectsMaterial(Material material, ICamera camera) {
        updateRelativisticEffectsMaterial(material, camera, (float) (camera.getSpeed() / Constants.C_KMH));
    }

    public void updateRelativisticEffectsMaterial(Material material, ICamera camera, float vc) {

        // v/c
        if (material.get(RelativisticEffectFloatAttribute.Vc) != null)
            ((RelativisticEffectFloatAttribute) material.get(RelativisticEffectFloatAttribute.Vc)).value = vc;

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

    public void updateGravitationalWavesMaterial(Material material, ICamera camera) {
        GravitationalWavesManager gw = GravitationalWavesManager.instance();

        // hterms
        ((Vector4Attribute) material.get(Vector4Attribute.Hterms)).value = gw.hterms;

        // gw
        ((Vector3Attribute) material.get(Vector3Attribute.Gw)).value.set(gw.gw);

        // gwmat3
        ((Matrix3Attribute) material.get(Matrix3Attribute.Gwmat3)).value.set(gw.gwmat3);

        // ts
        ((RelativisticEffectFloatAttribute) material.get(RelativisticEffectFloatAttribute.Ts)).value = gw.gwtime;

        // omgw
        ((RelativisticEffectFloatAttribute) material.get(RelativisticEffectFloatAttribute.Omgw)).value = gw.omgw;
    }

}
