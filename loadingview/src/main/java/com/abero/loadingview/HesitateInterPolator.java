package com.abero.loadingview;

import android.view.animation.Interpolator;

/**
 * Created by abero on 2017/7/5.
 */

public class HesitateInterPolator implements Interpolator {

    public HesitateInterPolator() {
    }

    public float getInterpolation(float t) {
        float cycles = 0.5f;
        return (float) Math.sin(2 * cycles * Math.PI * t);
    }
}
