package com.mytechia.robobo.framework.service;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.FrameworkListener;
import com.mytechia.robobo.framework.FrameworkManager;
import com.mytechia.robobo.framework.FrameworkState;
import com.mytechia.robobo.framework.R;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by gervasio on 16/5/16.
 */
public class RoboboService extends Service implements FrameworkListener {

    private int ROBOBO_NOTIFICATION_ID = 808080;

    private NotificationManager mNotificationManager;

    private FrameworkManager roboboManager;
    private boolean roboboManagerIsReady = false;


    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager =
                (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

        showServiceNotification();

        try {

            Properties modules = loadDefaultPropertiesFile();

            roboboManager = FrameworkManager.instantiate(modules, getApplication());
            roboboManager.addFrameworkListener(this);

            startUpManagerInThread();

        }
        catch(IOException ex) {
            Log.e("ROBOBO-FRAMEWORK", ex.getMessage());
            showErrorOnNotification("Unable to read modules configuration.");
        }

    }


    private Properties loadDefaultPropertiesFile() throws IOException {

        Properties modulesProperties = new Properties();
        modulesProperties.load(getApplicationContext().getAssets().open("modules.properties"));

        return modulesProperties;

    }

    private void startUpManagerInThread() {

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    if (roboboManager != null)
                        roboboManager.startup();

                } catch (InternalErrorException e) {
                    Log.e("ROBOBO-FRAMEWORK", e.getMessage());
                    showErrorOnNotification(e.getMessage());
                }

            }
        });

        t.start();

    }


    private void showServiceNotification() {

        Notification.Builder notificationBuilder = getBaseNotifiationBuilder()
                .setContentText("The framework is starting up.");

        startForeground(ROBOBO_NOTIFICATION_ID, notificationBuilder.build());

    }


    private void showErrorOnNotification(String errorMsg) {

        showOnNotification("ERROR: " + errorMsg);

    }


    private void showOnNotification(String msg) {

        Notification.Builder notificationBuilder = getBaseNotifiationBuilder()
                .setContentText(msg);

        mNotificationManager.notify(ROBOBO_NOTIFICATION_ID, notificationBuilder.build());

    }


    private Notification.Builder getBaseNotifiationBuilder() {
        return new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.robobo_logo)
                .setContentTitle("Robobo Framework");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        try {

            if (roboboManager != null)
                roboboManager.shutdown();

        } catch (InternalErrorException e) {
            showErrorOnNotification(e.getMessage());
        }

    }

    @Override
    public IBinder onBind(Intent intent) {

        return this.roboboManager;

    }

    @Override
    public void loadingModule(String moduleInfo, String moduleVersion) {

    }

    @Override
    public void moduleLoaded(String moduleInfo, String moduleVersion) {

    }

    @Override
    public void frameworkStateChanged(FrameworkState state) {

        if (state == FrameworkState.RUNNING) {
            this.roboboManagerIsReady = true;
            showOnNotification("Framework is running.");
        }

    }
}
