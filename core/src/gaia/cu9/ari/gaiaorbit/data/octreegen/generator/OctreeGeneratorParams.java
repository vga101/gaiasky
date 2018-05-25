package gaia.cu9.ari.gaiaorbit.data.octreegen.generator;

public class OctreeGeneratorParams {

    public int maxPart;
    public boolean sunCentre;
    public double maxDistanceCap = 1e6;
    public boolean postprocessEmpty = false;

    public OctreeGeneratorParams(int maxPart, boolean sunCentre, boolean postprocessEmpty) {
        super();
        this.maxPart = maxPart;
        this.sunCentre = sunCentre;
        this.postprocessEmpty = postprocessEmpty;
    }
    public OctreeGeneratorParams(int maxPart, boolean sunCentre) {
        super();
        this.maxPart = maxPart;
        this.sunCentre = sunCentre;
    }

    public OctreeGeneratorParams(boolean sunCentre) {
        super();
        this.sunCentre = sunCentre;
    }

}
