/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mytechia.robobo.framework.test;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.FrameworkManager;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Gervasio Varela
 */
public class FrameworkManagerTest {
    
    public FrameworkManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    
    @Test
    public void testStartup()
    {
        
        Properties modules = new Properties();
        modules.put("robobo.module.0", "com.mytechia.robobo.framework.test.DummyTestModule1");
        modules.put("robobo.module.1", "com.mytechia.robobo.framework.test.DummyTestModule2");
        
        
        FrameworkManager frameworkManager = FrameworkManager.instantiate(modules);
        
        try {
            
            frameworkManager.startup();
            
            assertNotNull(frameworkManager.getModuleInstance(DummyTestModule1.class));
            assertNotNull(frameworkManager.getModuleInstance(DummyTestModule2.class));
            
            
            
            
            
            frameworkManager.shutdown();
            
            assertNull(frameworkManager.getModuleInstance(DummyTestModule1.class));
            assertNull(frameworkManager.getModuleInstance(DummyTestModule2.class));
            
            
        } catch (InternalErrorException ex) {
            Logger.getLogger(FrameworkManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    
    
}
