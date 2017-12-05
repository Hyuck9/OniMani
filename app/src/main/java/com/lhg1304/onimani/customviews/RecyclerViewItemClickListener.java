package com.lhg1304.onimani.customviews;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by lhg1304 on 2017-12-02.
 */

public class RecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {

    public interface OnItemClickListener {
        void onItemClick(View view,int position);
    }

    private OnItemClickListener mListener;

    GestureDetector mGestureDetector;

    public RecyclerViewItemClickListener(Context context, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            //누르고 뗄 때 한번만 인식하도록 하기위해서
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        // //손으로 터치한 곳의 좌표를 토대로 해당 Item의 View를 가져옴
        View childView = rv.findChildViewUnder(e.getX(), e.getY());

        //터치한 곳의 View가 RecyclerView 안의 아이템이고 그 아이템의 View가 null이 아니라
        //정확한 Item의 View를 가져왔고, gestureDetector에서 한번만 누르면 true를 넘기게 구현했으니
        //한번만 눌려서 그 값이 true가 넘어왔다면
        if ( childView != null && mListener != null && mGestureDetector.onTouchEvent(e) ) {
            mListener.onItemClick(childView, rv.getChildPosition(childView));

            //현재 터치된 곳의 position을 가져오고
//            int currentPosition = rv.getChildAdapterPosition(childView);
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
