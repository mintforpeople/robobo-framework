package com.mytechia.robobo.framework;

import com.mytechia.commons.framework.exception.InternalErrorException;

/**
 * This interface must be implemented in order to implement a module for Robobo
 * robot. 
 * 
 * It only defines how this moudles are going to be loaded and unloaded 
 * so that the framework can manage the startup and stop of the robot.
 * 
 * Once started, a module is free to use and create any resource required
 * (threads or whatever) for their operation, but they must free all resource
 * on shutdown.
 * 
 * The modules receives an instance of the FrameworkManager during startup. It
 * can be used to obtain instances of other modules that are requiered by
 * this module to operate. Modules are started in order, so that a module must be 
 * started after their dependencies.
 *
 * @author Gervasio Varela
 */
public interface IModule 
{

    /** Starts the module execution.
     * 
     * @param manager the framework manager can be used to obtains instances of other modules required
     * @throws com.mytechia.commons.framework.exception.InternalErrorException if there was any error during startup
     */
    public void startup(FrameworkManager manager) throws InternalErrorException;
    
    
    
    /** Terminates the execution of the module and frees all the resources.
     * 
     * @throws com.mytechia.commons.framework.exception.InternalErrorException if there was any error during shutdown
     */
    public void shutdown() throws InternalErrorException;
    
}
