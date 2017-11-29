package com.lhg1304.onimani.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lhg1304.onimani.R;
import com.lhg1304.onimani.customviews.RoundedImageView;
import com.lhg1304.onimani.models.Appoint;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Nexmore on 2017-11-28.
 */

public class AppointListAdapter extends RecyclerView.Adapter<AppointListAdapter.AppointHolder> {

    private ArrayList<Appoint> mAppointList;

    public AppointListAdapter() {
        this.mAppointList = new ArrayList<>();
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
    }

    @Override
    public int getItemCount() {
        return mAppointList.size();
    }

    public static class AppointHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.appoint_item_thumb)
        RoundedImageView appointThumbnailView;

        @BindView(R.id.appoint_item_title)
        TextView tvTitle;

        @BindView(R.id.apppoint_item_place)
        TextView tvPlace;

        @BindView(R.id.apppoint_item_time)
        TextView tvTime;

        @BindView(R.id.appoint_item_root_view)
        LinearLayout rootView;

        public AppointHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
