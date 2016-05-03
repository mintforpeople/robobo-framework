package com.mytechia.robobo.framework.example;

import android.os.Bundle;

import com.mytechia.robobo.framework.FrameworkManager;
import com.mytechia.robobo.framework.app.DefaultRoboboActivity;

public class RoboboTestActivity extends DefaultRoboboActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRoboboApp(new ExampleRoboboApp());
        super.onCreate(savedInstanceState);
    }

}
