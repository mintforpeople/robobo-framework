/*******************************************************************************
 *
 *   Copyright 2016 Mytech Ingenieria Aplicada <http://www.mytechia.com>
 *   Copyright 2016 Gervasio Varela <gervasio.varela@mytechia.com>
 *   Copyright 2017 Luis Llamas <luis.llamas@mytechia.com>
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
import android.os.Bundle;
import android.util.Log;


import com.mytechia.commons.di.container.IDIContainer;
import com.mytechia.commons.di.container.PicoContainerWrapper;
import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.frequency.FrequencyMode;
import com.mytechia.robobo.framework.frequency.IFrequencyModeListener;
import com.mytechia.robobo.framework.power.IPowerModeListener;
import com.mytechia.robobo.framework.power.PowerMode;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;


/**
 * Manages the startup and shutdown of Robobo modules
 * <p>
 * The modules can be populated using a  Properties file like in the example below:
 * <p>
 * Properties modules = new Properties();
 * modules.put("robobo.module.0", "com.mytechia.robobo.framework.DummyTestModule1");
 * modules.put("robobo.module.1", "com.mytechia.robobo.framework.DummyTestModule2");
 * <p>
 * RoboboManager frameworkManager = RoboboManager.instantiate(modules, null);
 *
 * @author Gervasio Varela
 */
public class RoboboManager extends Binder {


    public static final String TAG = "ROBOBO-MANAGER";
    private final String MODULE_LOADER_KEY = "robobo.module.%d";


    private final Properties modulesFile;
    private final Bundle options;
    private Application app;

    private final LinkedList<IModule> modules;
    private final IDIContainer diContainer;

    private int modulesIndex = 0;
    private int modulesCount = 0;

    private RoboboManagerState state = RoboboManagerState.CREATED;

    private Throwable exception;

    private PowerMode powerMode = PowerMode.NORMAL;
    private FrequencyMode frequencyMode = FrequencyMode.NORMAL;
    private boolean powerManagement = true;

    private final ArrayList<RoboboManagerListener> listeners;
    private final ArrayList<IPowerModeListener> powerModeListeners;
    private final ArrayList<IFrequencyModeListener> frequencyModeListeners;


    private static RoboboManager _instance = null;

    private Logger log;
    LoggerContext lc;


    private RoboboManager(Properties modulesFile, Bundle options, Application app) {
        this.modulesFile = modulesFile;
        this.options = options == null ? new Bundle() : options;
        this.app = app;
        this.modules = new LinkedList<>();
        this.diContainer = new PicoContainerWrapper();
        this.listeners = new ArrayList<>(2);
        this.powerModeListeners = new ArrayList<>(5);
        this.frequencyModeListeners = new ArrayList<>(5);


        lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        StatusPrinter.print(lc);
        log = LoggerFactory.getLogger("com.mytechia.robobo.framework");
        log.info("Starting Robobo Framework");

    }


    /**
     * Instantiates a new Framework manager. mainActivity can be null, in such a case the
     * Android context will not be available for modules.
     *
     * @param modulesFile a properties file with the modules to load
     * @param options     a Bundle opbjec with optional parameters for the framework and modules
     * @param app         the Robobo Android application
     * @return a new instance of RoboboManager
     */
    public static final RoboboManager instantiate(Properties modulesFile, Bundle options, Application app) {
        _instance = new RoboboManager(modulesFile, options, app);
        return _instance;
    }


    public RoboboManagerState state() {
        return this.state;
    }


    /**
     * Starts the framework and the configured Robobo modules.
     *
     * @throws InternalErrorException if there was an error while loading the modules
     */
    public synchronized void startup() throws InternalErrorException {

        this.exception = null;

        if (state == RoboboManagerState.CREATED) {

            log(LogLvl.INFO, TAG, "Starting up Robobo Manager.");

            while (isNextModule()) {

                try {

                    IModule module = registerNextModule();

                    notifyLoadingModule(module);

                    module.startup(this);

                    notifyModuleLoaded(module);

                } catch (ClassNotFoundException ex) {
                    logError(TAG, "Error loading module", ex);
                    InternalErrorException newEx = new InternalErrorException("Module not found: " + ex.getMessage());
                    frameworkError(newEx);
                    throw newEx;
                } catch (InstantiationException ex) {
                    logError(TAG, "Error loading module", ex);
                    InternalErrorException newEx = new InternalErrorException(ex);
                    frameworkError(ex);
                    throw newEx;
                } catch (IllegalAccessException ex) {
                    logError(TAG, "Error loading module", ex);
                    InternalErrorException newEx = new InternalErrorException(ex);
                    frameworkError(ex);
                    throw newEx;
                } catch (InternalErrorException ex) {
                    logError(TAG, "Error loading module", ex);
                    frameworkError(ex);
                    throw ex;
                }

            }

            frameworkStateChanged(RoboboManagerState.ALL_MODULES_LOADED);

            frameworkStateChanged(RoboboManagerState.RUNNING);

        }

    }


    /**
     * Shutdowns the framework and all the modules
     *
     * @throws InternalErrorException if there was an error while shutting down the modules
     */
    public synchronized void shutdown() throws InternalErrorException {


        log(LogLvl.INFO, TAG, "Shutting down Robobo Manager.");

        Iterator<IModule> modulesIterator = this.modules.descendingIterator();

        while (modulesIterator.hasNext()) {

            IModule module = modulesIterator.next();
            try {
                module.shutdown();
            }catch (Throwable th){
                logError(TAG, "Error shutdown module", th);
            }

        }

        modulesIterator = this.modules.descendingIterator();

        while (modulesIterator.hasNext()) {
            IModule module = modulesIterator.next();
            this.diContainer.unregister(module.getClass());
            modulesIterator.remove();
        }

        //Stops the logging system
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        loggerContext.stop();

    }


    public Bundle getOptions() {
        return this.options;
    }


    public Throwable exception() {
        return this.exception;
    }


    /**
     * Checks whether there is more subsystems to load or not
     *
     * @return true if there is more subsystems to load, false otherwise
     */
    private boolean isNextModule() {
        return this.modulesCount < this.modulesFile.values().size();
    }


    /**
     * Loads the next subsystem loader and instantiates the loader
     *
     * @return an instance of the next subsystem loader
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private IModule registerNextModule() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String key = String.format(MODULE_LOADER_KEY, this.modulesIndex++);

        while (!modulesFile.containsKey(key)) {
            key = String.format(MODULE_LOADER_KEY, this.modulesIndex++);
        }

        final String propertyName = key;
        String ssLoaderClassName = modulesFile.getProperty(propertyName);

        Objects.requireNonNull(ssLoaderClassName, String.format("Not found property=%s", propertyName));

        Class ssLoaderClass = Class.forName(ssLoaderClassName);

        IModule module = (IModule) ssLoaderClass.newInstance();

        registerModuleInstance(module, ssLoaderClass);
        modulesCount++;
        return module;
    }


    /**
     * Returns the instance of the request Robobo module
     *
     * @param moduleClass the class of the module requested
     * @return the instance of the module requested
     */
    public <T> T getModuleInstance(Class<T> moduleClass) throws ModuleNotFoundException {

        Objects.requireNonNull(moduleClass, "The parameter class is required");

        T module = this.diContainer.getInstance(moduleClass);

        if (module == null) {
            throw new ModuleNotFoundException(moduleClass.getCanonicalName());
        } else {
            return module;
        }

    }


    public Collection<IModule> getAllModules() {

        return new ArrayList<>(this.modules);

    }


    /**
     * Registers a module instance in this manager
     *
     * @param module      the module instance
     * @param moduleClass the module class
     */
    synchronized void registerModuleInstance(IModule module, Class moduleClass) {

        if (module == null) return;

        Objects.requireNonNull(moduleClass, "The parameter class is required");

        this.diContainer.registerSingleton(moduleClass, module);
        this.modules.add(module);

    }


    /**
     * Removes a module instance from this manager
     *
     * @param module the module instance to remove
     */
    synchronized void removeModule(IModule module) {

        Objects.requireNonNull(module, "The parameter module is required");

        this.diContainer.unregister(module.getClass());
        this.modules.remove(module);

    }


    /**
     * Return the Android application context of this Robobo application
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

        for (RoboboManagerListener listener : this.listeners) {
            listener.frameworkStateChanged(state);
        }

    }


    private void frameworkError(Throwable th) {

        this.exception = th;

        try {
            this.shutdown();
        } catch (InternalErrorException e) {
            logError(TAG, "Error framework shutdown", e);
        }

        frameworkStateChanged(RoboboManagerState.ERROR);

        for (RoboboManagerListener listener : this.listeners) {
            listener.frameworkError(th);
        }

    }

    public void notifyModuleError(Throwable th) {
        this.frameworkError(th);
    }


    private void notifyLoadingModule(IModule module) {

        String moduleInfo = module.getModuleInfo();
        String moduleVersion = module.getModuleVersion();

        for (RoboboManagerListener listener : this.listeners) {
            listener.loadingModule(moduleInfo, moduleVersion);
        }

        log("ROBOBO-MANAGER", "Loading module: "+moduleInfo+" - "+moduleVersion);

    }

    private void notifyModuleLoaded(IModule module) {

        String moduleInfo = module.getModuleInfo();
        String moduleVersion = module.getModuleVersion();

        for (RoboboManagerListener listener : this.listeners) {
            listener.moduleLoaded(moduleInfo, moduleVersion);
        }

    }


    public void addFrameworkListener(RoboboManagerListener listener) {
        this.listeners.add(listener);
    }

    public void removeFrameworkListener(RoboboManagerListener listener) {
        this.listeners.remove(listener);
    }


    /**
     * Logs a message with a tag and Debug logError level,
     * can be configured via logback.xml in thye assets folder of the app
     *
     * @param tag     Tag of the message
     * @param message Body of the message
     */
    public void log(String tag, String message){
        String formattedmessage = tag+": "+message;
        if (log != null) {
            log.debug(formattedmessage);
        }else {
            Log.d(tag, message);
        }

    }

    public void logError(String tag, String message, Throwable th) {

        String formattedmessage = tag + ": " + message;

        if (log != null) {
            log.error(formattedmessage+" ", th);
        } else {
            Log.e(tag, message, th);
        }


    }


    /**
     * Logs a message with a tag and a user defined logError level,
     * can be configured via logback.xml in thye assets folder of the app
     *
     * @param tag     Tag of the message
     * @param message Body of the message
     */
    public void log(LogLvl logLevel, String tag, String message) {

        String formattedmessage = tag + ": " + message;

        if (log != null) {
            switch (logLevel) {
                case DEBUG:
                    log.debug(formattedmessage);
                    break;

                case INFO:
                    log.info(formattedmessage);
                    break;

                case WARNING:
                    log.warn(formattedmessage);
                    break;

                case ERROR:
                    log.error(formattedmessage);
                    break;

                case TRACE:
                    log.trace(formattedmessage);
                    break;
            }
        } else {
            switch (logLevel) {
                case DEBUG:
                    Log.d(tag, message);
                    break;

                case INFO:
                    Log.i(tag, message);
                    break;

                case WARNING:
                    Log.w(tag, message);
                    break;

                case ERROR:
                    Log.e(tag, message);
                    break;

                case TRACE:
                    Log.v(tag, message);
                    break;
            }
        }
    }


    //POWER MODE MANAGEMENT


    /**
     * Enables or disables the power management features of the framework.
     * When disabled, the frame is locked in PowerMode.NORMAL
     * WHen enabled, apps or modules can change the power mode from NORMAL to LOWPOWER and
     * viceversa. In LOWPOWER mode modules are "invited" to free resources and stop
     * consuming processor time and memory.
     *
     * @param enabled whether to enable or disable power management
     */
    public void setPowerManagementEnabled(boolean enabled) {
        this.powerManagement = enabled;
        if (!isPowerManagementEnabled()) {
            forcePowerModeTo(PowerMode.NORMAL);
        }
    }


    /**
     * Checks whether the power management is enabled or not
     *
     * @return whether the power management is enabled or not
     */
    public boolean isPowerManagementEnabled() {
        return this.powerManagement;
    }


    /**
     * Private method to internally change the power mode of the robot.
     * It allows to change the mode without looking at the power management state.
     *
     * @param newMode new power mode.
     */
    private void forcePowerModeTo(PowerMode newMode) {
        if (newMode != this.powerMode) {
            this.powerMode = newMode;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    notifyPowerModeChange();
                }
            });
            t.start();
        }
    }

    /**
     * Changes the power mode to a new one.
     * Only changes the mode if the new one is diferent than the previous one and power
     * management is enabled
     *
     * @param newMode new power mode
     */
    public void changePowerModeTo(PowerMode newMode) {
        if (isPowerManagementEnabled()) {
            forcePowerModeTo(newMode);
        }
    }


    private void notifyPowerModeChange() {
        for (IPowerModeListener l : this.powerModeListeners) {
            l.onPowerModeChange(this.powerMode);
        }
    }

    public void changeFrequencyModeTo(FrequencyMode newMode){
        for (IFrequencyModeListener l : this.frequencyModeListeners) {
            l.onFrequencyModeChanged(newMode);
        }
    }

    /**
     * Subscribes a listener to power mode change events.
     *
     * @param listener the listener to receive power mode change events
     */
    public void subscribeToPowerModeChanges(IPowerModeListener listener) {
        this.powerModeListeners.add(listener);
    }

    /**
     * Unsubscribes a listener to power mode change events.
     *
     * @param listener the listener to unsubscribe
     */
    public void unsubscribeFromPowerModeChanges(IPowerModeListener listener) {
        this.powerModeListeners.remove(listener);
    }

    /**
     * Subscribes a listener to frequency mode change events.
     *
     * @param listener the listener to receive frequency mode change events
     */
    public void subscribeToFrequencyModeChanges(IFrequencyModeListener listener) {
        this.frequencyModeListeners.add(listener);
    }

    /**
     * Unsubscribes a listener to frequency mode change events.
     *
     * @param listener the listener to unsubscribe
     */
    public void unsubscribeFromFrequencyModeChanges(IFrequencyModeListener listener) {
        this.frequencyModeListeners.remove(listener);
    }

}
