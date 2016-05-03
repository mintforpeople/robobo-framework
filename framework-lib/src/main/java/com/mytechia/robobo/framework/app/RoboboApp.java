package com.mytechia.robobo.framework.app;

import android.app.Activity;

import com.mytechia.robobo.framework.FrameworkManager;

/**
 * Created by gervasio on 3/5/16.
 */
public abstract class RoboboApp {


    private FrameworkManager roboboManager;
    private Activity mainActivity;

    private boolean end = false;
    private boolean paused = false;


    public RoboboApp() {}


    protected FrameworkManager getRoboboFrameworkManager() {
        return this.roboboManager;
    }

    protected Activity getMainActivity() {
        return this.mainActivity;
    }


    public void start() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                while (!end & !paused) {
                    execute();
                }

                if (end) {
                    onFinish();
                    return;
                }

            }
        });
        t.start();
    }

    public void stop() {
        this.end = true;
    }



    void init(FrameworkManager roboboManager, Activity mainActivity) {
        this.roboboManager = roboboManager;
        this.mainActivity = mainActivity;
        onInit();
    }
    protected abstract void onInit();


    protected void resume() {
        this.paused = false;
        onResume();
    }
    protected abstract void onResume();


    protected void pause() {
        this.paused = true;
        onPause();;
    }
    protected abstract void onPause();

    protected void finish() {
        onFinish();
        stop();
    }
    protected abstract void onFinish();


    protected abstract void execute();



}
