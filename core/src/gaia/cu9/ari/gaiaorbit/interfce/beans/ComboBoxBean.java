package gaia.cu9.ari.gaiaorbit.interfce.beans;

public class ComboBoxBean {
    public String name;
    public int value;

    public ComboBoxBean(String name, int samples) {
        super();
        this.name = name;
        this.value = samples;
    }

    @Override
    public String toString() {
        return name;
    }

}