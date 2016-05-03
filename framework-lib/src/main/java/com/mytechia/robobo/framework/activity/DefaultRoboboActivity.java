package com.mytechia.robobo.framework.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mytechia.commons.framework.exception.InternalErrorException;
import com.mytechia.robobo.framework.FrameworkListener;
import com.mytechia.robobo.framework.FrameworkManager;
import com.mytechia.robobo.framework.FrameworkState;
import com.mytechia.robobo.framework.R;

import java.io.IOException;
import java.util.Properties;

public abstract class DefaultRoboboActivity extends Activity implements FrameworkListener {

    private FrameworkManager roboboFramework;
    private Class mainActivityClass;

    private TextView txtStatus;
    private Button btnContinue;
    private Button btnExit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default_robobo);

        txtStatus = (TextView) findViewById(R.id.txtStatus);

        this.btnExit = (Button) findViewById(R.id.btnExit);
        this.btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });

        this.btnContinue = (Button) findViewById(R.id.btnContinue);
        this.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchMainActivity();
            }
        });

        new InitRoboboFrameworkTask().execute();

    }


    protected abstract void startRoboboApplication();



    protected void setMainActivityClass(Class activityClass) {
        this.mainActivityClass = activityClass;
    }

    protected Class getMainActivityClass() {
        return this.mainActivityClass;
    }



    protected FrameworkManager getRoboboFramework() {
        return this.roboboFramework;
    }


    protected void launchMainActivity() {
        if (getMainActivityClass() != null) {
            Intent myIntent = new Intent(DefaultRoboboActivity.this, getMainActivityClass());
            DefaultRoboboActivity.this.startActivity(myIntent);
        }
    }


    protected void showErrorDialog(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(DefaultRoboboActivity.this);

        builder.setTitle("Robobo Framework Error").
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


    private class InitRoboboFrameworkTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {

                initFramework();

                startRoboboApplication();

                launchMainActivity();

            } catch (IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorDialog("Unable to read Robobo Framework Configuration.");
                    }
                });
            } catch (InternalErrorException e) {
                final String errorMsg = e.getMessage();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showErrorDialog("Unable to load module class:" + errorMsg);
                    }
                });
            }

            return null;

        }

    }


    public void initFramework() throws IOException, InternalErrorException {

        Properties modulesProperties = new Properties();
        modulesProperties.load(getApplicationContext().getAssets().open("modules.properties"));

        this.roboboFramework = FrameworkManager.instantiate(modulesProperties, this);

        this.roboboFramework.addFrameworkListener(this);

        this.roboboFramework.startup();

    }


    @Override
    public void loadingModule(final String moduleInfo, String moduleVersion) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.setText("Loading module: "+moduleInfo);
            }
        });
    }

    @Override
    public void moduleLoaded(String moduleInfo, String moduleVersion) {

    }

    @Override
    public void frameworkStateChanged(FrameworkState state) {

        if (state == FrameworkState.RUNNING) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtStatus.setText("Robobo is running!");
                }
            });
        }
    }
}
