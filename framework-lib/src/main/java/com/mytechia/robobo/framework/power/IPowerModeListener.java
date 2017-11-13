package com.mytechia.robobo.framework.power;

/**
 * Listener of changes in power mode
 */

public interface IPowerModeListener
{


    /**
     * Called when the power mode changes
     * @param newMode current power mode
     */
    void onPowerModeChange(PowerMode newMode);



}
