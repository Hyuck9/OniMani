package com.lhg1304.onimani;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.kakao.auth.Session;
import com.lhg1304.onimani.common.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_splash);

        findViewById(R.id.splash).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!Session.getCurrentSession().checkAndImplicitOpen()) {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);

                }

            }
        }, 500);
    }
}
