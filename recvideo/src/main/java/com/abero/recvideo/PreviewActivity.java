package com.abero.recvideo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.seu.magicfilter.MediaRecorderActivity;

import java.io.IOException;

/**
 * Created by abero on 2018/2/28.
 */

public class PreviewActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "PreviewActivity";

    private MediaPlayer mediaPlayer;
    private String path;
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        path = getIntent().getStringExtra(MediaRecorderActivity.VIDEO_URI);
        Log.i(TAG, "onCreate: path="+path);

        setContentView(R.layout.play);

        SurfaceView surfaceView = findViewById(R.id.view);
        surfaceHolder = surfaceView.getHolder();

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceHolder.addCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
      // surfaceHolder.addCallback(null);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDisplay(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
