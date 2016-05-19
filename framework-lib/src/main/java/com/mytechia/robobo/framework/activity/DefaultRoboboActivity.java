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

package com.mytechia.robobo.framework.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mytechia.robobo.framework.RoboboManagerListener;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.RoboboManagerState;
import com.mytechia.robobo.framework.IModule;
import com.mytechia.robobo.framework.R;
import com.mytechia.robobo.framework.service.RoboboService;

import java.util.Collection;


/** This class provides a skeleton Activity to build new custom Robobo applications.
 *
 * It provides the code required to initialize and load the Robobo Framework, and
 * a skeleton method to provide the custom code of the application.
 *
 * Furthermore, this Activity automatically launches a second activity, known as 'Display Activity'
 * that is going to be shown in the display of the Robobo during the execution of the
 * Application.
 *
 * In order to implement a new Robobo application the developer must chose between two options:
 * - The easy way is to subclass this Activity and override the onCreate method to specify the
 *   Display Activity with the method setDisplayActivityClass(MyActivity.class) and them initialize
 *   a thread with the custom app code by implementing the method startRoboboApplication()
 *
 * - The not so easy way is to implement a custom Android Activity and directly instantiate
 *   and use the RoboboFramework using RoboboManager.instantiate()
 *
 */
public abstract class DefaultRoboboActivity extends Activity {

    private RoboboManager roboboFramework;
    private boolean frameworkStarted = false;
    private Class displayActivityClass;

    private TextView txtStatus;
    private Button btnContinue;
    private Button btnExit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_robobo);

        //start & bind the Robobo service
        bindRoboboService();

        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtStatus.setMovementMethod(new ScrollingMovementMethod());

        /** This button is used to finish and exit the Robobo application
         */
        this.btnExit = (Button) findViewById(R.id.btnExit);
        this.btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });

        /** This button changes the display again to the main activity
         * of this Robobo application
         */
        this.btnContinue = (Button) findViewById(R.id.btnContinue);
        this.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMainActivity();
            }
        });

    }


    /** Custom Robobo applications must override this method
     * to start their custom application code (threads, etc.)
     */
    protected abstract void startRoboboApplication();


    /** Sets the class of the activity that is going to be shown as display for
     * this application.
     *
     * @param activityClass main activity of this Robobo application
     */
    protected void setDisplayActivityClass(Class activityClass) {
        this.displayActivityClass = activityClass;
    }

    /** Gets a references to the class of the activity shown in the display
     * for this application.
     *
     * @return returns the class of the activity shown for this application
     */
    protected Class getDisplayActivityClass() {
        return this.displayActivityClass;
    }


    /** Gets the references to the Robobo Framework manager
     *
     * @return a reference to the Robobo Framework manager
     */
    protected RoboboManager getRoboboFramework() {
        return this.roboboFramework;
    }


    /** Starts the main activity
     */
    protected void launchMainActivity() {
        if (getDisplayActivityClass() != null) {
            Intent myIntent = new Intent(DefaultRoboboActivity.this, getDisplayActivityClass());
            DefaultRoboboActivity.this.startActivity(myIntent);
        }
    }


    /** Shows an error dialog with the message 'msg'
     *
     * @param msg the message to be shown in the error dialog
     */
    protected void showErrorDialog(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(DefaultRoboboActivity.this);

        builder.setTitle(R.string.title_error_dialog).
                setMessage(msg);
        builder.setPositiveButton(R.string.ok_msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                System.exit(0);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    protected void addToStatusLog(CharSequence msg) {

        final CharSequence localMsg = msg;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.append(localMsg);
                txtStatus.append("\n");
            }
        });

    }


    /** Asynchronous task to initialize the Robobo Framework an set up the
     * UI of the application.
     */
    private class InitRoboboFrameworkTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            startRoboboApplication();

            launchMainActivity();

            return null;

        }

    }


    protected synchronized void frameworkStarted() {

        if (!frameworkStarted) {

            frameworkStarted = true;

            //execute custom application
            new InitRoboboFrameworkTask().execute();

            logStartupStatus();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    btnContinue.setEnabled(true);
                }
            });

        }

    }

    private void logStartupStatus() {

        final Collection<IModule> allModules = roboboFramework.getAllModules();

        addToStatusLog(getText(R.string.msg_framework_running));
        addToStatusLog("\n");
        addToStatusLog(getText(R.string.msg_framework_modules_active)+" "+allModules.size());

        for (IModule module : allModules) {
            addToStatusLog(module.getModuleInfo()+" - "+module.getModuleVersion());
        }

    }


    /** Binds the Robobo service to get a valid instance of the Robobo framework.
     */
    protected void bindRoboboService() {

        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                if (service != null) {
                    roboboFramework = (RoboboManager) service;

                    //it is possible that the framework has not yet finished starting
                    roboboFramework.addFrameworkListener(new RoboboListener());

                    //or mayby it has finished
                    if (roboboFramework.isStartedUp()) {
                        frameworkStarted();
                    }
                }
                else {
                    showErrorDialog("Unable to find Robobo service.");
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                roboboFramework = null;
            }
        };

        Intent intent = new Intent(this, RoboboService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }


    private class RoboboListener implements RoboboManagerListener {

        @Override
        public void loadingModule(final String moduleInfo, String moduleVersion) {

        }

        @Override
        public void moduleLoaded(String moduleInfo, String moduleVersion) {

        }

        @Override
        public void frameworkStateChanged(RoboboManagerState state) {

            if (state == RoboboManagerState.RUNNING) {
                //if the framework has finished starting up
                frameworkStarted();
            }
        }

    }
}
