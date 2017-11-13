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


/** A listener that receives life-cycle events from the Robobo Manager
 *
 * @author Gervasio Varela
 */
public interface RoboboManagerListener {

    /**
     * Called when a module starts to load
     * @param moduleInfo information of the module
     * @param moduleVersion version od the module
     */
    void loadingModule(String moduleInfo, String moduleVersion);

    /**
     * Called when a module finish to load
     * @param moduleInfo information of the module
     * @param moduleVersion version od the module
     */
    void moduleLoaded(String moduleInfo, String moduleVersion);

    /**
     * Called when the manager changes its state
     * @param state current manager state
     */
    void frameworkStateChanged(RoboboManagerState state);

    /**
     * Called when the framework throws an exception
     * @param ex the exception
     */
    void frameworkError(Throwable ex);


}
