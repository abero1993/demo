package com.abero.aberodemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by abero on 2018/2/27.
 */

public class TestMapView extends View {

    private static final String TAG = "TestMapView";
    private Paint mPaint;
    private Camera mCamera;
    private Bitmap mBitmap;
    private float mDegreeY=0;

    public TestMapView(Context context) {
        this(context, null);
    }


    public TestMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mCamera = new Camera();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float newZ = -displayMetrics.density * 6;
        mCamera.setLocation(0, 0, newZ);

        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.db);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int bitmapWidth = mBitmap.getWidth();
        int bitmapHeight = mBitmap.getHeight();
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int x = centerX - bitmapWidth / 2;
        int y = centerY - bitmapHeight / 2;

        canvas.save();
        mCamera.save();
        canvas.translate(centerX, centerY);
        //canvas.rotate(-degreeZ);
        mCamera.rotateY(mDegreeY);
        mCamera.applyToCanvas(canvas);
        //计算裁切参数时清注意，此时的canvas的坐标系已经移动
        canvas.clipRect(0, -centerY, centerX, centerY);
        //canvas.rotate(degreeZ);
        canvas.translate(-centerX, -centerY);
        mCamera.restore();
        canvas.drawBitmap(mBitmap, x, y, mPaint);
        canvas.restore();

        //画另一半
        canvas.save();
        mCamera.save();

        canvas.translate(centerX,centerY);
        mCamera.applyToCanvas(canvas);

        canvas.clipRect(-centerX,-centerY,0,centerY);
        canvas.translate(-centerX,-centerY);

        mCamera.restore();
        canvas.drawBitmap(mBitmap,x,y,mPaint);
        canvas.restore();

    }

    public void setDegreeY(float degreeY) {
        this.mDegreeY = degreeY;
        invalidate();
    }

    public float getDegreeY() {
        return mDegreeY;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
