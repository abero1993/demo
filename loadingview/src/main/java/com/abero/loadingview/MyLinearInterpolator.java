package com.abero.loadingview;

/**
 * Created by Administrator on 2017/12/18.
 */

public class MyLinearInterpolator extends Calculator {

    private static final String TAG = "MyLinearInterpolator";

    @Override
    public float getInterpolation(float input) {
        if(input<0.2)
            return 0;
        else if (input<=0.9)
            return  ((10f/7)*input-(2f/7));
        else return 1;
    }
}
