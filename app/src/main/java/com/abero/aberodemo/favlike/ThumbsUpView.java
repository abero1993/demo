package com.abero.aberodemo.favlike;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.abero.aberodemo.R;


public class ThumbsUpView extends View implements Like, ValueAnimator.AnimatorUpdateListener {

    private static final String TAG = "ThumbsUpView";

    private Drawable selectedDrawable;
    private Drawable unselectedDrawable;

    private Paint mPaint;
    private Paint mLinePaint;

    private ObjectAnimator outAnimator;
    private ObjectAnimator likeInAnimator;
    private ObjectAnimator unlikeInAnimator;
    private ObjectAnimator flashAnimator;
    private ObjectAnimator rotateAnimator;
    private AnimatorSet likeAnimatorSet;
    private AnimatorSet unlikeAnimatorSet;

    private float outProgress = 1;
    private float likeInProgress;
    private float unlikeInProgress = 1;
    private float flashProgress;
    private float rotateProgress;

    private int mWidth;
    private int mHeight;
    private int mDefauleWidth = dp2px(38);
    private int mDefauleHeight = dp2px(38);

    private boolean liked;

    private float maxlineLength;
    private float degree;

    public ThumbsUpView(Context context) {
        this(context, null);
    }

    public ThumbsUpView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbsUpView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        selectedDrawable = getResources().getDrawable(R.drawable.icon_zan_highlight);
        unselectedDrawable = getResources().getDrawable(R.drawable.icon_zan_normal);

        degree = 20;

        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dp2px(2));
        mPaint.setShadowLayer(dp2px(1), dp2px(1), dp2px(1), Color.RED);
        setLayerType(LAYER_TYPE_HARDWARE, mPaint);

        mLinePaint = new Paint();
        mLinePaint.setColor(Color.RED);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(1);

        outAnimator = ObjectAnimator.ofFloat(this, "outProgress", 0, 1);
        likeInAnimator = ObjectAnimator.ofFloat(this, "likeInProgress", 0, 1);
        unlikeInAnimator = ObjectAnimator.ofFloat(this, "unlikeInProgress", 0, 1);
        flashAnimator = ObjectAnimator.ofFloat(this, "flashProgress", 0, 1);
        rotateAnimator = ObjectAnimator.ofFloat(this, "rotateProgress", 0, -degree, 0);

        outAnimator.setDuration(100);
        likeInAnimator.setDuration(600);
        rotateAnimator.setDuration(600);

        outAnimator.addUpdateListener(this);
        likeInAnimator.addUpdateListener(this);
        unlikeInAnimator.addUpdateListener(this);

        likeAnimatorSet = new AnimatorSet();
        unlikeAnimatorSet = new AnimatorSet();
        likeAnimatorSet.play(outAnimator).before(likeInAnimator).with(rotateAnimator);
        unlikeAnimatorSet.play(outAnimator).before(unlikeInAnimator);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST)
            setMeasuredDimension(mDefauleWidth, mDefauleHeight);
        else if (widthMode == MeasureSpec.AT_MOST)
            setMeasuredDimension(mDefauleWidth, heightSize);
        else if (heightMode == MeasureSpec.AT_MOST)
            setMeasuredDimension(widthSize, mDefauleHeight);
    }

    // 画布用于主要内容的绘制比例，剩余的0.05用于点赞时一个红圈扩散效果。
    private float mainDrawScale = 0.95f;
    // 拇指大小占绘制区域的比例
    private float thumbsScale = 0.6f;
    // 拇指图位置偏离绘制中心的距离
    private int thumbsOffsetY;
    // 发光图位置偏离绘制中心的距离
    private int shiningOffsetY;
    // 缩小动画执行时，图片缩小部分的比例
    private float minifyScale = 0.3f;
    // 画拇指的区域
    private Rect thumbsRect = new Rect();
    // 画布绘制的中心点
    private Point centerPoint = new Point();
    // 正方形绘制区域的边长
    private int squareSideLen = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制区域的高和宽
        mWidth = (int) (mainDrawScale * (getWidth() - getPaddingLeft() - getPaddingRight()));
        mHeight = (int) (mainDrawScale * (getHeight() - getPaddingTop() - getPaddingBottom()));

        // 绘制中心
        centerPoint.x = getWidth() / 2;
        centerPoint.y = getHeight() / 2;
        canvas.translate(centerPoint.x, centerPoint.y);

        // 使用正方形的画布来进行绘制，选出宽高较小的作为边长
        if (Math.max(mWidth, mHeight) == mHeight)
            squareSideLen = mWidth;
        else
            squareSideLen = mHeight;

        // 拇指的绘制位置在y轴上的偏移量
        thumbsOffsetY =0;
        // 发光的绘制位置在y轴上的偏移量
        shiningOffsetY = (int) (-0.26 * squareSideLen);
        // 拇指缩小时，跟随拇指往下移动，保持两张图片的一致性
        int animateY = (int) (thumbsScale * minifyScale * outProgress * squareSideLen / 2);

        maxlineLength = (float) (squareSideLen / 2 * 0.40);

        /*** 退出动画 ***/
        // 缩小动画期间，拇指drawable的绘制区域（包括灰色和红色）
        if (outProgress < 1) {

            thumbsRect.left = (int) (-thumbsScale * (1 - minifyScale * outProgress) * squareSideLen / 2);
            thumbsRect.top = (int) (-thumbsScale * (1 - minifyScale * outProgress) * squareSideLen /
                    2) + thumbsOffsetY;
            thumbsRect.right = (int) (thumbsScale * (1 - minifyScale * outProgress) * squareSideLen /
                    2);
            thumbsRect.bottom = (int) (thumbsScale * (1 - minifyScale * outProgress) * squareSideLen
                    / 2) + thumbsOffsetY;

            if (liked) {
                unselectedDrawable.setBounds(thumbsRect);
                unselectedDrawable.draw(canvas);
            } else {
                selectedDrawable.setBounds(thumbsRect);
                selectedDrawable.draw(canvas);
            }
        }


        if (outProgress < 1)
            // 避免if嵌套太多，这里作一个return
            return;

        /*** 进入动画 ***/
        if (liked) {
            // 画拇指
            thumbsRect.left = (int) (-thumbsScale * (1 - minifyScale * (1 - likeInProgress)) *
                    squareSideLen / 2);
            thumbsRect.top = (int) (-thumbsScale * (1 - minifyScale * (1 - likeInProgress)) *
                    squareSideLen / 2) + thumbsOffsetY;
            thumbsRect.right = (int) (thumbsScale * (1 - minifyScale * (1 - likeInProgress)) *
                    squareSideLen / 2);
            thumbsRect.bottom = (int) (thumbsScale * (1 - minifyScale * (1 - likeInProgress)) *
                    squareSideLen / 2) + thumbsOffsetY;
            selectedDrawable.setBounds(thumbsRect);
            canvas.save();
            canvas.rotate(rotateProgress);
            selectedDrawable.draw(canvas);
            canvas.restore();

            drawShining(canvas);

        } else {
            // 画拇指
            thumbsRect.left = (int) (-thumbsScale * (1 - minifyScale * (1 - unlikeInProgress)) * squareSideLen / 2);
            thumbsRect.top = (int) (-thumbsScale * (1 - minifyScale * (1 - unlikeInProgress)) * squareSideLen / 2) +
                    thumbsOffsetY;
            thumbsRect.right = (int) (thumbsScale * (1 - minifyScale * (1 - unlikeInProgress)) * squareSideLen / 2);
            thumbsRect.bottom = (int) (thumbsScale * (1 - minifyScale * (1 - unlikeInProgress)) * squareSideLen / 2)
                    + thumbsOffsetY;
            unselectedDrawable.setBounds(thumbsRect);
            unselectedDrawable.draw(canvas);

        }
    }

    private void drawShining(Canvas canvas) {
        //每条光线的起点构成一条抛物线x（2-x）+len 放弃，数学不好
        if (likeInProgress < 0.5)
            return;

        Path path = new Path();
        float radius = (float) (maxlineLength * 0.4);
        float length = maxlineLength * (likeInProgress - 0.5f) / 0.5f;
        float x, y;
        float sqrt12 = (float) Math.sqrt(1d / 2);
        //左线
        path.moveTo(-length, shiningOffsetY);
        path.lineTo(-maxlineLength, shiningOffsetY);
        //左上线（r-）
        x = (radius - radius * sqrt12);
        y = (shiningOffsetY - radius * sqrt12);
        path.moveTo(x - sqrt12 * length, y - sqrt12 * length);
        path.lineTo((x - sqrt12 * maxlineLength), (y - sqrt12 * maxlineLength));
        //中间的竖线
        x = radius;
        y = shiningOffsetY - radius;
        path.moveTo(x, y - length);
        path.lineTo(x, y - maxlineLength);
        //右上线
        x = radius + sqrt12 * radius;
        y = shiningOffsetY - radius * sqrt12;
        path.moveTo(x + sqrt12 * length, y - sqrt12 * length);
        path.lineTo(x + sqrt12 * maxlineLength, y - sqrt12 * maxlineLength);
        //右线
        x = 2 * radius;
        y = shiningOffsetY;
        path.moveTo(x + length, y);
        path.lineTo(x + maxlineLength, y);

        canvas.drawPath(path, mLinePaint);
    }

    public float getOutProgress() {
        return outProgress;
    }

    public void setOutProgress(float outProgress) {
        this.outProgress = outProgress;
    }

    public float getLikeInProgress() {
        return likeInProgress;
    }

    public void setLikeInProgress(float likeInProgress) {
        this.likeInProgress = likeInProgress;
    }

    public float getUnlikeInProgress() {
        return unlikeInProgress;
    }

    public void setUnlikeInProgress(float unlikeInProgress) {
        this.unlikeInProgress = unlikeInProgress;
    }

    public float getFlashProgress() {
        return flashProgress;
    }

    public void setFlashProgress(float flashProgress) {
        this.flashProgress = flashProgress;
    }

    public float getRotateProgress() {
        return rotateProgress;
    }

    public void setRotateProgress(float rotateProgress) {
        this.rotateProgress = rotateProgress;
    }

    private int dp2px(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + 0.5);
    }

    public void like() {
        liked = true;
        if (likeAnimatorSet.isRunning())
            return;
        if (unlikeAnimatorSet.isRunning())
            unlikeAnimatorSet.cancel();
        likeAnimatorSet.start();
    }

    public void unlike() {
        liked = false;
        if (unlikeAnimatorSet.isRunning())
            return;
        if (likeAnimatorSet.isRunning())
            unlikeAnimatorSet.cancel();
        unlikeAnimatorSet.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }

    @Override
    public void changeLike() {
        if (liked)
            unlike();
        else
            like();
    }
}
