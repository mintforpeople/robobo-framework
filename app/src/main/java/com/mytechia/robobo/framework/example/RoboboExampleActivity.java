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
package com.mytechia.robobo.framework.example;

import android.os.Bundle;

import com.mytechia.robobo.framework.FrameworkManager;
import com.mytechia.robobo.framework.activity.DefaultRoboboActivity;
import com.mytechia.robobo.framework.example.dummy.DummyTestModule1;

/** An example of how to use DefaultRoboboActivity to easily build a new
 * custom Robobo Application.
 *
 * 1st it overrides onCreate() to sets the Display Activity for this application.
 * 2nd it implements the custom robobo application code in a Runnable object.
 * 3rd it implements startRoboboApplication() to initialize the custom application code thread.
 *
 */
public class RoboboExampleActivity extends DefaultRoboboActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //set the display activity class
        setMainActivityClass(RoboboCustomMainActivity.class);
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void startRoboboApplication() {

        //start the application code in a new thread
        Thread t = new Thread(new RoboboApp(getRoboboFramework()));
        t.start();

    }


    private class RoboboApp implements Runnable {

        private FrameworkManager roboboManager;
        private DummyTestModule1 roboboModule1;

        public RoboboApp(FrameworkManager roboboManager) {
            this.roboboManager = roboboManager;
            roboboModule1 = this.roboboManager.getModuleInstance(DummyTestModule1.class);
        }


        @Override
        public void run() {

            //do whatever is required for the application like check sensors, issue commands
            //to the ROB, process data, etc.
            //this code can be run in a loop, por example
            /*************************************************
                while(!stop) {
                    readSensors();
                    doSomethingWithData();
                    issueCommandsToHardware();
                    waitForNewData(); //it's recommended to left some time free por the system
                }
            **************************************************/

            System.out.println("Doing something usefull with the Robobo modules!");
            System.out.println("Robobo module = "+this.roboboModule1.getModuleVersion());

        }

    }

}
