package com.lhg1304.onimani.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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

    @BindView(R.id.fab1)
    FloatingActionButton mFab1;

    @BindView(R.id.fab2)
    FloatingActionButton mFab2;

    @BindView(R.id.fab3)
    FloatingActionButton mFab3;

    private ViewPagerAdapter mPagerAdapter;

    private UserProfile userProfile;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDBRef;

    private boolean isFabOpen = false;

    private Animation fabOpen, fabClose, fabRClockwise, fabRAntiClockWise;

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

        initalizeAnimation();

        mFab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( !isFabOpen ) {
                    mFab1.startAnimation(fabRClockwise);
                    mFab2.startAnimation(fabOpen);
                    mFab3.startAnimation(fabOpen);
                    mFab2.setClickable(true);
                    mFab3.setClickable(true);
                    isFabOpen = true;
                } else {
                    mFab1.startAnimation(fabRAntiClockWise);
                    mFab2.startAnimation(fabClose);
                    mFab3.startAnimation(fabClose);
                    mFab2.setClickable(false);
                    mFab3.setClickable(false);
                    isFabOpen = false;
                }
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
            }
        });
    }

    private void initalizeAnimation() {
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close);
        fabRClockwise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise);
        fabRAntiClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise);
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

    private void setUpViewPager() {
        mPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(new FriendFragment(), "친구1");
        mPagerAdapter.addFragment(new FriendFragment(), "친구2");
        mViewPager.setAdapter(mPagerAdapter);
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
                                                redirectSignupActivity();
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
