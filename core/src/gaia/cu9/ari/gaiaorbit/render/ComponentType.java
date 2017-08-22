package gaia.cu9.ari.gaiaorbit.render;

import java.util.HashMap;
import java.util.Map;

import gaia.cu9.ari.gaiaorbit.util.I18n;

public enum ComponentType {
    Stars("icon-elem-stars"), // 0000 0000 0000 0000 0001
    Planets("icon-elem-planets"), // 0000 0000 0000 0000 0010
    Moons("icon-elem-moons"), // 0000 0000 0000 0000 0100
    Satellites("icon-elem-satellites"), // 0000 0000 0000 0000 1000
    Asteroids("icon-elem-asteroids"), // 0000 0000 0000 0001 0000
    Labels("icon-elem-labels"), // 0000 0000 0000 0010 0000
    Equatorial("icon-elem-equatorial"), // 0000 0000 0000 0100 0000
    Ecliptic("icon-elem-ecliptic"), // 0000 0000 0000 1000 0000
    Galactic("icon-elem-galactic"), // 0000 0000 0001 0000 0000
    Orbits("icon-elem-orbits"), // 0000 0000 0010 0000 0000
    Atmospheres("icon-elem-atmospheres"), // 0000 0000 0100 0000 0000
    Constellations("icon-elem-constellations"), // 0000 0000 1000 0000 0000
    Boundaries("icon-elem-boundaries"), // 0000 0001 0000 0000 0000
    MilkyWay("icon-elem-milkyway"), // 0000 0010 0000 0000 0000
    Galaxies("icon-elem-galaxies"), // 0000 0100 0000 0000 0000
    Countries("icon-elem-countries"), // 0000 1000 0000 0000 0000
    Locations("icon-elem-locations"), // 0001 0000 0000 0000 0000
    Meshes("icon-elem-meshes"), // 0010 0000 0000 0000 0000
    Others("icon-elem-others"); // 0011 0000 0000 0000 0000

    private static Map<String, ComponentType> keysMap = new HashMap<String, ComponentType>();

    static {
        for (ComponentType ct : ComponentType.values()) {
            keysMap.put(ct.key, ct);
        }
    }

    public String key;
    public String style;

    private ComponentType(String icon) {
        this.key = "element." + name().toLowerCase();
        this.style = icon;
    }

    public String getName() {
        return I18n.bundle.get(key);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static ComponentType getFromKey(String key) {
        return keysMap.get(key);
    }
}
