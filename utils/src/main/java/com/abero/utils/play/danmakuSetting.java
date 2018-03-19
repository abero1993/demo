package com.abero.utils.play;

import android.content.Context;
import android.content.SharedPreferences;

import com.ocean.motube.BaseApplication;

/**
 * Created by abero on 2017/9/28.
 */

public class danmakuSetting {

    public static String KEY_DIRECTION = "direction";
    public static String KEY_TEXTSIZE = "textsize";
    public static String KEY_TEXTCOLOR = "textcolor";

    private static SharedPreferences sp = BaseApplication.getContext().getSharedPreferences("danmaku_setting",
            Context.MODE_PRIVATE);

    public static int getDirection() {
        return sp.getInt(KEY_DIRECTION, 3);
    }

    public static void putDirection(int direction) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_DIRECTION, direction);
        editor.apply();
    }

    public static int getTextSize() {
        return sp.getInt(KEY_TEXTSIZE, 18);
    }

    public static void putTextSize(int textsize) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(KEY_TEXTSIZE, textsize);
        editor.apply();
    }

    public static String getTextColor() {
        return sp.getString(KEY_TEXTCOLOR, "0xffffff");
    }

    public static void putTextColor(String textColor) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_TEXTCOLOR, textColor);
        editor.apply();
    }
}
