package com.abero.loadingview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;

/**
 * Created by abero on 2017/12/11.
 */

public class TBLoadingView extends View {

    private static final String TAG = "TBLoadingView";
    private static final float sMagicNumber = 0.55228475f;
    private static boolean DEBUG = true;
    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mCenterY;
    //围绕大圆的半径
    private int mRadius;
    //圆球的半径
    private int mBallRadius;

    //紫色的圆
    private float mACircleX;
    private float mACircleY;
    private int mACircleColor;

    //蓝色的圆
    private float mBCircleX;
    private float mBCircleY;
    private int mBCircleColor;

    //青色的圆
    private float mCCircleX;
    private float mCCircleY;
    private int mCCircleColor;

    private float mCurrentDegree;

    private Paint mPaint;
    private ObjectAnimator mAnimator;
    private Path mPath;
    private int[] mGradientColor;


    public TBLoadingView(Context context) {
        super(context);
        init();
    }

    public TBLoadingView(Context context,  AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    private void init() {

        mCurrentDegree = 48;
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mACircleColor = 0xffac47bc;
        mBCircleColor = 0xff5677fc;
        mCCircleColor = 0xff26c6da;

        mGradientColor = new int[]{0xffac47bc, 0xffac47bc, 0xffac47bc, 0xff5677fc, 0xff26c6da, 0xff26c6da, 0xff26c6da};

        mPath = new Path();
    }

    float cos(float num) {
        return (float) Math.cos(num * Math.PI / 180);
    }

    float acos(double values) {
        return (float) (Math.acos(values) * (180 / Math.PI));
    }

    float sin(float num) {
        return (float) Math.sin(num * Math.PI / 180);
    }

    public void showLoading() {
        if (mAnimator != null && mAnimator.isRunning())
            return;

        mAnimator = ObjectAnimator.ofFloat(this, "degree", 30, 60);
        mAnimator.setDuration(800 * 20*2);
        mAnimator.setStartDelay(0);
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        //mAnimator.setRepeatCount(-1);
        mAnimator.setInterpolator(new LinearInterpolator());
       // mAnimator.start();
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                invalidate();
            }
        });
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        mHeight = getHeight() - getPaddingTop() - getPaddingBottom();

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;

        canvas.translate(mCenterX, mCenterY);

        mRadius = mWidth / 2 / 2;
        mBallRadius = mRadius / 4;

        float degree = mCurrentDegree + 300;
        mCCircleX = mRadius * cos(degree);
        mCCircleY = mRadius * sin(degree);

        degree = mCurrentDegree * 2 + 60;
        mBCircleX = mRadius * cos(degree);
        mBCircleY = mRadius * sin(degree);

        degree = mCurrentDegree * 3 + 180;
        mACircleX = mRadius * cos(degree);
        mACircleY = mRadius * sin(degree);

        //判断是否接近 距离是一个球的直径,根据两点距离公式
        //A-C 融合
        float acdis = (float) Math.sqrt(Math.pow((double) (mACircleX - mCCircleX), 2) + Math.pow((double) (mACircleY -
                mCCircleY), 2));
        //Logi("acdis=" + acdis + " ball r=" + mBallRadius);
        if (acdis < mBallRadius * 4) {

            mPath.reset();
            if (Math.abs(mACircleX) < Math.abs(mCCircleX)) //追赶
            {

                float roration = acos(Math.abs(mACircleX - mCCircleX) / acdis);
                Logi("de=" + roration);
                if (acdis > mBallRadius * 3) {

                    float ratio = (1 - (acdis / 2 - mBallRadius) / mBallRadius);
                    float lradius = ratio * mBallRadius * 0.5f + mBallRadius;
                    float sradius = mBallRadius;
                    //Logi("lradius=" + lradius + " sradius=" + sradius);
                    mPaint.setColor(mACircleColor);
                    drawOval(canvas, mACircleX, mACircleY, sradius, lradius, roration, mPaint);

                    mPaint.setColor(mCCircleColor);
                    drawOval(canvas, mCCircleX, mCCircleY, lradius, sradius, roration, mPaint);
                } else if (acdis > mBallRadius * 2) {

                    float ratio = (acdis - 2 * mBallRadius) / (mBallRadius);
                    float lradius = (ratio) * mBallRadius * 0.4f + mBallRadius;
                    float sradius = mBallRadius;
                    Logi("lradius=" + lradius + " sradius=" + sradius + " radio=" + ratio);
                    drawPeanut(canvas, mACircleX, mACircleY, mCCircleX, mCCircleY, sradius, lradius, roration, ratio,
                            mPaint);
                } else if (acdis > mBallRadius) {
                    drawCask(canvas, mACircleX, mACircleY, mCCircleX, mCCircleY, roration, mPaint);
                }
            }

        } else {
            mPaint.setColor(mCCircleColor);
            canvas.drawCircle(mCCircleX, mCCircleY, mBallRadius, mPaint);

            mPaint.setColor(mACircleColor);
            canvas.drawCircle(mACircleX, mACircleY, mBallRadius, mPaint);
        }

        mPaint.setShader(null);
        mPaint.setColor(mBCircleColor);
        canvas.drawCircle(mBCircleX, mBCircleY, mBallRadius, mPaint);

    }

    private void drawOval(Canvas canvas, float x, float y, float leftRadius, float rightRadius, float degree, Paint
            paint) {
        /*
        需要旋转的角度
        假设对图片上任意点(x,y)，绕一个坐标点(rx0,ry0)逆时针旋转a角度后的新的坐标设为(x0, y0)，有公式：
        x0= (x - rx0)*cos(a) - (y - ry0)*sin(a) + rx0 ;
        y0= (x - rx0)*sin(a) + (y - ry0)*cos(a) + ry0 ;
        * */
        //s ( mACircleX,mACircleY-mBallRadius)
        //c1 (mACircleX + mBallRadius * sMagicNumber,mACircleY - mBallRadius)
        //c2 ( mACircleX +lradius, mACircleY - mBallRadius * sMagicNumber)
        //e (mACircleX + lradius, mACircleY)
        mPath.reset();
        mPath.moveTo(x + mBallRadius * sin(degree), -mBallRadius * cos(degree) + y);
        mPath.cubicTo((mBallRadius * sMagicNumber) * cos(degree) + mBallRadius * sin(degree) + x,
                (mBallRadius * sMagicNumber) * sin(degree) - mBallRadius * cos(degree) + y,
                rightRadius * cos(degree) + (mBallRadius * sMagicNumber) * sin(degree) + x, rightRadius * sin(degree)
                        - (mBallRadius * sMagicNumber) * cos(degree) + y, rightRadius * cos(degree) + x,
                rightRadius * sin(degree) + y);
        mPath.lineTo(x, y);

        //s (mACircleX, mACircleY + mBallRadius)
        // c1 (mACircleX + mBallRadius * sMagicNumber, mACircleY + mBallRadius)
        // c2 (mACircleX + lradius, mACircleY + mBallRadius * sMagicNumber)
        // e  (mACircleX + lradius, mACircleY)
        // x0= (x - rx0)*cos(a) - (y - ry0)*sin(a) + rx0 ;
        //y0= (x - rx0)*sin(a) + (y - ry0)*cos(a) + ry0 ;


        mPath.moveTo(-mBallRadius * sin(degree) + x, y + mBallRadius * cos(degree));
        mPath.cubicTo(mBallRadius * sMagicNumber * cos(degree) - mBallRadius * sin(degree) + x,
                mBallRadius * sMagicNumber * sin(degree) + mBallRadius * cos(degree) + y,
                rightRadius * cos(degree) - mBallRadius * sMagicNumber * sin(degree) + x, rightRadius * sin(degree) +
                        mBallRadius * sMagicNumber * cos(degree) + y, rightRadius * cos(degree) + x,
                rightRadius * sin(degree) + y);
        mPath.lineTo(x, y);


        //s (mACircleX, mACircleY - mBallRadius)
        // c1 (mACircleX - mBallRadius * sMagicNumber, mACircleY - mBallRadius)
        // c2 (mACircleX - sradius, mACircleY - mBallRadius * sMagicNumber)
        // e  ( mACircleX - sradius, mACircleY)

        mPath.moveTo(mBallRadius * sin(degree) + x, -mBallRadius * cos(degree) + y);
        mPath.cubicTo(-mBallRadius * sMagicNumber * cos(degree) + mBallRadius * sin(degree) + x,
                -mBallRadius * sMagicNumber * sin(degree) - mBallRadius * cos(degree) + y,
                -leftRadius * cos(degree) + mBallRadius * sMagicNumber * sin(degree) + x, -leftRadius * sin(degree)
                        - mBallRadius * sMagicNumber * cos(degree) + y, x - leftRadius * cos(degree)
                , y - leftRadius * sin(degree));
        mPath.lineTo(x, y);


        //s (mACircleX, mACircleY + mBallRadius)
        // c1 (mACircleX - mBallRadius * sMagicNumber, mACircleY + mBallRadius)
        // c2 (mACircleX - sradius, mACircleY + mBallRadius * sMagicNumber)
        // e  ( mACircleX - sradius, mACircleY)

        mPath.moveTo(x - mBallRadius * sin(degree), y + mBallRadius * cos(degree));
        mPath.cubicTo(x - mBallRadius * sMagicNumber * cos(degree) - mBallRadius * sin(degree), y
                        + mBallRadius * cos(degree) - mBallRadius * sMagicNumber * sin(degree),
                x - leftRadius * cos(degree) - mBallRadius * sMagicNumber * sin(degree), y +
                        mBallRadius * sMagicNumber * cos(degree) - leftRadius * sin(degree), x - leftRadius *
                        cos(degree),
                y - leftRadius * sin(degree));
        mPath.lineTo(x, y);

        canvas.drawPath(mPath, paint);

    }

    private void drawPeanut(Canvas canvas, float fx, float fy, float bx, float by, float sradius, float
            lradius, float degree, float radio, Paint paint) {

        mPath.reset();

        //左边
        mPath.moveTo(mBallRadius * sin(degree) + fx, -mBallRadius * cos(degree) + fy);
        mPath.cubicTo(-mBallRadius * sMagicNumber * cos(degree) + mBallRadius * sin(degree) + fx,
                -mBallRadius * sMagicNumber * sin(degree) - mBallRadius * cos(degree) + fy,
                -sradius * cos(degree) + mBallRadius * sMagicNumber * sin(degree) + fx, -sradius * sin(degree)
                        - mBallRadius * sMagicNumber * cos(degree) + fy, fx - sradius * cos(degree)
                , fy - sradius * sin(degree));
        mPath.lineTo(fx, fy);


        mPath.moveTo(fx - mBallRadius * sin(degree), fy + mBallRadius * cos(degree));
        mPath.cubicTo(fx - mBallRadius * sMagicNumber * cos(degree) - mBallRadius * sin(degree), fy
                        + mBallRadius * cos(degree) - mBallRadius * sMagicNumber * sin(degree),
                fx - sradius * cos(degree) - mBallRadius * sMagicNumber * sin(degree), fy +
                        mBallRadius * sMagicNumber * cos(degree) - sradius * sin(degree), fx - sradius *
                        cos(degree), fy - sradius * sin(degree));
        mPath.lineTo(fx, fy);

        //右边要接触了,连接点为中点，计算中点
        float centerX = (fx + bx) / 2;
        float centerY = (fy + by) / 2;

        //要计算 垂直于ac，相交于中点的直线上的点,相当于把膨胀
        float toppullX = centerX + sin(degree) * (1 - radio) * mBallRadius * 0.7f;
        float toppullY = centerY - cos(degree) * (1 - radio) * mBallRadius * 0.7f;

        float botpullX = centerX - sin(degree) * (1 - radio) * mBallRadius * 0.7f;
        float botpullY = centerY + cos(degree) * (1 - radio) * mBallRadius * 0.7f;

        //s ( mACircleX,mACircleY-mBallRadius)
        //c1 (mACircleX + mBallRadius * sMagicNumber,mACircleY - mBallRadius)
        //c2  (toppullX,toppullY-mBallRadius*sMagicNumer)
        //e (toppullX,toppullY)
        mPath.moveTo(fx + mBallRadius * sin(degree), -mBallRadius * cos(degree) + fy);
        mPath.cubicTo((mBallRadius * sMagicNumber) * cos(degree) + mBallRadius * sin(degree) + fx,
                (mBallRadius * sMagicNumber) * sin(degree) - mBallRadius * cos(degree) + fy,
                lradius * cos(degree) + (mBallRadius * sMagicNumber) * sin(degree) + fx,
                lradius * sin(degree) - (mBallRadius * sMagicNumber) * cos(degree) + fy, toppullX, toppullY);
        mPath.lineTo(fx, fy);
        Logi("c2 y=" + (lradius * sin(degree) - (mBallRadius * sMagicNumber) * cos(degree) + fy));

        //s (mACircleX, mACircleY + mBallRadius)
        // c1 (mACircleX + mBallRadius * sMagicNumber, mACircleY + mBallRadius)
        // c2 (mACircleX + lradius, mACircleY + mBallRadius * sMagicNumber) (botx-ra)
        // e  (mACircleX + lradius, mACircleY)
        mPath.moveTo(-mBallRadius * sin(degree) + fx, fy + mBallRadius * cos(degree));
        mPath.cubicTo(mBallRadius * sMagicNumber * cos(degree) - mBallRadius * sin(degree) + fx,
                mBallRadius * sMagicNumber * sin(degree) + mBallRadius * cos(degree) + fy,
                lradius * cos(degree) - mBallRadius * sMagicNumber * sin(degree) + fx, lradius * sin(degree) +
                        mBallRadius * sMagicNumber * cos(degree) + fy, botpullX, botpullY);
        mPath.lineTo(fx, fy);


        //右边
        mPath.moveTo(mBallRadius * sin(degree) + bx, -mBallRadius * cos(degree) + by);
        mPath.cubicTo(-mBallRadius * sMagicNumber * cos(degree) + mBallRadius * sin(degree) + bx,
                -mBallRadius * sMagicNumber * sin(degree) - mBallRadius * cos(degree) + by,
                -lradius * cos(degree) + mBallRadius * sMagicNumber * sin(degree) + bx, -lradius * sin(degree)
                        - mBallRadius * sMagicNumber * cos(degree) + by, toppullX, toppullY);

        mPath.lineTo(bx, by);


        mPath.moveTo(bx - mBallRadius * sin(degree), by + mBallRadius * cos(degree));
        mPath.cubicTo(bx - mBallRadius * sMagicNumber * cos(degree) - mBallRadius * sin(degree), by
                        + mBallRadius * cos(degree) - mBallRadius * sMagicNumber * sin(degree),
                bx - lradius * cos(degree) - mBallRadius * sMagicNumber * sin(degree), by +
                        mBallRadius * sMagicNumber * cos(degree) - lradius * sin(degree), botpullX, botpullY);
        mPath.lineTo(bx, by);

        mPath.moveTo(bx + mBallRadius * sin(degree), -mBallRadius * cos(degree) + by);
        mPath.cubicTo((mBallRadius * sMagicNumber) * cos(degree) + mBallRadius * sin(degree) + bx,
                (mBallRadius * sMagicNumber) * sin(degree) - mBallRadius * cos(degree) + by,
                sradius * cos(degree) + (mBallRadius * sMagicNumber) * sin(degree) + bx, sradius * sin(degree)
                        - (mBallRadius * sMagicNumber) * cos(degree) + by, sradius * cos(degree) + bx,
                sradius * sin(degree) + by);
        mPath.lineTo(bx, by);

        mPath.moveTo(-mBallRadius * sin(degree) + bx, by + mBallRadius * cos(degree));
        mPath.cubicTo(mBallRadius * sMagicNumber * cos(degree) - mBallRadius * sin(degree) + bx,
                mBallRadius * sMagicNumber * sin(degree) + mBallRadius * cos(degree) + by,
                sradius * cos(degree) - mBallRadius * sMagicNumber * sin(degree) + bx, sradius * sin(degree) +
                        mBallRadius * sMagicNumber * cos(degree) + by, sradius * cos(degree) + bx,
                sradius * sin(degree) + by);
        mPath.lineTo(bx, by);

        mPath.moveTo(toppullX, toppullY);
        mPath.lineTo(botpullX, botpullY);
        mPath.lineTo(bx, by);

        mPath.moveTo(toppullX, toppullY);
        mPath.lineTo(botpullX, botpullY);
        mPath.lineTo(fx, fy);

        //paint.setStyle(Paint.Style.STROKE);
        LinearGradient gradient = new LinearGradient(fx - sradius * cos(degree), fy - sradius * sin(degree), sradius
                * cos(degree) + bx, sradius * sin(degree) + by, mGradientColor, null, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        canvas.drawPath(mPath, paint);

    }

    private void drawCask(Canvas canvas, float fx, float fy, float bx, float by, float degree, Paint paint) {
        //画圆桶
        //s (mACircleX, mACircleY - mBallRadius)
        // c1 (mACircleX - mBallRadius * sMagicNumber, mACircleY - mBallRadius)
        // c2 (mACircleX - sradius, mACircleY - mBallRadius * sMagicNumber)
        // e  ( mACircleX - sradius, mACircleY)

        //左边
        mPath.reset();
        mPath.moveTo(mBallRadius * sin(degree) + fx, -mBallRadius * cos(degree) + fy);
        mPath.cubicTo(-mBallRadius * sMagicNumber * cos(degree) + mBallRadius * sin(degree) + fx,
                -mBallRadius * sMagicNumber * sin(degree) - mBallRadius * cos(degree) + fy,
                -mBallRadius * cos(degree) + mBallRadius * sMagicNumber * sin(degree) + fx, -mBallRadius * sin(degree)
                        - mBallRadius * sMagicNumber * cos(degree) + fy, fx - mBallRadius * cos(degree)
                , fy - mBallRadius * sin(degree));
         mPath.lineTo(fx, fy);

        //s (mACircleX, mACircleY + mBallRadius)
        // c1 (mACircleX - mBallRadius * sMagicNumber, mACircleY + mBallRadius)
        // c2 (mACircleX - sradius, mACircleY + mBallRadius * sMagicNumber)
        // e  ( mACircleX - sradius, mACircleY)

       mPath.moveTo(fx - mBallRadius * sin(degree), fy + mBallRadius * cos(degree));
        mPath.cubicTo(fx - mBallRadius * sMagicNumber * cos(degree) - mBallRadius * sin(degree), fy
                        + mBallRadius * cos(degree) - mBallRadius * sMagicNumber * sin(degree),
                fx - mBallRadius * cos(degree) - mBallRadius * sMagicNumber * sin(degree), fy +
                        mBallRadius * sMagicNumber * cos(degree) - mBallRadius * sin(degree), fx - mBallRadius *
                        cos(degree),
                fy - mBallRadius * sin(degree));


        float centerX = (fx + bx) / 2;
        float centerY = (fy + by) / 2;
        float toppullX = centerX + sin(degree) *  mBallRadius ;
        float toppullY = centerY - cos(degree) * mBallRadius ;

        float botpullX = centerX - sin(degree)  * mBallRadius ;
        float botpullY = centerY + cos(degree) * mBallRadius ;



        //右边
        mPath.moveTo(bx + mBallRadius * sin(degree), -mBallRadius * cos(degree) + by);
        mPath.cubicTo((mBallRadius * sMagicNumber) * cos(degree) + mBallRadius * sin(degree) + bx,
                (mBallRadius * sMagicNumber) * sin(degree) - mBallRadius * cos(degree) + by,
                mBallRadius * cos(degree) + (mBallRadius * sMagicNumber) * sin(degree) + bx, mBallRadius * sin(degree)
                        - (mBallRadius * sMagicNumber) * cos(degree) + by, mBallRadius * cos(degree) + bx,
                mBallRadius * sin(degree) + by);

        //s (mACircleX, mACircleY + mBallRadius)
        // c1 (mACircleX + mBallRadius * sMagicNumber, mACircleY + mBallRadius)
        // c2 (mACircleX + lradius, mACircleY + mBallRadius * sMagicNumber)
        // e  (mACircleX + lradius, mACircleY)
        // x0= (x - rx0)*cos(a) - (y - ry0)*sin(a) + rx0 ;
        //y0= (x - rx0)*sin(a) + (y - ry0)*cos(a) + ry0 ;


        mPath.moveTo(-mBallRadius * sin(degree) + bx, by+ mBallRadius * cos(degree));
        mPath.cubicTo(mBallRadius * sMagicNumber * cos(degree) - mBallRadius * sin(degree) + bx,
                mBallRadius * sMagicNumber * sin(degree) + mBallRadius * cos(degree) + by,
                mBallRadius * cos(degree) - mBallRadius * sMagicNumber * sin(degree) + bx, mBallRadius * sin(degree) +
                        mBallRadius * sMagicNumber * cos(degree) + by, mBallRadius * cos(degree) + bx,
                mBallRadius * sin(degree) + by);


        mPath.moveTo(mBallRadius * sin(degree) + fx, -mBallRadius * cos(degree) + fy);
        mPath.lineTo(bx + mBallRadius * sin(degree), -mBallRadius * cos(degree) + by);

        mPath.moveTo(-mBallRadius * sin(degree) + bx, by+ mBallRadius * cos(degree));
        mPath.lineTo(fx - mBallRadius * sin(degree), fy + mBallRadius * cos(degree));

        //paint.setStyle(Paint.Style.STROKE);
        LinearGradient gradient = new LinearGradient( fx - mBallRadius * cos(degree)
                , fy - mBallRadius * sin(degree),  mBallRadius * cos(degree) + bx,
                mBallRadius * sin(degree) + by, mGradientColor, null, Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        canvas.drawPath(mPath, paint);


    }

    private void drawMingle() {

    }

    public void setDegree(float degree) {
        mCurrentDegree = degree;
    }

    public float getDegree() {
        return mCurrentDegree;
    }

    private void Logi(String str) {
        if (DEBUG)
            Log.i(TAG, str);
    }

    private void Logi(String tag, String str) {
        if (DEBUG)
            Log.i(tag, str);
    }

    private void Loge(String error) {
        if (DEBUG)
            Log.e(TAG, error);
    }
}
