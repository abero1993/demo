package com.abero.aberodemo.favlike;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.abero.aberodemo.R;


/**
 * Created
 * by jaren on 2017/5/26.
 */

public class FavView extends View {
    private static final String TAG = "LikeView";
    private final int mDefaultColor;
    /**
     * 圆最大半径（心形）
     */
    private float mRadius;
    /**
     * View变化用时
     */
    private int mCycleTime;
    /**
     * Bézier曲线画圆的近似常数
     */
    private static final float c = 0.551915024494f;
    /**
     * 选择/取消选择
     */
    private boolean state;
    /**
     * 心形默认颜色
     */
    private static final int CLICKED_CLOLOR = 0xffe53a42;
    /**
     * 环绕圆点的颜色
     */
    private static final int[] dotColors = {0xffdaa9fa, 0xfff2bf4b, 0xffe3bca6, 0xff329aed, 0xffb1eb99, 0xff67c9ad,
            0xffde6bac};
    /**
     * 1.绘制心形并伴随缩小和颜色渐变
     */
    private static final int HEART_VIEW = 0;
    /**
     * 2.绘制圆并伴随放大和颜色渐变
     */
    private static final int CIRCLE_VIEW = 1;
    /**
     * 3.绘制圆环并伴随放大和颜色渐变
     */
    private static final int RING_VIEW = 2;
    /**
     * 4.圆环减消失、心形放大、周围环绕十四圆点
     */
    private static final int RING_DOT__HEART_VIEW = 3;
    /**
     * 5.环绕的十四圆点向外移动并缩小、透明度渐变、渐隐
     */
    private static final int DOT__HEART_VIEW = 4;

    private float mCenterX;
    private float mCenterY;
    private Paint mPaint;
    private float mOffset;
    private OnClickListener mListener;
    private ValueAnimator animatorTime;
    private ValueAnimator animatorArgb;
    private int mCurrentRadius;
    private int mCurrentColor;
    private int mCurrentState;
    private float mCurrentPercent;

    private PointF tPointA;
    private PointF tPointB;
    private PointF tPointC;
    private PointF rPointA;
    private PointF rPointB;
    private PointF rPointC;
    private PointF bPointA;
    private PointF bPointB;
    private PointF bPointC;
    private PointF lPointA;
    private float radiusRatio = (float) (42.2 / 63.4);
    private float rDotL;
    private float rDotS;
    private float offS;
    private float offL;
    private boolean isMax;
    private float dotR;


    public FavView(Context context) {
        this(context, null);
    }

    public FavView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FavView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FavView, defStyleAttr, 0);
        mRadius = array.getDimension(R.styleable.FavView_cirRadius, dp2px(10));
        mCycleTime = array.getInt(R.styleable.FavView_cycleTime, 2000);
        mDefaultColor = array.getColor(R.styleable.FavView_defaultColor, 0Xff657487);
        mOffset = c * mRadius;
        mCenterX = mRadius;
        mCenterY = mRadius;
        mPaint = new Paint();
        mCurrentRadius = (int) mRadius;
        mCurrentColor = mDefaultColor;
        dotR = mRadius / 6;
        Log.i(TAG, "init radius=" + mRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mCenterX, mCenterY);//使坐标原点在canvas中心位置
        switch (mCurrentState) {
            case HEART_VIEW:
                drawHeart(canvas, mCurrentRadius, mCurrentColor);
                break;
            case CIRCLE_VIEW:
                drawCircle(canvas, mCurrentRadius, mCurrentColor);
                break;
            case RING_VIEW:
                drawRing(canvas, mCurrentRadius, mCurrentColor, mCurrentPercent);
                break;
            case RING_DOT__HEART_VIEW:
                drawDotWithRing(canvas, mCurrentRadius, mCurrentColor);
                break;
            case DOT__HEART_VIEW:
                drawDot(canvas, mCurrentRadius, mCurrentColor);
                break;
        }
    }


    //绘制心形
    private void drawHeart(Canvas canvas, int radius, int color) {
        //Log.i(TAG,"radius="+radius);
        initControlPoints(radius);
        mPaint.setColor(color);
        mPaint.setAntiAlias(true);
        if (state) {
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(0xfffe5050);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
        }
        mPaint.setStrokeWidth(dp2px(2));
        Path path = new Path();
        path.moveTo(tPointA.x, tPointA.y);
        path.lineTo(tPointB.x, tPointB.y);
        path.lineTo(tPointC.x, tPointC.y);
        path.lineTo(rPointA.x, rPointA.y);
        path.lineTo(rPointB.x, rPointB.y);
        path.lineTo(rPointC.x, rPointC.y);
        path.lineTo(bPointA.x, bPointA.y);
        path.lineTo(bPointB.x, bPointB.y);
        path.lineTo(bPointC.x, bPointC.y);
        path.lineTo(lPointA.x, lPointA.y);

        path.close();
        canvas.drawPath(path, mPaint);
    }

    //绘制圆
    private void drawCircle(Canvas canvas, int radius, int color) {
        mPaint.setColor(color);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(0f, 0f, radius, mPaint);
    }


    //绘制圆环
    private void drawRing(Canvas canvas, int radius, int color, float percent) {

        mPaint.setColor(color);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2 * mRadius * percent);
        RectF rectF = new RectF(-radius, -radius, radius, radius);
        canvas.drawArc(rectF, 0, 360, false, mPaint);
    }

    //绘制圆点、圆环、心形
    private void drawDotWithRing(Canvas canvas, int radius, int color) {
        Log.i(TAG, "prering radius=" + mCurrentRadius);
        mPaint.setColor(color);
        mPaint.setAntiAlias(true);

        mPaint.setStyle(Paint.Style.STROKE);
        if (mCurrentPercent <= 1) {
            RectF rectF = new RectF(-radius, -radius, radius, radius);
            canvas.drawArc(rectF, 0, 360, false, mPaint);
        }
        mCurrentPercent = (1f - mCurrentPercent > 1f ? 1f : 1f - mCurrentPercent) * 0.2f;//用于计算圆环宽度，最小0，与动画进度负相关
        mPaint.setStrokeWidth(2 * mRadius * mCurrentPercent);

        float innerR = radius - mRadius * mCurrentPercent + dotR;
        double angleA = 0;
        double angleB = -Math.PI / 20;

        offS += dotR / 17;
        offL += dotR / 14;
        rDotS = radius - mRadius / 12 / 2 + offS;
        rDotL = innerR + offL;

        mPaint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 7; i++) {
            canvas.drawCircle((float) (rDotS * Math.sin(angleA)), (float) (rDotS * Math.cos(angleA)), dotR, mPaint);
            angleA += 2 * Math.PI / 7;
            canvas.drawCircle((float) (rDotL * Math.sin(angleB)), (float) (rDotL * Math.cos(angleB)), dotR, mPaint);
            angleB += 2 * Math.PI / 7;
        }
        mCurrentRadius = (int) (mRadius / 3 + offL * 4);

        Log.i(TAG, "ring radius=" + mCurrentRadius);
        drawHeart(canvas, mCurrentRadius, CLICKED_CLOLOR);

    }

    //绘制圆点、心形
    private void drawDot(Canvas canvas, int radius, int color) {
        Log.i(TAG, "predot radius=" + mCurrentRadius);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        double angleA = 0;
        double angleB = -Math.PI / 20;
        float dotRS;
        float dotRL;
        if (rDotL < 2.6 * mRadius) {//限制圆点的扩散范围
            rDotS += dotR / 17;
            rDotL += dotR / 14;
        }
        if (!isMax && mCurrentRadius <= 1.1 * mRadius) {
            offL += dotR / 14;
            mCurrentRadius = (int) (mRadius / 3 + offL * 4);

        } else {
            isMax = true;
            mCurrentRadius= (int) mRadius;
        }

        if (isMax && mCurrentRadius > mRadius) {
            mCurrentRadius = (int) (mCurrentRadius - dotR / 16);

        }
        Log.i(TAG, "predot radius2=" + mCurrentRadius);
        drawHeart(canvas, mCurrentRadius, CLICKED_CLOLOR);

        mPaint.setAlpha((int) (255 * (1 - mCurrentPercent)));//圆点逐渐透明
        dotRS = (float) (dotR * (1 - mCurrentPercent));
        dotRL = (float) (dotR * (1 - mCurrentPercent) * 4) > dotR ? dotR : (float) (dotR * (1 - mCurrentPercent) * 3);
        for (int i = 0; i < 7; i++) {
            mPaint.setColor(dotColors[i]);
            canvas.drawCircle((float) (rDotS * Math.sin(angleA)), (float) (rDotS * Math.cos(angleA)), dotRS, mPaint);
            angleA += 2 * Math.PI / 7;
            canvas.drawCircle((float) (rDotL * Math.sin(angleB)), (float) (rDotL * Math.cos(angleB)), dotRL, mPaint);
            angleB += 2 * Math.PI / 7;
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mWidth, mHeight;
        mWidth = (int) (5.2 * mRadius + 2 * dotR);
        mHeight = (int) (5.2 * mRadius + 2 * dotR);
        setMeasuredDimension(mWidth, mHeight);

    }

    /**
     * 初始化Bézier 曲线四组控制点
     */
    private void initControlPoints(int mRadius) {
        mOffset = c * mRadius;

        tPointA = new PointF(0, -mRadius); //A
        tPointB = new PointF((float) (mRadius * asin(36) * radiusRatio), -(float) (mRadius * acos(36) * radiusRatio))
        ;//B
        tPointC = new PointF((float) (mRadius * acos(18)), (float) (-mRadius * asin(18)));//C
        rPointA = new PointF((float) (mRadius * acos(18) * radiusRatio), (float) (mRadius * asin(18) * radiusRatio));//D
        rPointB = new PointF((float) (mRadius * asin(36)), (float) (mRadius * acos(36)));//E
        rPointC = new PointF(0, mRadius * radiusRatio);//F
        bPointA = new PointF(-(float) (mRadius * asin(36)), (float) (mRadius * acos(36))); //G
        bPointB = new PointF(-(float) (mRadius * acos(18) * radiusRatio), (float) (mRadius * asin(18) * radiusRatio))
        ;//H
        bPointC = new PointF(-(float) (mRadius * acos(18)), (float) (-mRadius * asin(18)));//I
        lPointA = new PointF(-(float) (mRadius * asin(36) * radiusRatio), -(float) (mRadius * acos(36) * radiusRatio));

    }

    float cos(int num) {
        return (float) Math.cos(num * Math.PI / 180);
    }

    float sin(int num) {
        return (float) Math.sin(num * Math.PI / 180);
    }

    private double asin(int a) {
        return Math.abs(sin(a));
    }

    private double acos(int a) {
        return Math.abs(cos(a));
    }


    /**
     * 展现View点击后的变化效果
     */
    private void startViewMotion() {
        if (animatorTime != null && animatorTime.isRunning())
            return;
        Log.i(TAG, "start radius=" + mRadius);
        resetState();
        animatorTime = ValueAnimator.ofInt(0, 1200);
        animatorTime.setDuration(mCycleTime);
        animatorTime.setInterpolator(new LinearInterpolator());//需要随时间匀速变化
        animatorTime.start();
        animatorTime.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();

                if (animatedValue == 0) {
                    if (animatorArgb == null || !animatorArgb.isRunning()) {
                        animatorArgb = ofArgb(mDefaultColor, 0Xfffe5050, 0Xffde7bcc);//100?
                        animatorArgb.setDuration(mCycleTime * 28 / 120);
                        animatorArgb.setInterpolator(new LinearInterpolator());
                        animatorArgb.start();
                    }
                } else if (animatedValue <= 100) {
                    float percent = calcPercent(0f, 100f, animatedValue);
                    mCurrentRadius = (int) (mRadius - mRadius * percent);
                    if (animatorArgb != null && animatorArgb.isRunning())
                        mCurrentColor = (int) animatorArgb.getAnimatedValue();
                    mCurrentState = HEART_VIEW;
                    Log.i(TAG, "HEART radius=" + mCurrentRadius);
                    invalidate();

                } else if (animatedValue <= 280) {
                    float percent = calcPercent(100f, 340f, animatedValue);//此阶段未达到最大半径
                    mCurrentRadius = (int) (2 * mRadius * percent);
                    if (animatorArgb != null && animatorArgb.isRunning())
                        mCurrentColor = (int) animatorArgb.getAnimatedValue();
                    mCurrentState = CIRCLE_VIEW;
                    Log.i(TAG, "CIRCLE radius=" + mCurrentRadius);
                    invalidate();
                } else if (animatedValue <= 340) {
                    float percent = calcPercent(100f, 340f, animatedValue);//半径接上一阶段增加，此阶段外环半径已经最大值
                    mCurrentPercent = 1f - percent + 0.2f > 1f ? 1f : 1f - percent + 0.2f;//用于计算圆环宽度，最小0.2，与动画进度负相关
                    mCurrentRadius = (int) (2 * mRadius * percent);
                    if (animatorArgb != null && animatorArgb.isRunning())
                        mCurrentColor = (int) animatorArgb.getAnimatedValue();
                    mCurrentState = RING_VIEW;
                    Log.i(TAG, "RING radius=" + mCurrentRadius);
                    invalidate();
                } else if (animatedValue <= 580) {
                    float percent = calcPercent(340f, 480f, animatedValue);//内环半径增大直至消亡
                    mCurrentPercent = percent;
                    mCurrentRadius = (int) (2 * mRadius);//外环半径不再改变
                    mCurrentState = RING_DOT__HEART_VIEW;
                    Log.i(TAG, "RING_DOT radius=" + mCurrentRadius);
                    invalidate();
                } else if (animatedValue <= 1200) {
                    float percent = calcPercent(480f, 1200f, animatedValue);
                    mCurrentPercent = percent;
                    mCurrentState = DOT__HEART_VIEW;
                    if (animatedValue == 1200) {
                        animatorTime.cancel();
                        animatorTime.removeAllListeners();
                    }
                    invalidate();

                }


            }
        });


    }

    /**
     * 重置为初始状态
     */
    private void resetState() {
        mCurrentPercent = 0;
        mCurrentRadius = 0;
        isMax = false;
        rDotS = 0;
        rDotL = 0;
        offS = 0;
        offL = 0;
        state = true;


    }

    private float calcPercent(float start, float end, float current) {
        return (current - start) / (end - start);
    }


    /**
     * @param values
     * @return 由于颜色变化的动画API是SDK21 添加的，这里导入了源码的 ArgbEvaluator
     */
    private ValueAnimator ofArgb(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        anim.setEvaluator(ArgbEvaluator.getInstance());
        return anim;
    }

    private float dp2px(int value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:

                if (mListener != null)
                    mListener.onClick(this);
                break;
        }

        return true;
    }

    public void fav() {
        if (getVisibility() == INVISIBLE || getVisibility() == GONE) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    startViewMotion();
                }
            }, 300);
        } else {
            startViewMotion();
        }
    }

    public void unfav() {
        deselectLike();
    }


    /**
     * 取消选择
     */
    private void deselectLike() {
        if (animatorTime != null && animatorTime.isRunning())
            return;
        mCurrentColor = mDefaultColor;
        mCurrentRadius = (int) mRadius;
        mCurrentState = HEART_VIEW;
        state = false;
        invalidate();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        mListener = l;
    }

    /**
     * Indicates whether this LikeView is  selected	 or not.
     *
     * @return true if the LikeView is selected now, false is deselected
     */
    public boolean getState() {
        return this.state;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (animatorTime != null)
            animatorTime.removeAllListeners();
        if (animatorArgb != null)
            animatorArgb.removeAllListeners();

    }
}
