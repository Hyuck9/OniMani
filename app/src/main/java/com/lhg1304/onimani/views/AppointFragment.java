package com.lhg1304.onimani.views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.usermgmt.response.model.UserProfile;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.adapters.AppointListAdapter;
import com.lhg1304.onimani.models.Plan;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppointFragment extends Fragment {

    @BindView(R.id.tv_none_appoint)
    TextView tvNoneAppoint;

    @BindView(R.id.rv_appoint_list)
    RecyclerView mRecyclerView;

    private UserProfile mUserProfile;
    private FirebaseDatabase mFirebaseDatabase;
//    private DatabaseReference mMyDBRef;
    private DatabaseReference mMyPlansDBRef;
//    private DatabaseReference mAllUserDBRef;
//    private DatabaseReference mMemberDBRef;

    private AppointListAdapter mAppointListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View appointView = inflater.inflate(R.layout.fragment_appoint, container, false);
        ButterKnife.bind(this, appointView);

        mUserProfile = UserProfile.loadFromCache(); // User Session
        mFirebaseDatabase = FirebaseDatabase.getInstance(); // firebase database init

//        mAllUserDBRef   = mFirebaseDatabase.getReference("users");
        mMyPlansDBRef = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId())).child("plans");
//        mMemberDBRef   = mFirebaseDatabase.getReference("meeting_members");
//        mMyDBRef        = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId()));

        mAppointListAdapter = new AppointListAdapter(getContext());

        mRecyclerView.setAdapter(mAppointListAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        addPlansListener();

        return appointView;
    }

    private void addPlansListener() {
        mMyPlansDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // 방 생성
                drawUI(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // 방 업데이트
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // 방 삭제
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void drawUI(DataSnapshot roomDataSnapshot) {
        final Plan room = roomDataSnapshot.getValue(Plan.class);    // 방 정보 얻어옴
        mAppointListAdapter.addItem(room);

        // TODO: 해당 방의 인원으로 방제목등 업데이트 할 필요가 있을 경우 아래 주석 풀고 로직 구현
//        mMemberDBRef.child(room.getPlanId()).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Iterator<DataSnapshot> memberIterator = dataSnapshot.getChildren().iterator();
//
//                while( memberIterator.hasNext() ) {
//                    User member = memberIterator.next().getValue(User.class);
//                    mAppointListAdapter.addItem(room);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
    }

}
