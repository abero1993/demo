package com.abero.loadingview;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;

/**
 * Created by abero on 2015/10/18.
 */
public class CubeTransitionIndicator extends Indicator {

    private static final String TAG = "CubeTransitionIndicator";

    float[] translateX = new float[4], translateY = new float[4];
    float[] degrees = new float[4];

    @Override
    public void draw(Canvas canvas, Paint paint) {
        float rWidth = getWidth() / 5;
        float rHeight = getHeight() / 5;
        for (int i = 0; i < 4; i++) {
            canvas.save();
            canvas.translate(translateX[i], translateY[i]);
            RectF rectF = new RectF(-rWidth / 2, -rHeight / 2, rWidth / 2, rHeight / 2);
            //paint.setColor(getColor());
            canvas.rotate(degrees[i]);
             Log.i(TAG, "deg=" + degrees[i] + "  index=" + i);
            // Log.i(TAG, "x=" + translateX[i] + " y=" + translateY[i] + "  index=" + i);
            //Log.i(TAG,"w="+rWidth+" h="+rHeight);
            canvas.drawRect(rectF, paint);

            canvas.restore();
        }
    }

    @Override
    public ArrayList<ValueAnimator> onCreateAnimators() {

        Log.i(TAG, "onCreateAnimators");
        ArrayList<ValueAnimator> animators = new ArrayList<>();
        float rectangleHeight = getWidth() / 5;
        float centreX = getWidth() / 2;
        float centreY = getHeight() / 2;
        float diagonal = (float) Math.sqrt(2) * getWidth() / 5;
        float sag = rectangleHeight * (17.0f / 50);

        for (int i = 0; i < 4; i++) {
            final int index = i;

            ValueAnimator translationXAnim = null;
            ValueAnimator translationYAnim = null;
            ValueAnimator rotateAnim = null;

            Log.i(TAG, "rectangle height=" + rectangleHeight + " sag=" + sag);
            Log.i(TAG, "centreX=" + centreX + " centreY=" + centreY + " dia=" + diagonal);
            if (0 == index) {
                translateX[index] = centreX;
                translateY[index] = centreY - (diagonal / 2);
                degrees[index]=45;
                translationYAnim = ValueAnimator.ofFloat(centreY - diagonal / 2, centreY -
                        diagonal / 2 - rectangleHeight / 2, centreY - diagonal / 2 - rectangleHeight, centreY -
                        diagonal / 2, centreY - diagonal / 2 + sag);

                translationYAnim = ValueAnimator.ofFloat(centreY - diagonal / 2 + sag, centreY - diagonal / 2 -
                        rectangleHeight);
                //无解？
                rotateAnim = ValueAnimator.ofFloat(45, 135);
            }

            if (1 == index) {
                translateX[index] = centreX - (diagonal / 2);
                translateY[index] = centreY;
                degrees[index]=135;
                translationXAnim = ValueAnimator.ofFloat(centreX - diagonal / 2, centreX -
                        diagonal / 2 - rectangleHeight / 2, centreX -
                        diagonal / 2 - rectangleHeight, centreX - diagonal / 2, centreX - diagonal
                        / 2 + sag);
                translationXAnim = ValueAnimator.ofFloat(centreX - diagonal / 2 + sag, centreX - diagonal / 2 -
                        rectangleHeight);

                rotateAnim = ValueAnimator.ofFloat(135, 45);
            }

            if (2 == index) {
                translateX[index] = centreX;
                translateY[index] = centreY + (diagonal / 2);
                degrees[index]=45;
                translationYAnim = ValueAnimator.ofFloat(centreY + diagonal / 2, centreY +
                                diagonal / 2 + rectangleHeight / 2, centreY +
                                diagonal / 2 + rectangleHeight, centreY + diagonal / 2,
                        centreY + diagonal / 2 - sag);
                translationYAnim = ValueAnimator.ofFloat(centreY + diagonal / 2 - sag, centreY + diagonal / 2 +
                        rectangleHeight);

                rotateAnim = ValueAnimator.ofFloat(45,135);
            }

            if (3 == index) {
                translateX[index] = centreX + (diagonal / 2);
                translateY[index] = centreY;
                degrees[index]=135;
                translationXAnim = ValueAnimator.ofFloat(centreX + diagonal / 2, centreX +
                                diagonal / 2 + rectangleHeight / 2, centreX +
                                diagonal / 2 + rectangleHeight, centreX + diagonal / 2,
                        centreX + diagonal / 2 - sag);
                translationXAnim = ValueAnimator.ofFloat(centreX + diagonal / 2 - sag, centreX + diagonal / 2 +
                        rectangleHeight);

                rotateAnim = ValueAnimator.ofFloat(135,45);
            }

            long duration = 800;
            if (translationXAnim != null) {
                translationXAnim.setInterpolator(new HesitateInterPolator());
                translationXAnim.setDuration(duration);
                translationXAnim.setRepeatCount(-1);
                translationXAnim.setStartDelay(50);
                addUpdateListener(translationXAnim, new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        translateX[index] = (float) animation.getAnimatedValue();
                        //Log.i(TAG, "Test X index=" + index + " value=" + (float) animation.getAnimatedValue());
                        postInvalidate();
                    }
                });


                animators.add(translationXAnim);
            }

            if (translationYAnim != null) {
                translationYAnim.setDuration(duration);
                translationYAnim.setInterpolator(new HesitateInterPolator());
                translationYAnim.setRepeatCount(-1);
                translationYAnim.setStartDelay(50);
                addUpdateListener(translationYAnim, new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        translateY[index] = (float) animation.getAnimatedValue();
                        //Log.i(TAG, "Test Y index=" + index + " value=" + (float) animation.getAnimatedValue());
                        postInvalidate();
                    }
                });

                animators.add(translationYAnim);

            }

            if (rotateAnim != null) {
                rotateAnim.setDuration(duration);
                rotateAnim.setInterpolator(new MyLinearInterpolator());
                rotateAnim.setRepeatCount(-1);
                rotateAnim.setStartDelay(50);
                addUpdateListener(rotateAnim, new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        degrees[index] = (float) animation.getAnimatedValue();
                        postInvalidate();
                    }
                });

                animators.add(rotateAnim);
            }

        }

        return animators;
    }
}
