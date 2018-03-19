package com.abero.aberodemo;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by abero on 2018/2/25.
 */

public class MyRollTextView extends View implements View.OnClickListener {

    private static final String TAG = "MyRollTextView";

    private Paint mPaint;
    private float mLineBase = 100;

    public MyRollTextView(Context context) {
        this(context, null);
    }

    public MyRollTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        setOnClickListener(this);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setTextSize(100);
        mPaint.setColor(0xff000000);
        mPaint.setStrokeWidth(5);

        String str = "Abero";

        Rect rect = new Rect();
        mPaint.getTextBounds(str, 0, str.length(), rect);
        int w = rect.width();
        int h = rect.height();

        canvas.drawLine(0, 100, 500, 100, mPaint);

        canvas.drawLine(0, 200, 500, 200, mPaint);

        canvas.drawText(str, 0, mLineBase, mPaint);

        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();

        canvas.drawText("h=" + h + " w=" + w, 0, mLineBase + mPaint.getFontSpacing(), mPaint);

        canvas.drawText(" as=" + fontMetrics.ascent + " de=" + fontMetrics.descent, 0, mLineBase + mPaint
                .getFontSpacing() * 2, mPaint);

        canvas.drawText(" top=" + fontMetrics.top + " bot=" + fontMetrics.bottom, 0, mLineBase + mPaint
                .getFontSpacing() * 3, mPaint);


    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick");
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "baseY", 100, 0);
        animator.setDuration(1000);
        animator.start();
    }

    public void setBaseY(float x) {
        mLineBase = x;
        postInvalidate();
    }

    public float getBaseY() {
        return mLineBase;
    }
}
