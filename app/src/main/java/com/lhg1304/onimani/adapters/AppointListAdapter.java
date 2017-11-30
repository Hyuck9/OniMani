package com.lhg1304.onimani.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lhg1304.onimani.R;
import com.lhg1304.onimani.models.Appoint;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nexmore on 2017-11-28.
 */

public class AppointListAdapter extends RecyclerView.Adapter<AppointListAdapter.AppointHolder> {

    private int lastPosition = -1;
    private Context mContext;

    private ArrayList<Appoint> mAppointList;

    public AppointListAdapter(Context context) {
        this.mAppointList = new ArrayList<>();
        this.mContext = context;
    }

    public void addItem(Appoint item) {
        this.mAppointList.add(item);
        notifyDataSetChanged();
    }

    public Appoint getItem(int position) {
        return this.mAppointList.get(position);
    }

    public void removeItem(Appoint item) {
        int position = getItemPosition(item.getRoomId());
        mAppointList.remove(position);
        notifyDataSetChanged();
    }

    private int getItemPosition(String roomId) {
        int position = 0;
        for ( Appoint currItem : mAppointList ) {
            if ( currItem.getRoomId().equals(roomId) ) {
                return position;
            }
            position++;
        }
        return -1;
    }

    @Override
    public AppointHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_appoint_item, parent, false);
        return new AppointHolder(view);
    }

    @Override
    public void onBindViewHolder(AppointHolder holder, int position) {
        Appoint item = getItem(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvPlace.setText(item.getPlace());
        holder.tvTime.setText(item.getTime());

        setAnimation(holder.rootView, position);
    }

    @Override
    public int getItemCount() {
        return mAppointList.size();
    }


    private void setAnimation(View view, int position) {
        if ( position > lastPosition ) {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            view.startAnimation(animation);
            lastPosition = position;
        }
    }

    public static class AppointHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.appoint_item_img)
        ImageView appointThumbnailView;

        @BindView(R.id.appoint_item_title)
        TextView tvTitle;

        @BindView(R.id.appoint_item_place)
        TextView tvPlace;

        @BindView(R.id.appoint_item_time)
        TextView tvTime;

        @BindView(R.id.appoint_item_root_view)
        LinearLayout rootView;

        public AppointHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
