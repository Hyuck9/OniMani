package com.lhg1304.onimani.views;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kakao.usermgmt.response.model.UserProfile;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.adapters.AppointListAdapter;
import com.lhg1304.onimani.common.utils.ItemClickSupport;
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
    private DatabaseReference mMemberDBRef;

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
        mMemberDBRef   = mFirebaseDatabase.getReference("meeting_members");
//        mMyDBRef        = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId()));

        mAppointListAdapter = new AppointListAdapter(getContext());

        initRecyclerView();

        addPlansListener();

        return appointView;
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setAdapter(mAppointListAdapter);
        // item long Click
        ItemClickSupport.addTo(mRecyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                Plan plan = mAppointListAdapter.getItem(position);
                leaveRoom(plan);
                return true;
            }
        });

        // item Click
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                // TODO: 방 입장 (지도 보기 등)
                Plan plan = mAppointListAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), PlansActivity.class);
                intent.putExtra("plan_id", plan.getPlanId());
                startActivity(intent);
            }
        });
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
                Plan item = dataSnapshot.getValue(Plan.class);
                mAppointListAdapter.removeItem(item);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void leaveRoom(final Plan plan) {
        Snackbar.make(getView(), "선택된 방을 나가시겠습니까?", Snackbar.LENGTH_LONG).setAction("예", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 내 방 목록 제거
                // users > {user_id} > plans > {plan_id} 제거
                mMyPlansDBRef.child(plan.getPlanId()).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        // 방 멤버 목록에서 제거
                        // meeting_members > {plan_id} > {user_id} 제거
                        mMemberDBRef.child(plan.getPlanId()).child(String.valueOf(mUserProfile.getId())).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                // TODO: 방 삭제 후처리 로직 여기에 구현
                            }
                        });
                    }
                });
            }
        }).show();
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
