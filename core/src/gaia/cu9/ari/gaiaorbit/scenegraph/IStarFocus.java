package gaia.cu9.ari.gaiaorbit.scenegraph;

public interface IStarFocus extends IFocus {

    /**
     * Gets the catalog source of this star. Possible values are:
     * <ul>
     * <li>-1: Unknown</li>
     * <li>1: Gaia</li>
     * <li>2: Hipparcos (HYG)</li>
     * <li>3: Tycho</li>
     * <li>4: Other</li>
     * </ul>
     * 
     * @return The catalog source number
     */
    public int getCatalogSource();

    /**
     * Returns the identifier
     * 
     * @return The identifier of this star
     */
    public long getId();

    /**
     * Returns the HIP number of this star, or negative if it has no HIP number
     * 
     * @return The HIP number
     */
    public int getHip();

    /**
     * Returns the TYC string identifier or null if it does not have one
     * 
     * @return The TYC string
     */
    public String getTycho();

}
