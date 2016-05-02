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

import android.app.Activity;
import android.content.Context;

import com.mytechia.commons.di.container.IDIContainer;
import com.mytechia.commons.di.container.PicoContainerWrapper;
import com.mytechia.commons.framework.exception.InternalErrorException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;

/**
 * Manages the startup and shutdown of Robobo modules
 *
 * The modules can be populated usinga  Properties file like in the example below:
 *
 *      Properties modules = new Properties();
 *      modules.put("robobo.module.0", "com.mytechia.robobo.framework.DummyTestModule1");
 *      modules.put("robobo.module.1", "com.mytechia.robobo.framework.DummyTestModule2");
 *
 *      FrameworkManager frameworkManager = FrameworkManager.instantiate(modules, null);
 *
 *
 * @author Gervasio Varela
 */
public class FrameworkManager 
{

    private final String MODULE_LOADER_KEY = "robobo.module.%d";


    private final Properties modulesFile;

    private Activity mainActivity;
    private Context androidContext;

    private final LinkedList<IModule> modules;
    private final IDIContainer diContainer;
    
    private int modulesIndex = 0;



    private FrameworkManager(Properties modulesFile, Activity mainActivity) {
        this.modulesFile = modulesFile;
        this.mainActivity = mainActivity;
        if (mainActivity != null) this.androidContext = mainActivity.getApplicationContext();
        this.modules = new LinkedList<>();
        this.diContainer = new PicoContainerWrapper();
    }


    /** Instantiates a new Framework manager. mainActivity can be null, in such a case the
     * Android context will not be available for modules.
     *
     * @param modulesFile a properties file with the modules to load
     * @param mainActivity the main Activity of the Robobo Android application
     */
    public static final FrameworkManager instantiate(Properties modulesFile, Activity mainActivity) {
        return new FrameworkManager(modulesFile, mainActivity);
    }


    /** Starts the framework and the configured Robobo modules.
     *
     * @throws InternalErrorException if there was an error while loading the modules
     */
    public void startup() throws InternalErrorException {
        
        while(isNextModule()) {
            
            try {
                
                IModule module = registerNextModule();
                module.startup(this);
                
            } catch (ClassNotFoundException ex) {
                throw new InternalErrorException(ex);
            } catch (InstantiationException ex) {
                throw new InternalErrorException(ex);
            } catch (IllegalAccessException ex) {
                throw new InternalErrorException(ex);
            }
            
        }
        
    }


    /** Shutdowns the framework and all the modules
     *
     * @throws InternalErrorException if there was an error while shutting down the modules
     */
    public void shutdown() throws InternalErrorException {
    
        Iterator<IModule> modulesIterator = this.modules.descendingIterator();
        
        while(modulesIterator.hasNext()) {
            
            IModule module = modulesIterator.next();
            module.shutdown();            
            
        }
        
        modulesIterator = this.modules.descendingIterator();
        
        while(modulesIterator.hasNext()) {
            IModule module = modulesIterator.next();
            this.diContainer.unregister(module.getClass());
            modulesIterator.remove();
        }
        
    }
    
    
    
    /** Checks whether there is more subsystems to load or not
     *
     * @return true if there is more subsystems to load, false otherwise
     */
    private boolean isNextModule() {
        return this.modulesIndex < this.modulesFile.values().size();
    }
    
    
    /** Loads the next subsystem loader and instantiates the loader
     *
     * @return an instance of the next subsystem loader 
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException 
     */
    private IModule registerNextModule() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final String propertyName = String.format(MODULE_LOADER_KEY, this.modulesIndex++);
        
        String ssLoaderClassName = modulesFile.getProperty(propertyName);
        
        Objects.requireNonNull(ssLoaderClassName, String.format("Not found property=%s", propertyName));
        
        Class ssLoaderClass = Class.forName(ssLoaderClassName);
        
        IModule module = (IModule) ssLoaderClass.newInstance(); 
        
        registerModuleInstance(module, ssLoaderClass);
        
        return module;
    }


    /** Returns the instance of the request Robobo module
     *
     * @param moduleClass the class of the module requested
     * @param <T>
     * @return the instance of the module requested
     */
    public <T> T getModuleInstance(Class<T> moduleClass) {
        
        Objects.requireNonNull(moduleClass, "The parameter clazz is required");
        
        return this.diContainer.getInstance(moduleClass);
        
    }


    /** Registers a module instance in this manager
     *
     * @param module the module instance
     * @param moduleClass the module class
     */
    void registerModuleInstance(IModule module, Class moduleClass) {
        
        if (module == null) return;
        
        Objects.requireNonNull(moduleClass, "The parameter clazz is required");
        
        this.diContainer.registerSingleton(moduleClass, module);
        this.modules.add(module);
        
    }


    /** Removes a module instance from this manager
     *
     * @param module the module instance to remove
     */
    void removeModule(IModule module) {
        
        Objects.requireNonNull(module, "The parameter module is required");
        
        this.diContainer.unregister(module.getClass());
        this.modules.remove(module);
        
    }


    /** Return the Android application context of this Robobo application
     *
     * @return the Android application context of this Robobo application
     */
    public Context getApplicationContext() {
        return this.androidContext;
    }
    
}