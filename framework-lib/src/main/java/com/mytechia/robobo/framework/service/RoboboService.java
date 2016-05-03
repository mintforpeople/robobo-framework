package com.mytechia.robobo.framework.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RoboboService extends Service {




    public RoboboService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
