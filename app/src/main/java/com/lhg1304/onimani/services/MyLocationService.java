package com.lhg1304.onimani.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.usermgmt.response.model.UserProfile;
import com.lhg1304.onimani.models.Plan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyLocationService extends Service {

    private LocationManager mLocationManager;
    private UserProfile mUserProfile;
    private DatabaseReference mMyDBRef;
    private DatabaseReference mMemberDBRef;

    private List<Plan> mMyPlanList;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mUserProfile = UserProfile.loadFromCache(); // User Session
        mMyDBRef = firebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId()));
        mMemberDBRef = firebaseDatabase.getReference("meeting_members");

        mMyPlanList = new ArrayList<>();

        getMyPlans();
        requestMyLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if ( mLocationManager != null ) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    private void requestMyLocation() {
        long minTime = 10000;   // 위치 갱신 최소 시간 (milliSecond)
        float minDistance = 0;  // 위치 갱신 최소 거리 (meter)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                mLocationListener
        );
        mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                mLocationListener
        );
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자

            String test = "위치정보 : " + provider + "\n위도 : " + longitude + "\n경도 : " + latitude + "\n고도 : " + altitude + "\n정확도 : "  + accuracy;
            Log.d("MyLocationService", test);

            updateMyLocation(latitude, longitude);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }
        @Override
        public void onProviderEnabled(String s) {
        }
        @Override
        public void onProviderDisabled(String s) {
        }
    };

    private void updateMyLocation(double latitude, double longitude) {
        Map<String, Object> geoPoint = new HashMap<String, Object>();
        geoPoint.put("latitude", latitude);
        geoPoint.put("longitude", longitude);
        mMyDBRef.updateChildren(geoPoint);
        for (Plan plan: mMyPlanList) {
            mMemberDBRef.child(plan.getPlanId()).child(String.valueOf(mUserProfile.getId())).updateChildren(geoPoint);
        }
    }

    private void getMyPlans() {
        mMyDBRef.child("plans").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mMyPlanList.add(dataSnapshot.getValue(Plan.class));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Plan plan = dataSnapshot.getValue(Plan.class);
                for (int i=0; i<mMyPlanList.size(); i++) {
                    if ( mMyPlanList.get(i).getPlanId().equals(plan.getPlanId()) ) {
                        mMyPlanList.remove(i);
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
