package com.lhg1304.onimani.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
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
        return new FriendHolder(view);
    }

    @Override
    public void onBindViewHolder(final FriendHolder holder, final int position) {
        Log.d("test", "onBindViewHolder!!!");
        final User friend = getItem(position);
        holder.mEmailView.setText(friend.getEmail());
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

        if ( getSelectionMode() == UNSELECTION_MODE ) {
            holder.mFriendSelectedView.setVisibility(View.GONE);
        } else {
            holder.mFriendSelectedView.setVisibility(View.VISIBLE);
        }

//        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//                Log.d("cardview","selected cardview position : "+ position);
//                if ( getSelectionMode() == UNSELECTION_MODE ) {
//                    holder.mFriendSelectedView.setChecked(true);
//                    setSelectionMode(SELECTION_MODE);
//                }
//                return false;
//            }
//        });
//
//        holder.mCardView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if ( getSelectionMode() == SELECTION_MODE ) {
//                    if ( holder.mFriendSelectedView.isChecked() ) {
//                        holder.mFriendSelectedView.setChecked(false);
//                    } else {
//                        holder.mFriendSelectedView.setChecked(true);
//                    }
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public static class FriendHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.card_view)
        CardView mCardView;

        @BindView(R.id.friend_item_checkbox)
        CheckBox mFriendSelectedView;

        @BindView(R.id.friend_item_thumb)
        RoundedImageView mThumbnailView;

        @BindView(R.id.friend_item_name)
        TextView mNickNameView;

        @BindView(R.id.friend_item_email)
        TextView mEmailView;

        private FriendHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
