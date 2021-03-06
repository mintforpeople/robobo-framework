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

package com.mytechia.robobo.framework.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.R;
import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.RoboboManagerListener;
import com.mytechia.robobo.framework.RoboboManagerState;
import java.io.IOException;
import java.util.Properties;


/** An Android service to start and manage the creation of the Robobo Framework instance.
 *
 *
 * @author Gervasio Varela
 */
public class RoboboService extends Service implements RoboboManagerListener {

    public static final String TAG = "RoboboService";
    private RoboboManager roboboManager;


    @Override
    public void onCreate() {
        super.onCreate();




    }


    private void launchRoboboManager(Bundle roboboOptions) {

        try {

            //by default it loads the modules from a config file en 'assets'
            Properties modules = loadDefaultPropertiesFile();
            this.roboboManager = RoboboManager.instantiate(modules, roboboOptions, getApplication());
        }
        catch(IOException ex) {
            Log.e("ROBOBO-FRAMEWORK", ex.getMessage());
            roboboManagerStatupError(ex);
        }

        this.roboboManager.addFrameworkListener(this);

        try {

            if (roboboManager != null) {
                roboboManager.startup();
            }

        } catch (InternalErrorException ex) {
            Log.e("ROBOBO-FRAMEWORK", ex.getMessage());
            roboboManagerStatupError(ex);
        }

    }


    /** Loads the Robobo modules from the default configuration file in 'assets'
     *
     * @return the default properties file with the modules configuration
     * @throws IOException if there was a problem reading the modules configuration file
     */
    private Properties loadDefaultPropertiesFile() throws IOException {

        Properties modulesProperties = new Properties();
        modulesProperties.load(getApplicationContext().getAssets().open("modules.properties"));

        return modulesProperties;

    }



    private void roboboManagerStatupError(Throwable ex) {

        if (roboboManager != null) {
            try {

                roboboManager.shutdown();
            } catch (InternalErrorException e1) {
                Log.e("ROBOBO-FRAMEWORK", e1.getMessage());
            }
        }



    }





    /** Sets-up the basic configuration of the notification (title, etc.)
     *
     * @return A Notification.Builder with a title and a logo
     */
    private Notification.Builder getBaseNotificationBuilder() {
        return new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.robobo_logo)
                .setContentTitle("Robobo Framework");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");

        try {

            if (roboboManager != null) {
                roboboManager.shutdown();
                roboboManager=null;
            }

        } catch (InternalErrorException e) {
            Log.e(TAG, "onDestroy", e);
        }

    }


    private Bundle getRoboboOptions(Intent intent) {
        Bundle roboboOptions = intent.getExtras();
        if (roboboOptions == null) {
            roboboOptions = new Bundle();
        }
        return roboboOptions;
    }

    @Override
    public IBinder onBind(Intent intent) {

        if ((this.roboboManager == null) || (this.roboboManager.state()==RoboboManagerState.STOPPED)) {
            //if the framework has not been started up yet (nobody has binded yet)
            launchRoboboManager(getRoboboOptions(intent));
        }

        return this.roboboManager;

    }

    @Override
    public void loadingModule(String moduleInfo, String moduleVersion) {

    }

    @Override
    public void moduleLoaded(String moduleInfo, String moduleVersion) {

    }

    @Override
    public void frameworkStateChanged(RoboboManagerState state) {



    }

    @Override
    public void frameworkError(Throwable ex) {

    }



}
