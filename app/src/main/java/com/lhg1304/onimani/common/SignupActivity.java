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
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.helper.log.Logger;
import com.lhg1304.onimani.views.MainActivity;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.common.utils.DateUtil;
import com.lhg1304.onimani.models.User;

/**
 * 유효한 세션이 있다는 검증 후
 * me를 호출하여 가입 여부에 따라 가입 페이지를 그리던지 Main 페이지로 이동 시킨다.
 */
public class SignupActivity extends BaseActivity {

    private static final String TAG = "SignupActivity";

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDBRef;

    /**
     * Main으로 넘길지 가입 페이지를 그릴지 판단하기 위해 me를 호출한다.
     * @param savedInstanceState 기존 session 정보가 저장된 객체
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 여기에 로딩 바 만들면 될 듯

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDBRef = mFirebaseDatabase.getReference("users");

        requestMe();
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
                    mUserDBRef.child(user.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if ( databaseError == null ) {
                                redirectMainActivity();
                            }
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

    private void redirectMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
