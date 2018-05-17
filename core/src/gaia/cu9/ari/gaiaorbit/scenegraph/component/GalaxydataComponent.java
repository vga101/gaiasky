package gaia.cu9.ari.gaiaorbit.scenegraph.component;

public class GalaxydataComponent {
    /** Star positions/sizes **/
    public String starsource;

    /** Bulge **/
    public String bulgesource;

    /** Dust positions/sizes **/
    public String dustsource;

    /** Nebulae - deprecated **/
    public String nebulasource;

    public GalaxydataComponent() {

    }

    public void setStarsource(String starsource) {
        this.starsource = starsource;
    }

    public void setDustsource(String dustsource) {
        this.dustsource = dustsource;
    }

    public void setNebulasource(String nebulasource) {
        this.nebulasource = nebulasource;
    }

    public void setBulgesource(String bulgesource) {
        this.bulgesource = bulgesource;
    }
}
