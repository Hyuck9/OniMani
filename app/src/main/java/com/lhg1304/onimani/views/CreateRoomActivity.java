package com.lhg1304.onimani.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.usermgmt.response.model.UserProfile;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.adapters.CreateRoomAdapter;
import com.lhg1304.onimani.common.utils.DateUtil;
import com.lhg1304.onimani.common.utils.ItemClickSupport;
import com.lhg1304.onimani.models.Plan;
import com.lhg1304.onimani.models.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateRoomActivity extends AppCompatActivity {

    @BindView(R.id.rv_add_friend_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.btn_create_room)
    Button mBtnOk;

    @BindView(R.id.create_room_title)
    EditText edTitle;

    private CreateRoomAdapter createRoomAdapter;

    private UserProfile mUserProfile;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDBRef;
    private DatabaseReference mMyPlansDBRef;
    private DatabaseReference mMemberDBRef;

    private ArrayList<User> mFriendList;

    private String mPlanId;
    private double mLatitude, mLongitude;
    private String mAddress = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);
        ButterKnife.bind(this);

        mUserProfile = UserProfile.loadFromCache();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDBRef      = mFirebaseDatabase.getReference("users");
        mMyPlansDBRef   = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId())).child("plans");

        mFriendList = (ArrayList<User>) getIntent().getSerializableExtra("myFriends");
        mAddress = getIntent().getStringExtra("address");
        mLatitude = getIntent().getDoubleExtra("latitude", 0);
        mLongitude = getIntent().getDoubleExtra("longitude", 0);
        createRoomAdapter = new CreateRoomAdapter(mFriendList);
        createRoomAdapter.allUnSelect();
        initRecyclerView();
        initButton();
    }
    
    private void initButton() {
        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = edTitle.getText().toString();
                if ( !title.isEmpty() ) {
                    if ( createRoomAdapter.getSelectionUserCount() > 0) {
                        createRoom(title);
                    } else {
                        Toast.makeText(CreateRoomActivity.this, "친구를 선택 해 주세요", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateRoomActivity.this, "제목을 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(createRoomAdapter);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                selectionModeItemClick(position);
            }
        });
    }

    private void selectionModeItemClick(int position) {
        final User friend = createRoomAdapter.getItem(position);
        friend.setSelection( !friend.isSelection() );
        createRoomAdapter.notifyItemChanged(position);
    }

    private void createRoom(String title) {
        final Plan plan = new Plan();
        mMyPlansDBRef = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId())).child("plans");
        mPlanId = mMyPlansDBRef.push().getKey();
        mMemberDBRef = mFirebaseDatabase.getReference("meeting_members").child(mPlanId);

        plan.setPlanId(mPlanId);
        plan.setTitle(title);
        plan.setTime(DateUtil.getCurrentDate());    //TODO: 약속 시간 정해서 로직 넣어야함
        plan.setPlace(mAddress);
        plan.setLatitude(mLatitude);
        plan.setLongitude(mLongitude);

        List<String> uidList = new ArrayList<>(Arrays.asList(createRoomAdapter.getSelectedUids()));
        uidList.add(String.valueOf(mUserProfile.getId()));

        for (int i=0; i<uidList.size(); i++) {
            String userId = uidList.get(i);
            final int index = i;
            mUserDBRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    User member = dataSnapshot.getValue(User.class);
                    /*member.setLatitude((double)0);
                    member.setLongitude((double)0);*/
                    member.setMemberIndex(index);
                    mMemberDBRef.child(member.getUid())
                            .setValue(member, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    dataSnapshot.getRef().child("plans").child(mPlanId).setValue(plan);
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }

        /*mMemberDBRef.child("memberCount").setValue(uidList.size());*/

        /*for ( String userId : uidList ) {
            final int[] index = {0};
            mUserDBRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    User member = dataSnapshot.getValue(User.class);
                    member.setLatitude((double)0);
                    member.setLongitude((double)0);
                    member.setMemberIndex(index[0]++);
                    mMemberDBRef.child(member.getUid())
                            .setValue(member, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    dataSnapshot.getRef().child("plans").child(mPlanId).setValue(plan);
                                }
                            });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }*/
        // TODO: 방 생성 후 입장 로직 구현
        finish();
    }
}
