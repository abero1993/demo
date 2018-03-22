package com.abero.eventdispatchdemo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Administrator on 2018/3/20.
 */

public class MyTextView extends TextView {

    private static final String TAG = "MyTextView";

    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean isCousumed = super.dispatchTouchEvent(event);
        Log.i(TAG, "dispatchTouchEvent: ev=" + EventUtils.getEvent(event) +"  "+isCousumed);
        return isCousumed;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isConsumed = super.onTouchEvent(event);
        Log.i(TAG, "onTouchEvent: ev=" + EventUtils.getEvent(event)+" "+isConsumed);
        return isConsumed;
    }
}
