package com.lhg1304.onimani.common;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by lhg1304 on 2017-11-27.
 */

public abstract class BaseActivity extends AppCompatActivity {
//    protected void showWaitingDialog() {
//        WaitingDialog.showWaitingDialog(this);
//    }
//
//    protected void cancelWaitingDialog() {
//        WaitingDialog.cancelWaitingDialog();
//    }

    protected void redirectLoginActivity() {
        final Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    protected void redirectSignupActivity() {
        final Intent intent = new Intent(this, SignupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
