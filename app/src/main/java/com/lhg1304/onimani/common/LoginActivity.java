package com.lhg1304.onimani.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.auth.ErrorCode;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.common.utils.DateUtil;
import com.lhg1304.onimani.models.User;

import static android.util.Log.getStackTraceString;

public class LoginActivity extends BaseActivity {

    private SessionCallback callback;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDBRef;

    private static final String TAG = "LoginActivity";

    /**
     * 로그인 버튼을 클릭 했을시 access token을 요청하도록 설정한다.
     *
     * @param savedInstanceState 기존 session 정보가 저장된 객체
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDBRef = mFirebaseDatabase.getReference("users");

        callback = new SessionCallback();

        Session.getCurrentSession().addCallback(callback);
        Session.getCurrentSession().checkAndImplicitOpen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }


    /**
     * 사용자의 상태를 알아 보기 위해 me API 호출을 한다.
     */
    protected void requestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Logger.d(message);

                ErrorCode result = ErrorCode.valueOf(errorResult.getErrorCode());
                if (result == ErrorCode.CLIENT_ERROR_CODE) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_message_for_service_unavailable), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    redirectLoginActivity();
                }
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                Log.d(TAG, "UserProfile : " + userProfile);
                joinUser(userProfile);
            }

            @Override
            public void onNotSignedUp() {
            }
        });
    }

    /**
     * 파이어베이스 데이터베이스 데이터 조회 후
     * 존재하지 않을 때 가입 처리 / 존재하면 자동 로그인 처리
     * @param userProfile
     */
    private void joinUser(final UserProfile userProfile) {
        mUserDBRef.child(String.valueOf(userProfile.getId())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ( !dataSnapshot.exists() ) {
                    // 데이터가 존재하지 않을 때 회원가입 처리 후 로그인
                    User user = createUser(userProfile);
                    mUserDBRef.child(user.getUid()).setValue(user, (databaseError, databaseReference) -> {
                        if ( databaseError == null ) {
                            redirectMainActivity();
                        }
                    });
                } else {
                    // 데이터가 존재하면 자동 로그인
                    redirectMainActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * 카카오에서 받은 UserProfile 객체로 User 객체 생성
     * @param userProfile
     * @return User
     */
    private User createUser(UserProfile userProfile) {
        User user = new User();
        user.setEmail(userProfile.getEmail());
        user.setNickName(userProfile.getNickname());
        user.setUid(String.valueOf(userProfile.getId()));
        user.setJoinedDate(DateUtil.getCurrentDate());
        if ( userProfile.getProfileImagePath() != null ) {
            user.setProfileUrl(userProfile.getProfileImagePath());
        }
        if ( userProfile.getThumbnailImagePath() != null ) {
            user.setThumbUrl(userProfile.getThumbnailImagePath());
        }
        return user;
    }



    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
//            redirectSignupActivity();
            requestMe();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if ( exception != null ) {
                Log.e(TAG, getStackTraceString(exception));
            }
        }
    }
}
