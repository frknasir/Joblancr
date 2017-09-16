package com.joblancr.activitiesAndAdapters;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.joblancr.helpers.SessionManager;

public class SplashScreenActivity extends AppCompatActivity {
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        sessionManager = new SessionManager(SplashScreenActivity.this);

        Thread timerThread = new Thread() {
            public void run() {
                try{
                    sleep(1000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }finally {
                    if(sessionManager.isLoggedIn()) {
                        Intent i = new Intent(SplashScreenActivity.this, DashboardActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
