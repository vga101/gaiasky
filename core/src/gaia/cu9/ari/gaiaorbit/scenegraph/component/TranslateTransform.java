package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import com.badlogic.gdx.math.Matrix4;

public class TranslateTransform implements ITransform {
    /** Translation **/
    float[] vector;
    

    public void apply(Matrix4 mat){
        mat.translate(vector[0], vector[1], vector[2]);
    }
    
    public void setVector(double[] vector){
        this.vector = new float[vector.length];
        for(int i =0; i< vector.length; i++)
            this.vector[i] = (float) vector[i];
    }
}
