package com.mytechia.robobo.framework.example;

import android.os.Bundle;

import com.mytechia.robobo.framework.FrameworkManager;
import com.mytechia.robobo.framework.activity.DefaultRoboboActivity;
import com.mytechia.robobo.framework.example.dummy.DummyTestModule1;

public class RoboboTestActivity extends DefaultRoboboActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setMainActivityClass(RoboboCustomMainActivity.class);
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void startRoboboApplication() {

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

            System.out.println("Doing something usefull with the Robobo modules!");
            System.out.println("Robobo module = "+this.roboboModule1.getModuleVersion());

        }

    }

}
