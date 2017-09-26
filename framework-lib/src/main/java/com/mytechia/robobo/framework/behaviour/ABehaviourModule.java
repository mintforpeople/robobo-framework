/*******************************************************************************
 *
 *   Copyright 2017 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2017 Gervasio Varela <gervasio.varela@mytechia.com>
 *   Copyright 2017 Julio Gomez <julio.gomez@mytechia.com>
 *   Copyright 2017 Luis Llamas <luis.llamas@mytechia.com>
 *
 *   This file is part of Robobo Ros Module.
 *
 *   Robobo Ros Module is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo Ros Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo Ros Module.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.mytechia.robobo.framework.behaviour;

import android.os.Handler;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.IModule;
import com.mytechia.robobo.framework.RoboboManager;

/** An abstract class to facilitate the implemention of new behaviours for the Robobo robot
 *
 * User must override three methods:
 * startBehaviour() -- To setup the behaviour execution, like suscribing listeners and accesing robobo modules
 * stopBehaviour() -- To release resources, like unsuscribing listeners
 * runStep() -- To execute some logic periodically
 *
 * @author Gervasio Varela | gervasio.varela@mytechia.com
 *
 */
public abstract class ABehaviourModule implements IModule {

    private static final int MIN_PERIOD = 10;
    private static final int DEFAULT_PERIOD = 50;


    /** Robobo manager instance */
    private RoboboManager robobo = null;

    /** Handler for periodic behaviour execution */
    private Handler handler = null;
    /** Code to run each period */
    private Runnable runnableCode = null;

    /** Period of each step run */
    private int stepPeriod = DEFAULT_PERIOD;



    /** Returns the current Robobo manager instace
     *
     * @return the current Robobo manager instace
     */
    public final RoboboManager getRobobo() {
        return this.robobo;
    }


    /** Changes the period of execution of the runStep() method.
     * By default is 50 ms (20 times/second)
     * The minimum allowed is 10 ms (100 times/second)
     *
     * @param period period of execution of the runStep() method
     */
    public final void setPeriod(int period) {
        if (period >= MIN_PERIOD) {
            this.stepPeriod = period;
        }
        else {
            this.stepPeriod = MIN_PERIOD;
        }
    }


    /** Starts the periodic execution of the behaviour by intializing a handler and setting
     * it to run periodically
     *
     * @param manager The instance of the Robobo manager framework
     * @throws InternalErrorException if there is an unexpected error during start-up
     */
    @Override
    public final void startup(RoboboManager manager) throws InternalErrorException {

        this.robobo = manager;

        startBehaviour();

        this.handler = new Handler();

        this.runnableCode = new Runnable() {
            @Override
            public void run() {

                runStep();

                handler.postDelayed(this, stepPeriod);
            }
        };

        this.handler.post(runnableCode);

    }


    /** Stops the priodic execution of the behaviour
     *
     * @throws InternalErrorException if there is an unexpected error during shutdown
     */
    @Override
    public final void shutdown() throws InternalErrorException {

        stopBehaviour();

        this.handler.removeCallbacks(this.runnableCode);

    }


    /** Users must override this method with the code required to start-up the execution of the
     * behaviour, like getting instances of Robobo modules or any other resource.
     *
     * @throws InternalErrorException
     */
    protected abstract void startBehaviour() throws InternalErrorException;

    /** Users must override this method with the core required to stop the execution of the
     * behaviour, like freeing some resource.
     *
     * @throws InternalErrorException
     */
    protected abstract void stopBehaviour() throws InternalErrorException;

    /** Users must override this method with the particular logic for their Robobo behaviour.
     * This method is called periodically, so each 'run' must be 'short in time' and return the control quickly.
     * If some heavy processing is required it should be done in a different thread.
     * This method should only be used to read sensors and send orders to actuators.
     */
    protected abstract void runStep();




}
