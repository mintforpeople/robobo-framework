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

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;

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
    public void testStartup() {

        Properties modules = new Properties();
        modules.put("robobo.module.0", "com.mytechia.robobo.framework.example.dummy.DummyTestModule1");
        modules.put("robobo.module.1", "com.mytechia.robobo.framework.example.dummy.DummyTestModule2");


        RoboboManager frameworkManager = RoboboManager.instantiate(modules, null, null);

        try {

            frameworkManager.startup();

            assertNotNull(frameworkManager.getModuleInstance(DummyTestModule1.class));
            assertNotNull(frameworkManager.getModuleInstance(DummyTestModule2.class));


            frameworkManager.shutdown();


        } catch (InternalErrorException ex) {
            Logger.getLogger(FrameworkManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test(expected=ModuleNotFoundException.class)
    public void testShutdown() throws InternalErrorException{

        Properties modules = new Properties();
        modules.put("robobo.module.0", "com.mytechia.robobo.framework.example.dummy.DummyTestModule1");
        modules.put("robobo.module.1", "com.mytechia.robobo.framework.example.dummy.DummyTestModule2");


        RoboboManager frameworkManager = RoboboManager.instantiate(modules, null, null);



            frameworkManager.startup();

            assertNotNull(frameworkManager.getModuleInstance(DummyTestModule1.class));
            assertNotNull(frameworkManager.getModuleInstance(DummyTestModule2.class));


            frameworkManager.shutdown();

            assertNull(frameworkManager.getModuleInstance(DummyTestModule1.class));
            assertNull(frameworkManager.getModuleInstance(DummyTestModule2.class));





    }

    
    
    
}
