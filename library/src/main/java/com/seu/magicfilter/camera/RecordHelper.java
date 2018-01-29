package com.seu.magicfilter.camera;

import android.graphics.Bitmap;
import android.media.AudioRecord;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;

import com.seu.magicfilter.camera.bean.PixelBuffer;
import com.seu.magicfilter.camera.interfaces.OnRecordListener;

import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsEncoder;
import net.ossrs.yasea.SrsMp4Muxer;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 录制数据
 *
 * @author Created by jz on 2017/5/2 11:21
 */
public class RecordHelper implements SrsEncodeHandler.SrsEncodeListener, SrsRecordHandler
        .SrsRecordListener {

    private static final String TAG = "RecordHelper";

    private static final int MAX_CACHE_BUFFER_NUMBER = 1000;

    private ConcurrentLinkedQueue<IntBuffer> mGLIntBufferCache = new ConcurrentLinkedQueue<>();
    private ByteBuffer mGLPreviewBuffer;
    private Thread mThread;
    private HandlerThread mHandlerThread;

    private OnRecordListener mOnRecordListener;


    //yasea encode
    private SrsEncoder mEncoder;
    private SrsMp4Muxer mMp4Muxer;
    private static AudioRecord mic;
    private static AcousticEchoCanceler aec;
    private static AutomaticGainControl agc;
    private byte[] mPcmBuffer = new byte[4096];
    private Thread aworker;
    private boolean sendVideoOnly = false;
    private boolean sendAudioOnly = false;

    private int mPreviewWidth;
    private int mPreviewHeight;

    public RecordHelper() {

    }


    public void setOnRecordListener(OnRecordListener l) {
        this.mOnRecordListener = l;
    }

    public void onRecord(IntBuffer buffer) {
        if (buffer != null)
            mGLIntBufferCache.add(buffer);
    }

    public void setPreview(int width, int height) {
        mPreviewWidth = width;
        mPreviewHeight = height;
        mGLPreviewBuffer = ByteBuffer.allocateDirect(mPreviewWidth * mPreviewHeight * 4);
    }

    public void start(int width, int height,int orientation, File outputFile) {
        if (mThread != null) {
            return;
        }
        mThread = new MyThread();
        mThread.start();

        mHandlerThread = new HandlerThread("encoder");
        mHandlerThread.start();

        if (null == mEncoder) {
            mEncoder = new SrsEncoder(new SrsEncodeHandler(mHandlerThread.getLooper(), this));
            mMp4Muxer = new SrsMp4Muxer(orientation,new SrsRecordHandler(mHandlerThread.getLooper(), this));
            mEncoder.setMp4Muxer(mMp4Muxer);
            mEncoder.setPreviewResolution(width, height);
            mEncoder.setPortraitResolution(width, height);
            mEncoder.setVideoHDMode();
        }

        if (mMp4Muxer != null) {
            startEncode();
            mMp4Muxer.record(outputFile);
        }

    }


    public void startEncode() {
        Log.i(TAG, "startEncode");
        if (!mEncoder.start()) {
            Log.e(TAG, "startEncode error");
            return;
        }

        startAudio();
    }

    public void startAudio() {
        mic = mEncoder.chooseAudioRecord();
        if (mic == null) {
            return;
        }

        if (AcousticEchoCanceler.isAvailable()) {
            aec = AcousticEchoCanceler.create(mic.getAudioSessionId());
            if (aec != null) {
                aec.setEnabled(true);
            }
        }

        if (AutomaticGainControl.isAvailable()) {
            agc = AutomaticGainControl.create(mic.getAudioSessionId());
            if (agc != null) {
                agc.setEnabled(true);
            }
        }

        aworker = new Thread(new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
                mic.startRecording();
                while (!Thread.interrupted()) {
                    if (sendVideoOnly) {
                        mEncoder.onGetPcmFrame(mPcmBuffer, mPcmBuffer.length);
                        try {
                            // This is trivial...
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            break;
                        }
                    } else {
                        int size = mic.read(mPcmBuffer, 0, mPcmBuffer.length);
                        if (size > 0) {
                            mEncoder.onGetPcmFrame(mPcmBuffer, size);
                        }
                    }
                }
            }
        });
        aworker.start();
    }

    public void pause() {

        if (mEncoder != null)
            mEncoder.pause();

        if (mMp4Muxer != null)
            mMp4Muxer.pause();

    }

    public void resume() {

        if (mEncoder != null)
            mEncoder.resume();

        if (mMp4Muxer != null)
            mMp4Muxer.resume();

    }

    public void stop() {
        if (mThread == null) {
            return;
        }
        mThread.interrupt();
        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mThread = null;

        if (mMp4Muxer != null) {
            stopEncode();
            mMp4Muxer.stop();
        }

        if (mHandlerThread != null)
            mHandlerThread.quit();

        Log.i(TAG, "stop");
    }

    public void stopEncode() {
        Log.i(TAG, "stopEncode");
        stopAudio();
        mEncoder.stop();
    }

    public void stopAudio() {
        if (aworker != null) {
            aworker.interrupt();
            try {
                aworker.join();
            } catch (InterruptedException e) {
                aworker.interrupt();
            }
            aworker = null;
        }

        if (mic != null) {
            mic.setRecordPositionUpdateListener(null);
            mic.stop();
            mic.release();
            mic = null;
        }

        if (aec != null) {
            aec.setEnabled(false);
            aec.release();
            aec = null;
        }

        if (agc != null) {
            agc.setEnabled(false);
            agc.release();
            agc = null;
        }
    }

    @Override
    public void onNetworkWeak() {

    }

    @Override
    public void onNetworkResume() {

    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {

    }

    @Override
    public void onBitmap(Bitmap bitmap) {
        if (mOnRecordListener != null)
            mOnRecordListener.onRecord(bitmap);
    }

    @Override
    public void onRecordPause() {
        if (mOnRecordListener != null)
            mOnRecordListener.onRecordPause();
    }

    @Override
    public void onRecordResume() {
        Log.i(TAG, "onRecordResume");
        if (mOnRecordListener != null)
            mOnRecordListener.onRecordResume();
    }

    @Override
    public void onRecordStarted(String msg) {
        Log.i(TAG, "onRecordStarted");
        if (mOnRecordListener != null)
            mOnRecordListener.onRecordStarted();
        else
            Log.e(TAG,"YYAAYY");
    }

    @Override
    public void onRecordFinished(String msg) {
        Log.i(TAG, "onRecordFinished");
        if (mOnRecordListener != null)
            mOnRecordListener.onRecordFinished(msg);
    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        if (mOnRecordListener != null)
            mOnRecordListener.onRecordIllegalArgumentException(e);
    }

    @Override
    public void onRecordIOException(IOException e) {
        if (mOnRecordListener != null)
            mOnRecordListener.onRecordIOException(e);
    }

    private class MyThread extends Thread {
        //转换成Bitmap演示用效率低下，可以用libyuv代替
        //一帧数据大概2M 960*540

        @Override
        public void run() {
            Log.i(TAG, "MyThread run");
            while (!isInterrupted()) {
                if (mGLIntBufferCache.isEmpty()) {
                    SystemClock.sleep(1);
                    continue;
                }

                long frontTime = System.currentTimeMillis();

                IntBuffer picture = mGLIntBufferCache.poll();
                mGLPreviewBuffer.asIntBuffer().put(picture.array());

                mEncoder.onGetRgbaFrame(mGLPreviewBuffer.array(), mPreviewWidth, mPreviewHeight);

                Log.i(TAG, "data run: 4 size=" + mGLIntBufferCache.size() + " time=" + (System.currentTimeMillis() -
                        frontTime));
            }
            mGLIntBufferCache.clear();
        }
    }


}