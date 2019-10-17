package com.lhg1304.onimani;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.kakao.auth.Session;
import com.lhg1304.onimani.common.LoginActivity;
import com.lhg1304.onimani.common.utils.PermissionUtils;
import com.lhg1304.onimani.views.MainActivity;
import com.lhg1304.onimani.views.PlaceSelectActivity;

import java.security.MessageDigest;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.layout_splash);

        //        키해시 얻기
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.nexmore.rnd", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("Key Hash", "Key Hash : " + Base64.encodeToString(md.digest(), Base64.NO_WRAP));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if ( PermissionUtils.permissionCheck(SplashActivity.this) ) {
            nextActivity();
        } else {
            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PlaceSelectActivity.REQUEST_LOCATION);
        }

    }

    private void nextActivity() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( requestCode == PlaceSelectActivity.REQUEST_LOCATION ) {    // 위치 권한
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                // 권한 거부
                finish();
            } else {
                // 권한 승낙
                nextActivity();
            }

        }
    }
}
