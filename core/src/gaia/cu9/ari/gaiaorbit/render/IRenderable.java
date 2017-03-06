package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.util.GSEnumSet;

/**
 * A top-level renderable interface that all renderable objects must extend.
 * @author Toni Sagrista
 *
 */
public interface IRenderable {

    /**
     * Gets the component types of this entity.
     * @return The component types
     */
    public GSEnumSet<ComponentType> getComponentType();

    /**
     * Gets the last distance to the camera calculated for this entity.
     * @return The distance.
     */
    public float getDistToCamera();

}
