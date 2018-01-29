package com.seu.magicfilter.camera.interfaces;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * 录制数据
 *
 * @author Created by jz on 2017/5/2 11:10
 */
public interface OnRecordListener {

    void onRecord(Bitmap bitmap);

    void onRecordFinished(String filePath);

    void onRecordIOException(IOException e);

    void onRecordIllegalArgumentException(IllegalArgumentException e);

    void onRecordStarted();

    void onRecordResume();

    void onRecordPause();


}
