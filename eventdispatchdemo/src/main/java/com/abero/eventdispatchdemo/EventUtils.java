package com.abero.eventdispatchdemo;

import android.view.MotionEvent;

/**
 * Created by abero on 2018/3/20.
 */

public class EventUtils {

    public static String getEvent(MotionEvent ev) {
        String str = "";
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                str = "down";
                break;

            case MotionEvent.ACTION_MOVE:
                str = "move";
                break;
            case MotionEvent.ACTION_UP:
                str = "up";
                break;

            default:
                str = "unknow";
        }

        return str;
    }
}
