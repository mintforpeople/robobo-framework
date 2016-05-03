package com.mytechia.robobo.framework.example;

import android.app.Activity;

import com.mytechia.robobo.framework.FrameworkManager;
import com.mytechia.robobo.framework.app.RoboboApp;

/**
 * Created by gervasio on 3/5/16.
 */
public class ExampleRoboboApp extends RoboboApp {


    @Override
    public void onInit() {

    }

    @Override
    protected void onResume() {

    }

    @Override
    protected void onPause() {

    }

    @Override
    protected void execute() {
        System.out.println("Executing code!");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFinish() {

    }

}
