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

import android.app.Application;
import android.content.Context;
import android.os.Binder;

import com.mytechia.commons.di.container.IDIContainer;
import com.mytechia.commons.di.container.PicoContainerWrapper;
import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;

/**
 * Manages the startup and shutdown of Robobo modules
 *
 * The modules can be populated using a  Properties file like in the example below:
 *
 *      Properties modules = new Properties();
 *      modules.put("robobo.module.0", "com.mytechia.robobo.framework.DummyTestModule1");
 *      modules.put("robobo.module.1", "com.mytechia.robobo.framework.DummyTestModule2");
 *
 *      RoboboManager frameworkManager = RoboboManager.instantiate(modules, null);
 *
 *
 * @author Gervasio Varela
 */
public class RoboboManager extends Binder
{

    private final String MODULE_LOADER_KEY = "robobo.module.%d";


    private final Properties modulesFile;

    private Application app;

    private final LinkedList<IModule> modules;
    private final IDIContainer diContainer;
    
    private int modulesIndex = 0;

    private RoboboManagerState state = RoboboManagerState.CREATED;

    private final ArrayList<RoboboManagerListener> listeners;


    private static RoboboManager _instance = null;


    /**
     *
     * @param modulesFile
     * @param app
     */
    private RoboboManager(Properties modulesFile, Application app) {
        this.modulesFile = modulesFile;
        this.app = app;
        this.modules = new LinkedList<>();
        this.diContainer = new PicoContainerWrapper();
        this.listeners = new ArrayList<>(2);
    }


    /** Instantiates a new Framework manager. mainActivity can be null, in such a case the
     * Android context will not be available for modules.
     *
     * @param modulesFile a properties file with the modules to load
     * @param app the Robobo Android application
     * @return a new instance of RoboboManager
     */
    public static final RoboboManager instantiate(Properties modulesFile, Application app) {
        _instance = new RoboboManager(modulesFile, app);
        return _instance;
    }


    /** Returns the current singleton instance of the RoboboManager
     *
     * @return the current singleton instance of the FrameworkMangaer
     */
    public static final RoboboManager getInstance() {
        return _instance;
    }


    /** Starts the framework and the configured Robobo modules.
     *
     * @throws InternalErrorException if there was an error while loading the modules
     */
    public void startup() throws InternalErrorException {

        if (state == RoboboManagerState.CREATED) {

            while (isNextModule()) {

                try {

                    IModule module = registerNextModule();

                    notifyLoadingModule(module);

                    module.startup(this);

                    notifyModuleLoaded(module);

                } catch (ClassNotFoundException ex) {
                    throw new InternalErrorException(ex.getMessage());
                } catch (InstantiationException ex) {
                    throw new InternalErrorException(ex);
                } catch (IllegalAccessException ex) {
                    throw new InternalErrorException(ex);
                }

            }

            frameworkStateChanged(RoboboManagerState.ALL_MODULES_LOADED);

            frameworkStateChanged(RoboboManagerState.RUNNING);

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
     * @return the instance of the module requested
     */
    public <T> T getModuleInstance(Class<T> moduleClass) throws ModuleNotFoundException {
        
        Objects.requireNonNull(moduleClass, "The parameter clazz is required");

        T module = this.diContainer.getInstance(moduleClass);

        if (module == null) {
            throw new ModuleNotFoundException(moduleClass.getCanonicalName());
        }
        else {
            return module;
        }

    }


    public Collection<IModule> getAllModules() {

        return new ArrayList<>(this.modules);

    }


    /** Registers a module instance in this manager
     *
     * @param module the module instance
     * @param moduleClass the module class
     */
    synchronized void registerModuleInstance(IModule module, Class moduleClass) {
        
        if (module == null) return;
        
        Objects.requireNonNull(moduleClass, "The parameter clazz is required");
        
        this.diContainer.registerSingleton(moduleClass, module);
        this.modules.add(module);
        
    }


    /** Removes a module instance from this manager
     *
     * @param module the module instance to remove
     */
    synchronized void removeModule(IModule module) {
        
        Objects.requireNonNull(module, "The parameter module is required");
        
        this.diContainer.unregister(module.getClass());
        this.modules.remove(module);
        
    }


    /** Return the Android application context of this Robobo application
     *
     * @return the Android application context of this Robobo application
     */
    public Context getApplicationContext() {
        return this.app.getApplicationContext();
    }


    public boolean isStartedUp() {
        return this.state == RoboboManagerState.RUNNING;
    }


    private void frameworkStateChanged(RoboboManagerState state) {

        this.state = state;

        for(RoboboManagerListener listener : this.listeners) {
            listener.frameworkStateChanged(state);
        }

    }


    private void notifyLoadingModule(IModule module) {

        String moduleInfo = module.getModuleInfo();
        String moduleVersion = module.getModuleVersion();

        for(RoboboManagerListener listener : this.listeners) {
            listener.loadingModule(moduleInfo, moduleVersion);
        }

    }

    private void notifyModuleLoaded(IModule module) {

        String moduleInfo = module.getModuleInfo();
        String moduleVersion = module.getModuleVersion();

        for(RoboboManagerListener listener : this.listeners) {
            listener.moduleLoaded(moduleInfo, moduleVersion);
        }

    }


    public void addFrameworkListener(RoboboManagerListener listener) {
        this.listeners.add(listener);
    }

    public void removeFrameworkListener(RoboboManagerListener listener) {
        this.listeners.remove(listener);
    }

}
