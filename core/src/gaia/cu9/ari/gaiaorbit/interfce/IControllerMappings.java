package gaia.cu9.ari.gaiaorbit.interfce;

public interface IControllerMappings {

    /**
     * Returns the code of the axis that produces:
     * <ul>
     * <li>Roll rotation in focus mode</li>
     * <li>Horizontal lateral movement in free mode</li>
     * </ul>
     * 
     * @return The axis code, negative if not mapped
     */
    public int getAxisRoll();

    /**
     * Returns the code of the axis that produces:
     * <ul>
     * <li>Vertical rotation around focus in focus mode</li>
     * <li>Vertical look rotation (pitch) in free mode</li>
     * </ul>
     * 
     * @return The axis code, negative if not mapped
     */
    public int getAxisPitch();

    /**
     * Returns the code of the axis that produces:
     * <ul>
     * <li>Horizontal rotation around focus in focus mode</li>
     * <li>Horizontal look rotation (yaw) in free mode</li>
     * </ul>
     * 
     * @return The axis code, negative if not mapped
     */
    public int getAxisYaw();

    /**
     * Returns the code of the axis that controls the forward and backward
     * movement
     * 
     * @return The axis code, negative if not mapped
     */
    public int getAxisMove();

    /**
     * Returns the code of the axis used to increase the velocity. All the range
     * of the axis is used. Usually mapped to a trigger button.
     * 
     * @return The axis code, negative if not mapped
     */
    public int getAxisVelocityUp();

    /**
     * Returns the code of the axis used to decrease the velocity. All the range
     * of the axis is used. Usually mapped to a trigger button.
     * 
     * @return The axis code, negative if not mapped
     */
    public int getAxisVelocityDown();

    /**
     * Returns the code of the button that, when pressed, multiplies the
     * velocity vector by 0.1.
     * 
     * @return The button code, negative if not mapped
     */
    public int getButtonVelocityMultiplierTenth();

    /**
     * Returns the code of the button that, when pressed, multiplies the
     * velocity vector by 0.5.
     * 
     * @return The button code, negative if not mapped
     */
    public int getButtonVelocityMultiplierHalf();

    /**
     * Returns the code of the button used to increase the velocity.
     * 
     * @return The button code, negative if not mapped
     */
    public int getButtonVelocityUp();

    /**
     * Returns the code of the button used to decrease the velocity.
     * 
     * @return The button code, negative if not mapped
     */
    public int getButtonVelocityDown();

}
