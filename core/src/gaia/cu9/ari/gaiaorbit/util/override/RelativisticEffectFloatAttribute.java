package gaia.cu9.ari.gaiaorbit.util.override;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;

public class RelativisticEffectFloatAttribute extends FloatAttribute {

    public RelativisticEffectFloatAttribute(long type) {
        super(type);
    }

    public RelativisticEffectFloatAttribute(long type, float value) {
        super(type, value);
    }

    public static final String VcAlias = "vc";
    public static final long Vc = register(VcAlias);

    @Override
    public Attribute copy() {
        return new RelativisticEffectFloatAttribute(type, value);
    }

}
