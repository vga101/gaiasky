package gaia.cu9.ari.gaiaorbit.util.override;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.utils.NumberUtils;

public class ShadowMapAttribute extends Attribute {
    public ShadowMapAttribute(long type) {
        super(type);
    }

    public ShadowMapAttribute(long type, float value) {
        this(type);
        this.value = value;
    }

    public float value;

    public static final String CameraNearAlias = "cameraNear";
    public static final long CameraNear = register(CameraNearAlias);

    public static final String CameraFarAlias = "cameraFar";
    public static final long CameraFar = register(CameraFarAlias);

    public static final String ShadowPCFOffsetAlias = "shadowPCFOffset";
    public static final long ShadowPCFOffset = register(ShadowPCFOffsetAlias);

    @Override
    public Attribute copy() {
        return new ShadowMapAttribute(type, value);
    }

    @Override
    public int hashCode() {
        int result = (int) type;
        result = 977 * result + NumberUtils.floatToRawIntBits(value);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /** TODO Implement this **/
    @Override
    public int compareTo(Attribute o) {
        return 0;
    }
}
