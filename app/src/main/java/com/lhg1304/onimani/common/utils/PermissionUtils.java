package com.lhg1304.onimani.common.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by lhg1304 on 2017-12-14.
 */

public class PermissionUtils {

    public static boolean permissionCheck(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // GPS 권한 거절 상태
            return false;
        } else {
            // GPS 권한 승낙 상태
            return true;
        }
    }
}
