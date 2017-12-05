package com.lhg1304.onimani.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kakao.usermgmt.response.model.UserProfile;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.adapters.FriendListAdapter;
import com.lhg1304.onimani.common.utils.ItemClickSupport;
import com.lhg1304.onimani.models.User;

import java.util.Iterator;

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

    private UserProfile mUserProfile;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMyDBRef;
    private DatabaseReference mMyFriendsDBRef;
    private DatabaseReference mAllUserDBRef;

    private FriendListAdapter mFriendListAdapter;

    public static final int FIND_FRIEND_REQUEST_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View friendView = inflater.inflate(R.layout.fragment_friend, container, false);
        ButterKnife.bind(this, friendView);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserProfile = UserProfile.loadFromCache();

        mAllUserDBRef   = mFirebaseDatabase.getReference("users");
        mMyDBRef        = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId()));
        mMyFriendsDBRef = mFirebaseDatabase.getReference("users").child(String.valueOf(mUserProfile.getId())).child("friends");
        mFriendListAdapter = new FriendListAdapter();

        // 리스이클러뷰 초기 셋팅
        initRecyclerView();

        // Firebase Database에서 친구목록을 가지고와 mFriendListAdapter에 추가
        addFriendListener();

        return friendView;
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mFriendListAdapter);
        // item long Click
        ItemClickSupport.addTo(mRecyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                final User friend = mFriendListAdapter.getItem(position);
                if ( mFriendListAdapter.getSelectionMode() == FriendListAdapter.UNSELECTION_MODE ) {
                    ((MainActivity)getActivity()).fabFriendDeleteMode();
                    friend.setSelection(true);
                    mFriendListAdapter.setSelectionMode(FriendListAdapter.SELECTION_MODE);
                }
                return true;
            }
        });

        // item Click
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                selectionModeItemClick(position);
            }
        });
    }

    private void initVisible() {
        if (mFriendListAdapter.getItemCount() > 0) {
            tvNoneFriend.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            tvNoneFriend.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    public void toggleSelectionMode() {
        mFriendListAdapter.setSelectionMode(
                mFriendListAdapter.getSelectionMode() == FriendListAdapter.SELECTION_MODE ? FriendListAdapter.UNSELECTION_MODE : FriendListAdapter.SELECTION_MODE
        );
    }

    private void selectionModeItemClick(int position) {
        final User friend = mFriendListAdapter.getItem(position);
        if ( mFriendListAdapter.getSelectionMode() == FriendListAdapter.SELECTION_MODE ) {
            friend.setSelection(friend.isSelection() ? false : true);
            mFriendListAdapter.notifyItemChanged(position);
        }
    }


    private void addFriendListener() {
        mMyFriendsDBRef.addChildEventListener(new ChildEventListener() {
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


    public void addFriend(String inputEmail) {
        searchFriend(inputEmail);
    }

    // 친구 검색
    private void searchFriend(@NonNull final String inputEmail) {
        Log.d("FriendFragment", "E-Mail :" + inputEmail);

        if ( inputEmail.equals(mUserProfile.getEmail()) ) {
            Snackbar.make(mRecyclerView, "본인은 친구로 등록할 수 없습니다.", Snackbar.LENGTH_LONG).show();
            return;
        }

        // 나의 정보를 조회하여 이미 등록된 친구인지 판단
        mMyFriendsDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> friendsIterator = dataSnapshot.getChildren().iterator();
                while ( friendsIterator.hasNext() ) {
                    User user = friendsIterator.next().getValue(User.class);

                    if ( user.getEmail().equals(inputEmail) ) {
                        Snackbar.make(mRecyclerView, "이미 등록된 친구입니다.", Snackbar.LENGTH_INDEFINITE).show();
                        return;
                    }
                }
                userValidation(inputEmail);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // users db에 존재하지 않는 이메일이라면, 가입하지 않은 친구라는 메시지를 띄워줌
    private void userValidation(final String inputEmail) {///////////////////////////////////////////////////asdasdasdasdasdasdasdasdasd
        mAllUserDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> userIterator = dataSnapshot.getChildren().iterator();
                int userCount = (int) dataSnapshot.getChildrenCount();
                int loopCount = 1;

                while ( userIterator.hasNext() ) {
                    final User currentUser = userIterator.next().getValue(User.class);

                    if ( inputEmail.equals(currentUser.getEmail()) ) {
                        addFriend(currentUser);
                    } else {
                        if ( userCount <= loopCount++ ) {
                            Snackbar.make(mRecyclerView, "가입을 하지 않은 친구입니다.", Snackbar.LENGTH_LONG).show();
                            return;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    // 친구 등록 로직
    private void addFriend(final User friend) {
        // users/{내이메일}/friends/{상대방이메일}/
        mMyFriendsDBRef.push().setValue(friend, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                // 나의 정보를 가져온다.
                mMyDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User me = dataSnapshot.getValue(User.class);    // 내 정보
                        // 상대방에게 내 정보 등록
                        // users/{상대방이메일}/friends/{나의이메일}/내정보등록
                        mAllUserDBRef.child(friend.getUid()).child("friends").push().setValue(me);
                        Snackbar.make(mRecyclerView, "친구등록이 완료되었습니다.", Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    public void deleteFriend() {
        // TODO: 친구삭제 로직 추가!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        mFriendListAdapter.allUnSelect();
        mFriendListAdapter.setSelectionMode(FriendListAdapter.UNSELECTION_MODE);
    }

    public void cancelDeleteFriend() {
        mFriendListAdapter.allUnSelect();
        mFriendListAdapter.setSelectionMode(FriendListAdapter.UNSELECTION_MODE);
    }

    private void drawUI(User friend) {
        mFriendListAdapter.addItem(friend);
        initVisible();
    }

}
