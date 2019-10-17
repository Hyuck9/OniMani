package com.lhg1304.onimani.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlansActivity extends AppCompatActivity {

    @BindView(R.id.plans_map_view)
    MapView mMapView;

    private String mPlanId;

    private UserProfile mUserProfile;
    private FirebaseDatabase mFirebaseDatabase;
    /*private DatabaseReference mAllUserDBRef;*/
    /*private DatabaseReference mMyDBRef;*/
    private DatabaseReference mPlanDBRef;
    private DatabaseReference mMemberDBRef;

    private MapPOIItem mPlaceMarker;
    private List<MapPOIItem> mMemberMarkerList;
    private List<Bitmap> mThumbnailList;

    private LocationManager locationManager;

    private float density;
    /*private int memberCount = 0;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);
        ButterKnife.bind(this);

        density = getDeviceDensity();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mPlanId = getIntent().getStringExtra("plan_id");
        Log.d("test", "planId : "+mPlanId);

        mUserProfile = UserProfile.loadFromCache(); // User Session
        mFirebaseDatabase = FirebaseDatabase.getInstance(); // firebase database init
        mMemberMarkerList = new ArrayList<>();
        mThumbnailList = new ArrayList<>();
        initDBRef();
        initAppointmentPlace();
//        requestMyLocation();
//        setMyLocation();
        memberLocationListener();
    }

    private void initDBRef() {
        /*mAllUserDBRef = mFirebaseDatabase.getReference("users");
        mMyDBRef = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId()));*/
        mPlanDBRef = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId())).child("plans").child(mPlanId);
        mMemberDBRef   = mFirebaseDatabase.getReference("meeting_members").child(mPlanId);
    }

    /*private void test() {
        mMemberDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                memberCount = dataSnapshot.child("memberCount").getValue(int.class) == null ? 0 : dataSnapshot.child("memberCount").getValue(int.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }*/

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
        mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(plan.getLatitude(), plan.getLongitude()), 6, true);

        User user = new User();
        user.setThumbUrl(mUserProfile.getThumbnailImagePath());
    }

    private void createMemberMarker(int position) {
        MapPOIItem memberMarker = mMemberMarkerList.get(position);
        mMapView.removePOIItem(memberMarker);
        mMapView.addPOIItem(memberMarker);
        mMapView.selectPOIItem(memberMarker, true);
    }




   /* private void requestMyLocation() {
        long minTime = 10000;   // 위치 갱신 최소 시간 (milliSecond)
        float minDistance = 5;  // 위치 갱신 최소 거리 (meter)
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
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        memberLocationListenerStop();
        /*locationManager.removeUpdates(mLocationListener);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*requestMyLocation();*/
    }

    /*private LocationListener mLocationListener = new LocationListener() {
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
    };*/

    /*private void updateMyLocation(double latitude, double longitude) {
        Map<String, Object> geoPoint = new HashMap<String, Object>();
        geoPoint.put("latitude", latitude);
        geoPoint.put("longitude", longitude);
        mMyDBRef.updateChildren(geoPoint);
        mMemberDBRef.child(String.valueOf(mUserProfile.getId())).updateChildren(geoPoint);
    }*/

   /* private void setMyLocation() {
        mMyDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User me = dataSnapshot.getValue(User.class);
                updateMyLocation(me.getLatitude(), me.getLongitude());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }*/

    ChildEventListener childEventListener = new ChildEventListener() {
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


            imageDownload(user);
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
                memberMarker.setCustomImageBitmap(getMemberMarker(mThumbnailList.get(user.getMemberIndex())));
                memberMarker.setCustomImageAutoscale(false);
                memberMarker.setCustomImageAnchor(0.5f, 1.0f);
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
    };
    private void memberLocationListenerStop() {
        mMemberDBRef.removeEventListener(childEventListener);
    }
    private void memberLocationListener() {
        mMemberDBRef.addChildEventListener(childEventListener);
    }


    /*private void imageDownload(User user) {
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

                *//*MapPOIItem memberMarker = mMemberMarkerList.get(user.getMemberIndex());*//*
                MapPOIItem memberMarker = new MapPOIItem();
                String name = user.getNickName();
                memberMarker.setItemName(name);
                memberMarker.setTag(1);
                memberMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(user.getLatitude(), user.getLongitude()));
                memberMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                memberMarker.setCustomImageBitmap(getMemberMarker(bitmap));
                memberMarker.setCustomImageAutoscale(false);
                memberMarker.setCustomImageAnchor(0.5f, 1.0f);
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
    }*/
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
                    e.getStackTrace();
                }

                MapPOIItem memberMarker = new MapPOIItem();
                String name = user.getNickName();
                memberMarker.setItemName(name);
                memberMarker.setTag(1);
                memberMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(user.getLatitude(), user.getLongitude()));
                memberMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                memberMarker.setCustomImageBitmap(getMemberMarker(bitmap));
                memberMarker.setCustomImageAutoscale(false);
                memberMarker.setCustomImageAnchor(0.5f, 1.0f);
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



    private Bitmap getMemberMarker(Bitmap img) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_map_person_on).copy(Bitmap.Config.ARGB_8888, true);

        double w = bitmap.getWidth() / 1.36; // 넓이 가져오기
        double h = bitmap.getHeight() / 1.36; // 높이 가져오기

        Bitmap reSizeBitmap = null;

        // copyBitmap에 들어갈 이미지의 사이즈를 재정의
        if (img != null) {
            reSizeBitmap = Bitmap.createScaledBitmap(img, (int) w, (int) h, true);
        } else {
            reSizeBitmap = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getResources(), R.drawable.kakao_default_profile_image), (int) w, (int) h,
                    true);
        }

        float left = 8 * density;
        float top = left - 1;

        Canvas canvas = new Canvas(bitmap);

        canvas.drawBitmap(setRoundCorner(reSizeBitmap), left, top, null); // canvas1에 reSizeBitmap를 그리는 옵션
        return bitmap;
    }


    private Bitmap setRoundCorner(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        float roundPx = 0;
        roundPx = density * 75;

        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        paint.setColor(color);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private float getDeviceDensity() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.density;
    }

}
