package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.util.ComponentTypes;

/**
 * A top-level renderable interface that all renderable objects must extend
 * 
 * @author Toni Sagrista
 *
 */
public interface IRenderable {

    /**
     * Gets the component types of this entity
     * 
     * @return The component types
     */
    public ComponentTypes getComponentType();

    /**
     * Gets the last distance to the camera calculated for this entity
     * 
     * @return The distance
     */
    public double getDistToCamera();

    /**
     * Returns the opacity of this renderable
     * 
     * @return The opacity
     */
    public float getOpacity();

}
