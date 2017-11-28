package com.lhg1304.onimani.views;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import com.lhg1304.onimani.adapters.FriendListAdapter;
import com.lhg1304.onimani.models.User;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendFragment extends Fragment {

    @BindView(R.id.tv_none_friend)
    TextView tvNoneFriend;

    @BindView(R.id.rv_friend_list)
    RecyclerView mRecyclerView;

    private UserProfile userProfile;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mFriendsDBRef;
    private DatabaseReference mUserDBRef;

    private FriendListAdapter mFriendListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View friendView = inflater.inflate(R.layout.fragment_friend, container, false);
        ButterKnife.bind(this, friendView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        userProfile = UserProfile.loadFromCache();

        mUserDBRef = mFirebaseDatabase.getReference("users");
        mFriendsDBRef = mFirebaseDatabase.getReference("users").child(String.valueOf(userProfile.getId())).child("friends");
        mFriendListAdapter = new FriendListAdapter();

        // Firebase Database에서 친구목록을 가지고와 mFriendListAdapter에 추가
        addFriendListener();

        mRecyclerView.setAdapter(mFriendListAdapter);

        return friendView;
    }

    private void addFriendListener() {
        mFriendsDBRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User friend = dataSnapshot.getValue(User.class);
                drawUI(friend);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
        });
    }

    private void drawUI(User friend) {
        mFriendListAdapter.addItem(friend);
    }

}
