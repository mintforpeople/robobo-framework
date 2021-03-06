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


package com.mytechia.robobo.framework.example.dummy;

import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.IModule;

/**
 *
 * @author Gervasio Varela
 */
public class DummyTestModule implements IModule
{
    
    private final int id;
    private RoboboManager m;
    
    public DummyTestModule(int id) {
        this.id = id;
    }
    

    @Override
    public void startup(RoboboManager manager) throws InternalErrorException {
        System.out.println(String.format("Starting up dummy module %d!", id));
        m = manager;
        m.log(this.getClass().getSimpleName(), String.format("Starting up dummy module %d!", id));

    }

    @Override
    public void shutdown() throws InternalErrorException {
        m.log(this.getClass().getSimpleName(), String.format("Shutting down dummy module %d!", id));

    }

    @Override
    public String getModuleInfo() {
        return "Dummy Test Module";
    }

    @Override
    public String getModuleVersion() {
        return "0.1";
    }

}
