/*******************************************************************************
 *
 *   Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2016 Gervasio Varela <gervasio.varela@mytechia.com>
 *
 *   This file is part of Robobo Framework Library.
 *
 *   Robobo Framework Library is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Robobo Framework Library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with Robobo Framework Library.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

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
 * (threads or whatever) for their operation, but they must free all resources
 * on shutdown.
 * 
 * The modules receives an instance of the RoboboManager during startup. It
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
    public void startup(RoboboManager manager) throws InternalErrorException;
    
    
    
    /** Terminates the execution of the module and frees all the resources.
     * 
     * @throws com.mytechia.commons.framework.exception.InternalErrorException if there was any error during shutdown
     */
    public void shutdown() throws InternalErrorException;

    /**
     * Gets the information from the module
     * @return information string
     */
    public String getModuleInfo();

    /**
     * Gets the version number
     * @return version number
     */
    public String getModuleVersion();
    
}
