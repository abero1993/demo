package com.abero.eventdispatchdemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by abero on 2018/3/20.
 */

public class MyLinearLayout extends LinearLayout {

    private static final String TAG = "MyLinearLayout";

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean isConsumed = super.dispatchTouchEvent(ev);
        Log.i(TAG, "dispatchTouchEvent:  ev=" + EventUtils.getEvent(ev) + "  " + isConsumed);
        return isConsumed;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isConsumed = super.onInterceptTouchEvent(ev);
        Log.i(TAG, "onInterceptTouchEvent: ev=" + EventUtils.getEvent(ev) + "  " + isConsumed);
        return isConsumed;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isConsumed = super.onTouchEvent(event);
        Log.i(TAG, "onTouchEvent:  ev=" + EventUtils.getEvent(event) + "  " + isConsumed);
        return isConsumed;
    }


}
