package com.abero.loadingview;


/**
 * Created by Administrator on 2017/12/18.
 */

public class CubicHermiteInterpolator extends Calculator {


    private float p0, p1, m0, m1;

    public CubicHermiteInterpolator(float p0, float p1, float m0, float m1) {
        this.p0 = p0;
        this.p1 = p1;
        this.m0 = m0;
        this.m1 = m1;
    }

    @Override
    public float getInterpolation(float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        return (float) (2 * t3 - 3 * t2 + 1) * p0 + (t3 - 2 * t2 + t) * m0 + (-2 * t3 + 3 * t2) * p1 + (t3 - t2) * m1;
    }
}
