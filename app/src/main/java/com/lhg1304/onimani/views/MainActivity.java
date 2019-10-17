package com.lhg1304.onimani.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.helper.log.Logger;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.common.BaseActivity;
import com.lhg1304.onimani.common.utils.PermissionUtils;
import com.lhg1304.onimani.models.User;
import com.lhg1304.onimani.services.MyLocationService;
import com.lhg1304.onimani.views.transitions.FabTransform;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tabs)
    TabLayout mTabLayout;

    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @BindView(R.id.fab_main)
    FloatingActionButton mFabMain;

    @BindView(R.id.fab_add_friend)
    FloatingActionButton mFabAddFriend;

    @BindView(R.id.fab_create_room)
    FloatingActionButton mFabCreateRoom;

    private ViewPagerAdapter mPagerAdapter;

    private UserProfile userProfile;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDBRef;

    private boolean isFabOpen = false;

    private Animation fabOpen, fabClose, fabRClockwise, fabRAntiClockWise;

    public static final int FIND_FRIEND_REQUEST_CODE = 107;

    private boolean isFriendDeleteMode = false;

    private ArrayList<User> mFriendList;

    public void addFriend(User friend) {
        if (mFriendList == null) {
            Log.d("test", "mFriendList is null!!!!!");
            mFriendList = new ArrayList<>();
        } else {
            Log.d("test", "mFriendList.size : "+mFriendList.size());
        }
        mFriendList.add(friend);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mToolbar.setLogo(R.drawable.hg_icon);
        mTabLayout.setupWithViewPager(mViewPager);
        setUpViewPager();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDBRef = mFirebaseDatabase.getReference("users");
        userProfile = UserProfile.loadFromCache();

        mFriendList = new ArrayList<>();

        initalizeAnimation();
        setupFab();

        Intent i = new Intent(this, MyLocationService.class);
        startService(i);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent i = new Intent(this, MyLocationService.class);
        stopService(i);
    }

    private void setupFab() {
        mFabMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( isFriendDeleteMode ) {
                    onClickFriendDelete();
                } else {
                    if ( !isFabOpen ) {
                        fabOpenAction();
                    } else {
                        fabCloseAction();
                    }
                }
            }
        });

        mFabAddFriend.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
                int color = ContextCompat.getColor(MainActivity.this, R.color.fab2_color);;
                FabTransform.addExtras(intent, color, R.drawable.ic_person_add_white_24dp);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(MainActivity.this,
                                mFabAddFriend,
                                getString(R.string.transition_name_add_friend));
                startActivityForResult(intent, FIND_FRIEND_REQUEST_CODE, optionsCompat.toBundle());

            }
        });

        mFabCreateRoom.setOnClickListener(view -> {
//                Intent intent = new Intent(MainActivity.this, CreateRoomActivity.class);

            if ( PermissionUtils.permissionCheck(MainActivity.this) ) {
                startPlaceSelectActivity();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PlaceSelectActivity.REQUEST_LOCATION);
            }
        });
    }

    private void fabOpenAction() {
        mFabMain.startAnimation(fabRClockwise);
        mFabAddFriend.startAnimation(fabOpen);
        mFabCreateRoom.startAnimation(fabOpen);
        mFabAddFriend.setClickable(true);
        mFabCreateRoom.setClickable(true);
        isFabOpen = true;
    }

    private void fabCloseAction() {
        mFabMain.startAnimation(fabRAntiClockWise);
        mFabAddFriend.startAnimation(fabClose);
        mFabCreateRoom.startAnimation(fabClose);
        mFabAddFriend.setClickable(false);
        mFabCreateRoom.setClickable(false);
        isFabOpen = false;
    }

    private void startPlaceSelectActivity() {
        Intent intent = new Intent(MainActivity.this, PlaceSelectActivity.class);
        intent.putExtra("myFriends", mFriendList);
        /*int color = ContextCompat.getColor(MainActivity.this, R.color.fab1_color);;
        FabTransform.addExtras(intent, color, R.drawable.ic_add_location_white_24dp);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(MainActivity.this,
                        mFabCreateRoom,
                        getString(R.string.transition_name_create_room));*/
        startActivity(intent/*, optionsCompat.toBundle()*/);
        fabCloseAction();
    }

    public void fabFriendDeleteMode() {
        isFriendDeleteMode = true;
        mFabMain.startAnimation(fabRClockwise);
    }

    public void fabFriendUnDeleteMode() {
        isFriendDeleteMode = false;
        mFabMain.startAnimation(fabRAntiClockWise);
    }

    private void initalizeAnimation() {
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        fabRClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise);
        fabRAntiClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise);
    }

    private void setUpViewPager() {
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(new AppointFragment(), "약속");
        mPagerAdapter.addFragment(new FriendFragment(), "친구");
        mViewPager.setAdapter(mPagerAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == FIND_FRIEND_REQUEST_CODE ) {
            if ( resultCode == RESULT_OK ) {
               // 친구 탭으로 이동
                mViewPager.setCurrentItem(2, true);

                String inputEmail = data.getStringExtra("email");
                Log.d("MainActivity", "onActivityResult Success!!!! : "+inputEmail+", "+mViewPager.getCurrentItem());
                Fragment currentFragment = mPagerAdapter.getItem(1);
                ((FriendFragment)currentFragment).addFriend(inputEmail);
            } else {
                Log.d("MainActivity", "onActivityResult Fail!!!!");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                onClickLogout();
                break;
            case R.id.action_unlink:
                onClickUnlink();
                break;
        }
        return true;
    }

    /**
     * 로그아웃
     */
    private void onClickLogout() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            public void onCompleteLogout() {
                redirectLoginActivity();
            }
        });
    }

    /**
     * 탈퇴
     */
    private void onClickUnlink() {
        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        new AlertDialog.Builder(this)
                .setMessage(appendMessage)
                .setPositiveButton(getString(R.string.com_kakao_ok_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {

                                mUserDBRef.child(String.valueOf(userProfile.getId())).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        UserManagement.requestUnlink(new UnLinkResponseCallback() {
                                            @Override
                                            public void onFailure(ErrorResult errorResult) {
                                                Logger.e(errorResult.toString());
                                            }

                                            @Override
                                            public void onSessionClosed(ErrorResult errorResult) {
                                                redirectLoginActivity();
                                            }

                                            @Override
                                            public void onNotSignedUp() {
                                            }

                                            @Override
                                            public void onSuccess(Long result) {
                                                redirectLoginActivity();
                                            }
                                        });
                                        dialog.dismiss();
                                    }
                                });

                            }
                        })
                .setNegativeButton(getString(R.string.com_kakao_cancel_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();

    }

    /**
     * 친구삭제
     */
    private void onClickFriendDelete() {
        final String appendMessage = "친구를 삭제하시겠습니까?";
        final FriendFragment currentFragment = (FriendFragment) mPagerAdapter.getItem(1);
        new AlertDialog.Builder(this)
                .setMessage(appendMessage)
                .setPositiveButton(getString(R.string.com_kakao_ok_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                currentFragment.deleteFriend();
                                dialog.dismiss();
                                fabFriendUnDeleteMode();
                            }
                        })
                .setNegativeButton(getString(R.string.com_kakao_cancel_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                currentFragment.cancelDeleteFriend();
                                dialog.dismiss();
                                fabFriendUnDeleteMode();
                            }
                        }).show();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if ( requestCode == PlaceSelectActivity.REQUEST_LOCATION ) {    // 위치 권한
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                // 권한 거부
                fabCloseAction();
            } else {
                // 권한 승낙
                startPlaceSelectActivity();
            }

        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }

}
