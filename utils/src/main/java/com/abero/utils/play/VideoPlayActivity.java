package com.abero.utils.play;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.ocean.motube.R;
import com.ocean.motube.hj.http.HttpMethods;

/**
 * Created by abero on 2017/9/14.
 */

public class VideoPlayActivity extends AppCompatActivity {

    public static String EXTRA_ID = "extra_id";
    private VideoPlayFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        long mediaId = 415;
        if (getIntent() != null) mediaId = getIntent().getLongExtra(EXTRA_ID, 415);

        fragment = (VideoPlayFragment) getSupportFragmentManager().findFragmentById(R.id.base_content);
        if (null == fragment) {
            fragment = VideoPlayFragment.newInstance();
        }

        if (!fragment.isActive())
            getSupportFragmentManager().beginTransaction().add(R.id.base_content, fragment).commit();
        new VideoPlayPresenter(HttpMethods.getInstance(), fragment, mediaId);
    }
    /*modify by HJ    begin*/
    //public static void startIntent(AppCompatActivity activity, int mediaId) {
    public static void startIntent(Activity activity, long mediaId) {
        /*modify by HJ end*/
        if (activity != null) {
            Intent intent = new Intent(activity, VideoPlayActivity.class);
            intent.putExtra(EXTRA_ID, mediaId);
            activity.startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (fragment != null) fragment.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (fragment != null) return fragment.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
