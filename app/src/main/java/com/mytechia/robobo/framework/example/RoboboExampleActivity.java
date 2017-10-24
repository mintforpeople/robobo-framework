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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.mytechia.robobo.framework.LogLvl;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.example.dummy.DummyTestModule1;
import com.mytechia.robobo.framework.exception.ModuleNotFoundException;
import com.mytechia.robobo.framework.service.RoboboServiceHelper;

import java.io.FileOutputStream;


/** An example of how to use DefaultRoboboActivity to easily build a new
 * custom Robobo Application.
 *
 * 1st it uses the RoboboServiceHelper to start the RoboboManager and obtain an instance of it
 * 2nd it implements the custom robobo application code in a Runnable object.
 * 3rd it starts the thread when the Robobo Manager has finished starting up all modules
 *
 */
public class RoboboExampleActivity extends Activity {

    private RoboboServiceHelper roboboHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robobo_custom_main);

        roboboHelper = new RoboboServiceHelper(this, new RoboboServiceHelper.Listener() {
            @Override
            public void onRoboboManagerStarted(RoboboManager roboboManager) {
                RoboboApp app = new RoboboApp(roboboManager);
                Thread t = new Thread(app);
                t.start();
            }

            @Override
            public void onError(Throwable ex) {}

        });

        Bundle roboboOptions = new Bundle();
        roboboOptions.putString("robobo.name", "HC-06");
        roboboHelper.bindRoboboService(roboboOptions);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        roboboHelper.unbindRoboboService();
        finish();
    }


    private class RoboboApp implements Runnable {

        private RoboboManager roboboManager;
        private DummyTestModule1 roboboModule1;

        public RoboboApp(RoboboManager roboboManager) {
            this.roboboManager = roboboManager;
            //Initializing log file
            String string = "";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput("log.txt", Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                roboboModule1 = this.roboboManager.getModuleInstance(DummyTestModule1.class);
            }
            catch(ModuleNotFoundException ex) {
                this.roboboManager.log(LogLvl.ERROR,"ROBOBO-APP", "Module not found: "+ex.getMessage());
            }


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

            this.roboboManager.log("ROBOBO-APP","Doing something usefull with the Robobo modules!");
            this.roboboManager.log("ROBOBO-APP","Robobo module = "+this.roboboModule1.getModuleVersion());

        }

    }

}
