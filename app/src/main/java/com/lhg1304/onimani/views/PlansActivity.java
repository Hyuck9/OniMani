package com.lhg1304.onimani.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.usermgmt.response.model.UserProfile;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.models.Plan;
import com.lhg1304.onimani.models.User;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlansActivity extends AppCompatActivity {

    @BindView(R.id.plans_map_view)
    MapView mMapView;

    private String mPlanId;

    private UserProfile mUserProfile;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mAllUserDBRef;
    private DatabaseReference mMyDBRef;
    private DatabaseReference mPlanDBRef;
    private DatabaseReference mMemberDBRef;

    private MapPOIItem mPlaceMarker;
    private List<MapPOIItem> mMemberMarkerList;
    private List<Bitmap> mThumbnailList;

    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);
        ButterKnife.bind(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mPlanId = getIntent().getStringExtra("plan_id");
        Log.d("test", "planId : "+mPlanId);

        mUserProfile = UserProfile.loadFromCache(); // User Session
        mFirebaseDatabase = FirebaseDatabase.getInstance(); // firebase database init
        mMemberMarkerList = new ArrayList<>();
        mThumbnailList = new ArrayList<>();
        initDBRef();
        initAppointmentPlace();
        requestMyLocation();
        memberLocationListener();
    }

    private void initDBRef() {
        mAllUserDBRef = mFirebaseDatabase.getReference("users");
        mMyDBRef = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId()));
        mPlanDBRef = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId())).child("plans").child(mPlanId);
        mMemberDBRef   = mFirebaseDatabase.getReference("meeting_members").child(mPlanId);
    }

    private void initAppointmentPlace() {
        mPlanDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Plan plan = dataSnapshot.getValue(Plan.class);
                createPlaceMarker(plan);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void createPlaceMarker(Plan plan) {
        mPlaceMarker = new MapPOIItem();
        String name = plan.getPlace();
        mPlaceMarker.setItemName(name);
        mPlaceMarker.setTag(0);
        mPlaceMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(plan.getLatitude(), plan.getLongitude()));
        mPlaceMarker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        mPlaceMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        mMapView.addPOIItem(mPlaceMarker);
        mMapView.selectPOIItem(mPlaceMarker, true);
        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(plan.getLatitude(), plan.getLongitude()), true);

        User user = new User();
        user.setThumbUrl(mUserProfile.getThumbnailImagePath());
    }

    private void createMemberMarker(int position) {
        MapPOIItem memberMarker = mMemberMarkerList.get(position);
        mMapView.removePOIItem(memberMarker);
        mMapView.addPOIItem(memberMarker);
        mMapView.selectPOIItem(memberMarker, true);
    }




    private void requestMyLocation() {
        long minTime = 5000;
        float minDistance = 0;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                minTime,
                minDistance,
                mLocationListener
        );
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                minTime,
                minDistance,
                mLocationListener
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(mLocationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestMyLocation();
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
            Log.d("test", test);

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
        mMemberDBRef.child(String.valueOf(mUserProfile.getId())).updateChildren(geoPoint);
    }

    private void memberLocationListener() {
        mMemberDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);

                MapPOIItem memberMarker = new MapPOIItem();
                String name = user.getNickName();
                memberMarker.setItemName(name);
                memberMarker.setTag(1);
                memberMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(user.getLatitude(), user.getLongitude()));
                memberMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                mMemberMarkerList.add(memberMarker);
                mThumbnailList.add(null);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                Log.d("test", "geoPoint : "+user.getLatitude()+", "+user.getLongitude());

                if (mThumbnailList.get(user.getMemberIndex()) != null) {
                    MapPOIItem memberMarker = mMemberMarkerList.get(user.getMemberIndex());
                    String name = user.getNickName();
                    memberMarker.setItemName(name);
                    memberMarker.setTag(1);
                    memberMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(user.getLatitude(), user.getLongitude()));
                    memberMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                    memberMarker.setCustomImageBitmap(mThumbnailList.get(user.getMemberIndex()));
                    memberMarker.setCustomImageAutoscale(false);
                    memberMarker.setCustomImageAnchor(0.5f, 0.5f);
                    mMemberMarkerList.set(user.getMemberIndex(), memberMarker);
                    createMemberMarker(user.getMemberIndex());
                } else {
                    imageDownload(user);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void imageDownload(User user) {
        @SuppressLint("StaticFieldLeak")
        AsyncTask at = new AsyncTask() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Object doInBackground(Object[] objects) {
                Bitmap bitmap = null;
                User user = (User) objects[0];
                try {
                    URL url = new URL(user.getThumbUrl());
                    bitmap = BitmapFactory.decodeStream((InputStream)url.getContent());
                } catch (IOException e) {
                }

                MapPOIItem memberMarker = mMemberMarkerList.get(user.getMemberIndex());
                String name = user.getNickName();
                memberMarker.setItemName(name);
                memberMarker.setTag(1);
                memberMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(user.getLatitude(), user.getLongitude()));
                memberMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                memberMarker.setCustomImageBitmap(bitmap);
                memberMarker.setCustomImageAutoscale(false);
                memberMarker.setCustomImageAnchor(0.5f, 0.5f);
                mMemberMarkerList.set(user.getMemberIndex(), memberMarker);
                mThumbnailList.set(user.getMemberIndex(), bitmap);

                return user.getMemberIndex();
            }

            @Override
            protected void onPostExecute(Object o) {
                createMemberMarker((Integer) o);
            }
        };

        at.execute(user);
    }


}
