package com.mytechia.robobo.framework;

import com.mytechia.commons.di.container.IDIContainer;
import com.mytechia.commons.di.container.PicoContainerWrapper;
import com.mytechia.commons.framework.exception.InternalErrorException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;

/**
 * Manages the startup and shutdown of modules
 *
 * @author Gervasio Varela
 */
public class FrameworkManager 
{
    
    private final String MODULE_LOADER_KEY = "robobo.module.%d";
    

    private final Properties modulesFile;
    
    private final LinkedList<IModule> modules;
    private final IDIContainer diContainer;
    
    private int modulesIndex = 0;
    
    
    
    
    private FrameworkManager(Properties modulesFile) {
        this.modulesFile = modulesFile;
        this.modules = new LinkedList<>();
        this.diContainer = new PicoContainerWrapper();
    }
    
    
    public static final FrameworkManager instantiate(Properties modulesFile) {
        return new FrameworkManager(modulesFile);
    }
    
    
    void startup() throws InternalErrorException {
        
        while(isNextModule()) {
            
            try {
                
                IModule module = registerNextModule();
                this.modules.add(module);
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
    
    
    void shutdown() throws InternalErrorException {
    
        Iterator<IModule> modulesIterator = this.modules.descendingIterator();
        
        while(modulesIterator.hasNext()) {
            
            IModule module = modulesIterator.next();
            module.shutdown();            
            
        }
        
        modulesIterator = this.modules.descendingIterator();
        
        while(modulesIterator.hasNext()) {
            removeModule(modulesIterator.next());
        }
        
    }
    
    
    
    /** Checks whether there is more subsystems to load or not
     * 
     * @param ssLoadersConfig properties file with the list of subsystem loaders
     * @return true if there is more subsystems to load, false otherwise
     */
    private boolean isNextModule() {
        return this.modulesIndex < this.modulesFile.values().size();
    }
    
    
    /** Loads the next subsystem loader and instantiates the loader
     * 
     * @param modulesFile properties file with the list of subsystem loaders
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
    
    
    
    
    public <T> T getModuleInstance(Class<T> moduleClass) {
        
        Objects.requireNonNull(moduleClass, "The parameter clazz is required");
        
        return this.diContainer.getInstance(moduleClass);
        
    }
    
    
    void registerModuleInstance(IModule module, Class moduleClass) {
        
        if (module == null) return;
        
        Objects.requireNonNull(moduleClass, "The parameter clazz is required");
        
        this.diContainer.registerSingleton(moduleClass, module);
        this.modules.add(module);
        
    }            
            
    
    void removeModule(IModule module) {
        
        Objects.requireNonNull(module, "The parameter module is required");
        
        this.diContainer.unregister(module.getClass());
        this.modules.remove(module);
        
    }
    
}
