package com.lhg1304.onimani;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.helper.log.Logger;
import com.lhg1304.onimani.common.BaseActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends BaseActivity {

    private UserProfile userProfile;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDBRef;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Hash key 조회 코드
         */
        try {
            PackageInfo info = getPackageManager().getPackageInfo("com.lhg1304.onimani", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash : ", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDBRef = mFirebaseDatabase.getReference("users");

        initializeButtons();
        logProfile();
    }

    private void logProfile() {
        userProfile = UserProfile.loadFromCache();
        Log.d(TAG, "email : " + userProfile.getEmail());
        Log.d(TAG, "profileImagePath : " + userProfile.getProfileImagePath());
        Log.d(TAG, "nickname : " + userProfile.getNickname());
        Log.d(TAG, "id : " + userProfile.getId());
    }

    /**
     * 버튼 초기화
     */
    private void initializeButtons() {
       final Button logoutButton = findViewById(R.id.btn_logout);
       logoutButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               onClickLogout();
           }
       });

       final Button unlinkButton = findViewById(R.id.btn_unlink);
       unlinkButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               onClickUnlink();
           }
       });
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
}
