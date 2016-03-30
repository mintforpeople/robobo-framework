/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package com.mytechia.robobo.framework.test;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.FrameworkManager;
import com.mytechia.robobo.framework.IModule;

/**
 * Description of the class 
 *
 * @author Gervasio Varela
 */
public class DummyTestModule implements IModule
{
    
    private final int id;
    
    public DummyTestModule(int id) {
        this.id = id;
    }
    

    @Override
    public void startup(FrameworkManager manager) throws InternalErrorException {
        System.out.println(String.format("Starting up dummy module %d!", id));
    }

    @Override
    public void shutdown() throws InternalErrorException {
        System.out.println(String.format("Shutting down dummy module %d!", id));
    }

}
