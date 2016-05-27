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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.mytechia.robobo.framework.RoboboManager;
import com.mytechia.robobo.framework.RoboboManagerListener;
import com.mytechia.robobo.framework.RoboboManagerState;

/** A helper class to facilitate the bind/unbind and the
 * management of the start process of the of the Robobo Manager
 * using the Robobo Service.
 *
 * @author Gervasio Varela
 */
public class RoboboServiceHelper {


    private Activity activity;

    private ServiceConnection connection;

    private RoboboManager roboboManager;

    private Listener listener;


    public RoboboServiceHelper(Activity activity, Listener listener) {
        this.activity = activity;
        this.listener = listener;
    }


    /** Starts the Robobo Manager and binds the activity to the service.
     * Then the manager has finished loading all the modules, the Listener
     * interface is used to notify the instance of the Robobo Manager.
     */
    public void bindRoboboService() {

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                if (service != null) {
                    roboboManager = (RoboboManager) service;

                    //it is possible that the framework has not yet finished starting
                    roboboManager.addFrameworkListener(new RoboboListener());

                    //or may be it has finished
                    if (roboboManager.isStartedUp()) {
                        frameworkStarted();
                    }
                }
                else {
                    listener.onError("Unable to find Robobo service.");
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                roboboManager = null;
            }
        };

        Intent intent = new Intent(activity, RoboboService.class);
        activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }


    /** Unbinds the activity from the service.
     */
    public void unbindRoboboService() {

        activity.unbindService(connection);

    }


    /** Shows a new activity in the Robobo display.
     * This activity can also use the helper to access the RoboboManager.
     */
    public void launchDisplayActivity(Class activityClass) {
        if (activityClass != null) {
            Intent myIntent = new Intent(this.activity, activityClass);
            activity.startActivity(myIntent);
        }
    }


    private void frameworkStarted() {

        //notify the finish of the bindin and startup process
        listener.onRoboboManagerStarted(this.roboboManager);

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
            else if (state == RoboboManagerState.ERROR) {
                listener.onError("Unable to start the Robobo Manager.");
            }
        }

    }


    /** Callback interface that must be used to be notified of the successul startup (or not)
     * of the Robobo Manager, and to obtain an instance of it.
     */
    public interface Listener {

        void onRoboboManagerStarted(RoboboManager roboboManaer);

        void onError(String errorMsg);

    }

}
