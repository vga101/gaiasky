package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import com.badlogic.gdx.math.Matrix4;

public class RotateTransform implements ITransform {
    /** Rotation axis **/
    float[] axis;
    /** Rotation angle **/
    float angle;
    
    public void apply(Matrix4 mat){
        mat.rotate(axis[0], axis[1], axis[2], angle);
    }
    
    public void setAxis(double[] axis){
        this.axis = new float[axis.length];
        for(int i =0; i< axis.length; i++)
            this.axis[i] = (float) axis[i];
    }
    
    public void setAngle(Double angle){
        this.angle = angle.floatValue();
    }
}
