package com.abero.eventdispatchdemo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by abero on 2018/3/20.
 */

public class MyButton extends Button {
    private static final String TAG = "MyButton";

    public MyButton(Context context) {
        super(context);
    }

    public MyButton(Context context, AttributeSet attrs) {
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
