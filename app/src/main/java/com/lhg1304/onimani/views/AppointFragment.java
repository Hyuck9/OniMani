package com.lhg1304.onimani.views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lhg1304.onimani.R;
import com.lhg1304.onimani.adapters.AppointListAdapter;
import com.lhg1304.onimani.models.Appoint;

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

    private AppointListAdapter mAppointListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View appointView = inflater.inflate(R.layout.fragment_appoint, container, false);
        ButterKnife.bind(this, appointView);

        mAppointListAdapter = new AppointListAdapter(getContext());

        mRecyclerView.setAdapter(mAppointListAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        drawUI();

        return appointView;
    }

    private void drawUI() {
        Appoint appointRoom = new Appoint();
        appointRoom.setTitle("테스트!!!");
        appointRoom.setPlace("건대입구!!");
        appointRoom.setTime("2017-11-29 11:25");
        mAppointListAdapter.addItem(appointRoom);
        mAppointListAdapter.addItem(appointRoom);
        mAppointListAdapter.addItem(appointRoom);
        mAppointListAdapter.addItem(appointRoom);
        mAppointListAdapter.addItem(appointRoom);
        mAppointListAdapter.addItem(appointRoom);
        mAppointListAdapter.addItem(appointRoom);
    }

}
