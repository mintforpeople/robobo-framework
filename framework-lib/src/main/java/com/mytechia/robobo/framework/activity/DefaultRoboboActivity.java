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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mytechia.robobo.framework.FrameworkListener;
import com.mytechia.robobo.framework.FrameworkManager;
import com.mytechia.robobo.framework.FrameworkState;
import com.mytechia.robobo.framework.R;
import com.mytechia.robobo.framework.service.RoboboService;

import java.util.Properties;

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
 *   and use the RoboboFramework using FrameworkManager.instantiate()
 *
 */
public abstract class DefaultRoboboActivity extends Activity {

    private FrameworkManager roboboFramework;
    private boolean frameworkStarted = false;
    private Class displayActivityClass;
    private Properties modulesProperties;

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


    /** Sets the properties file used to load a custom set of Robobo modules.
     * This method should be called in the onCreate() method of custom
     * applications. If the properties is not set, the framwork will try to start
     * with a default set of modules.
     *
     * @param modulesProperties properties file with the modules configuration
     */
    protected void setModulesProperties(Properties modulesProperties) {
        this.modulesProperties = modulesProperties;
    }


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
    protected FrameworkManager getRoboboFramework() {
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

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtStatus.setText(getText(R.string.msg_framework_running));
                    btnContinue.setEnabled(true);
                }
            });

        }

    }


    protected void bindRoboboService() {

        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                roboboFramework = (FrameworkManager) service;

                roboboFramework.addFrameworkListener(new RoboboListener());

                if (roboboFramework.isStartedUp()) {
                    frameworkStarted();
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


    private class RoboboListener implements FrameworkListener {

        @Override
        public void loadingModule(final String moduleInfo, String moduleVersion) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtStatus.setText(getText(R.string.msg_loading_module).toString() + moduleInfo);
                }
            });
        }

        @Override
        public void moduleLoaded(String moduleInfo, String moduleVersion) {

        }

        @Override
        public void frameworkStateChanged(FrameworkState state) {

            if (state == FrameworkState.RUNNING) {
                frameworkStarted();
            }
        }

    }
}
