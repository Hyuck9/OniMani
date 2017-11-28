package com.lhg1304.onimani.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lhg1304.onimani.R;
import com.lhg1304.onimani.customviews.RoundedImageView;
import com.lhg1304.onimani.models.User;

import java.util.ArrayList;

/**
 * Created by Nexmore on 2017-11-28.
 */

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.FriendHolder> {

    public static final int UNSELECTION_MODE = 1;
    public static final int SELECTION_MODE = 2;

    private int selectionMode = UNSELECTION_MODE;

    private ArrayList<User> friendList;

    public FriendListAdapter() {
        this.friendList = new ArrayList<>();
    }

    public void setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        notifyDataSetChanged();
    }

    public int getSelectionMode() {
        return this.selectionMode;
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

    public void addItem(User friend) {
        this.friendList.add(friend);
        notifyDataSetChanged();
    }

    public User getItem(int position) {
        return this.friendList.get(position);
    }

    @Override
    public FriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_friend_item, parent, false);
        FriendHolder friendHolder = new FriendHolder(view);
        return friendHolder;
    }

    @Override
    public void onBindViewHolder(FriendHolder holder, int position) {
        User friend = getItem(position);
        holder.mEmailView.setText(friend.getEmail());
        holder.mNickNameView.setText(friend.getNickName());
        if ( friend.getThumbUrl() != null ) {
            Glide.with(holder.itemView).load(friend.getThumbUrl()).into(holder.mThumbnailView);
        }
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class FriendHolder extends RecyclerView.ViewHolder {

        CheckBox mFriendSelectedView;

        RoundedImageView mThumbnailView;

        TextView mNickNameView;

        TextView mEmailView;

        private FriendHolder(View itemView) {
            super(itemView);
            mFriendSelectedView = itemView.findViewById(R.id.friend_item_checkbox);
            mThumbnailView = itemView.findViewById(R.id.friend_item_thumb);
            mNickNameView = itemView.findViewById(R.id.friend_item_name);
            mEmailView = itemView.findViewById(R.id.friend_item_email);
        }
    }
}
