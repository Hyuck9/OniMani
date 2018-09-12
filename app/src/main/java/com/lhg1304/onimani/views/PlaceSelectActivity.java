package com.lhg1304.onimani.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.lhg1304.onimani.R;
import com.lhg1304.onimani.models.User;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaceSelectActivity extends AppCompatActivity implements /*MapView.CurrentLocationEventListener,*/ MapReverseGeoCoder.ReverseGeoCodingResultListener, MapView.MapViewEventListener {

    @BindView(R.id.select_place_map_view)
    MapView mMapView;

//    @BindView(R.id.tv_address)
//    TextView mAddress;

    @BindView(R.id.btn_next)
    Button mNextButton;

    @BindView(R.id.et_address)
    EditText mAddress;

    @BindView(R.id.selected_location)
    ImageView mImgSelected_location;

    /*private LocationManager locationManager;*/

    public final static int REQUEST_LOCATION = 100;    // 위치 권한 요청 CODE

//    private MapPoint.GeoCoordinate mSelectedGeoPoint = null;
    private MapReverseGeoCoder mReverseGeoCoder = null;

    private ArrayList<User> mFriendList = null;
    private double mLatitude, mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_select);
        ButterKnife.bind(this);

//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mFriendList = (ArrayList<User>) getIntent().getSerializableExtra("myFriends");

//        List<String> list = locationManager.getAllProviders();
//        String test = "";
//        for (String str : list) {
//            test += "위치제공자 : " + str + ", 사용가능여부 - " + locationManager.isProviderEnabled(str) + "\n";
//        }
//        Log.d("test", test);

        /*mMapView.setCurrentLocationEventListener(this);*/
        mMapView.setMapViewEventListener(this);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = "";
                if ( mAddress.getText().toString().isEmpty() ) {
                    address = mAddress.getHint().toString();
                } else {
                    address = mAddress.getText().toString();
                }
                Intent intent = new Intent(PlaceSelectActivity.this, CreateRoomActivity.class);
                intent.putExtra("myFriends", mFriendList);
                intent.putExtra("address", address);
                intent.putExtra("latitude", mLatitude);
                intent.putExtra("longitude", mLongitude);
                startActivity(intent);
                finish();
            }
        });
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( requestCode == REQUEST_LOCATION ) {    // 위치 권한
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                // 권한 거부
                finish();
            } else {
                // 권한 승낙
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
//                requestMyLocation();
            }

        }
    }*/

    private void onFinishReverseGeoCoding(String result) {
        mAddress.setHint(result);
    }



    /* ** ReverseGeoCodingResultListener ** */
    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {
        mapReverseGeoCoder.toString();
        onFinishReverseGeoCoding(s);
    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {
        onFinishReverseGeoCoding("주소 정보 없음");
    }
    /* -- ReverseGeoCodingResultListener -- */



    /* ** CurrentLocationEventListener ** */
    /*@Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        if ( mapView.getCurrentLocationTrackingMode() == MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading ) {
            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving);
        }
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i("PlaceSelectActivity", String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }*/
    /* -- CurrentLocationEventListener -- */



    /* ** MapViewEventListener ** */
    @Override
    public void onMapViewInitialized(MapView mapView) {
        /*mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);*/
        mMapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.56640625, 126.97787475585938), 5, true);
        MapPoint.GeoCoordinate mapPointGeo = mMapView.getMapCenterPoint().getMapPointGeoCoord();
        mLatitude = mapPointGeo.latitude;
        mLongitude = mapPointGeo.longitude;
        Log.d("test", "onCreate - 현재 위치 : "+mLatitude+", "+mLongitude);

        mReverseGeoCoder = new MapReverseGeoCoder(getString(R.string.kakao_app_key), mapView.getMapCenterPoint(), this, this);
        mReverseGeoCoder.startFindingAddress();
    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapCenterPoint) {
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {
    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        mImgSelected_location.setImageResource(R.drawable.map_pin2);
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        mImgSelected_location.setImageResource(R.drawable.map_pin);
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapView.getMapCenterPoint().getMapPointGeoCoord();
        mLatitude = mapPointGeo.latitude;
        mLongitude = mapPointGeo.longitude;
        mReverseGeoCoder = new MapReverseGeoCoder(getString(R.string.kakao_app_key), mapView.getMapCenterPoint(), this, this);
        mReverseGeoCoder.startFindingAddress();
    }
    /* -- MapViewEventListener -- */
}
