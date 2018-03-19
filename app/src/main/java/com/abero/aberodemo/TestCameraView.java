package com.abero.aberodemo;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by abero on 2018/2/26.
 */

public class TestCameraView extends View {

    private Camera mCamera;
    private Matrix mMatirix;
    private Paint mPaint;

    public TestCameraView(Context context) {
        this(context, null);
    }

    public TestCameraView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mCamera = new Camera();
        mMatirix = new Matrix();

        mPaint.setColor(0xffff0000);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mMatirix.reset();
        mCamera.save();
        mCamera.rotateZ(60);
        mCamera.getMatrix(mMatirix);
        mCamera.restore();

        mMatirix.preTranslate(-getWidth()/2, -getHeight()/2);
        mMatirix.postTranslate(getWidth()/2, getHeight()/2);

        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.db, option);
        option.inSampleSize = calculateInSampleSize(option, getWidth() / 2, getHeight() / 2);
        option.inJustDecodeBounds = false;
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.db, option), mMatirix, mPaint);

    }

    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


}
