package com.lhg1304.onimani.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.customviews.RoundedImageView;
import com.lhg1304.onimani.models.User;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by lhg1304 on 2017-11-28.
 */

public class CreateRoomAdapter extends RecyclerView.Adapter<CreateRoomAdapter.FriendHolder> {

    private ArrayList<User> friendList;

    public CreateRoomAdapter(ArrayList<User> list) {
        this.friendList = list;
    }

    public int getSelectionUserCount() {
        int selectedCount = 0;
        for ( User user : friendList ) {
            if ( user.isSelection() ) {
                selectedCount++;
            }
        }
        return selectedCount;
    }

    public void allUnSelect() {
        for ( User user : friendList ) {
            user.setSelection(false);
        }
    }

    public String [] getSelectedUids() {
        String [] selectedUids = new String[getSelectionUserCount()];
        int i = 0;
        for ( User user : friendList ) {
            if ( user.isSelection() ) {
                selectedUids[i++] = user.getUid();
            }
        }
        return selectedUids;
    }

//    public void addItem(User friend) {
//        this.friendList.add(friend);
//        notifyDataSetChanged();
//    }

    public User getItem(int position) {
        return this.friendList.get(position);
    }

    @Override
    public FriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.create_room_item, parent, false);
        return new FriendHolder(view);
    }

    @Override
    public void onBindViewHolder(final FriendHolder holder, final int position) {
        final User friend = getItem(position);
        holder.mNickNameView.setText(friend.getNickName());
        if ( friend.getThumbUrl() != null ) {
            Glide.with(holder.itemView).load(friend.getThumbUrl()).into(holder.mThumbnailView);
        }

        holder.mFriendSelectedView.setOnCheckedChangeListener(null);
        holder.mFriendSelectedView.setChecked(friend.isSelection());
        holder.mFriendSelectedView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                friend.setSelection(b);
            }
        });


    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class FriendHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.create_room_item_checkbox)
        CheckBox mFriendSelectedView;

        @BindView(R.id.create_room_item_thumb)
        RoundedImageView mThumbnailView;

        @BindView(R.id.create_room_item_name)
        TextView mNickNameView;

        private FriendHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
