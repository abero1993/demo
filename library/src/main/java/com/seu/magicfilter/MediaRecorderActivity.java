package com.seu.magicfilter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.seu.magicfilter.camera.CameraGlSurfaceView;
import com.seu.magicfilter.camera.interfaces.OnErrorListener;
import com.seu.magicfilter.camera.interfaces.OnRecordListener;

import net.ossrs.yasea.DeviceUtils;
import net.ossrs.yasea.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;

/**
 * 视频录制
 */
public class MediaRecorderActivity extends Activity implements OnClickListener {

    private static final String TAG = "MediaRecorderActivity";
    /**
     * 刷新进度条
     */
    private static final int HANDLE_INVALIDATE_PROGRESS = 0;
    /**
     * 延迟拍摄停止
     */
    private static final int HANDLE_STOP_RECORD = 1;
    /**
     * 下一步
     */
    private ImageView mTitleNext;
    /**
     * 前后摄像头切换
     */
    private CheckBox mCameraSwitch;
    /**
     * 闪光灯
     */
    private CheckBox mRecordLed;
    /**
     * 拍摄按钮
     */
    private ImageView mRecordController;

    /**
     * 选择本地视频上传
     */
    private ImageView mUploadlocalImge;

    /**
     * 放弃本次录制
     */
    private ImageView mDeleteImage;

    /**
     * 时间layout
     */
    private LinearLayout mTimeLayout;
    private TextView mTimeTextView;
    private int second = 0;

    /**
     * 是否是点击状态
     */
    private volatile boolean mPressedStatus;
    /**
     * 是否已经释放
     */
    private volatile boolean mReleased;
    /**
     * 视屏地址
     */
    public final static String VIDEO_URI = "video_uri";
    /**
     * 本次视频保存的文件夹地址
     */
    public final static String OUTPUT_DIRECTORY = "output_directory";
    /**
     * 视屏截图地址
     */
    public final static String VIDEO_SCREENSHOT = "video_screenshot";
    /**
     * 录制完成后需要跳转的activity
     */
    public final static String OVER_ACTIVITY_NAME = "over_activity_name";
    public final static String UPLOAD_LOCAL_ACTIVITY_NAME = "upload_local_activity_name";
    /**
     * 最大录制时间的key
     */
    public final static String MEDIA_RECORDER_MAX_TIME_KEY = "media_recorder_max_time_key";
    /**
     * 最小录制时间的key
     */
    public final static String MEDIA_RECORDER_MIN_TIME_KEY = "media_recorder_min_time_key";
    /**
     * 录制配置key
     */
    public final static String MEDIA_RECORDER_CONFIG_KEY = "media_recorder_config_key";

    private final String MEDIA_DIR = "motube";

    private RelativeLayout mTitleLayout;

    private final int PERMISSION_REQUEST_CODE = 0x001;

    private String mVideoCachePath;
    private String mSaveMp4Path;

    private static final String[] permissionManifest = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public enum RecorderStatus {
        PREPARE, RECORDING, PAUSE
    }

    private RecorderStatus mCurrentStatus;
    private CameraGlSurfaceView mCameraGlSurfaceView;

    /**
     * @param context
     * @param overGOActivityName 录制结束后需要跳转的Activity全类名
     * @param uploadGOActivity   选择本地需要跳转的activity全类名
     */
    public static void goSmallVideoRecorder(Context context, String overGOActivityName, String uploadGOActivity) {
        context.startActivity(new Intent(context, MediaRecorderActivity.class).putExtra(OVER_ACTIVITY_NAME,
                overGOActivityName).putExtra(UPLOAD_LOCAL_ACTIVITY_NAME, uploadGOActivity));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取消标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 防止锁屏
    }

    private void initSmallVideo() {
        // 设置拍摄视频缓存路径
        File dcim = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (dcim.exists()) {
                mVideoCachePath = dcim + File.separator + MEDIA_DIR + File.separator;
            } else {
                mVideoCachePath = dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + MEDIA_DIR +
                        File.separator;
            }
        } else {
            mVideoCachePath = dcim + File.separator + MEDIA_DIR + File.separator;
        }
        Log.i(TAG, "path=" + mVideoCachePath);
    }

    /**
     * 加载视图
     */
    private void loadViews() {
        setContentView(R.layout.activity_media_recorder_ab);
        mCameraGlSurfaceView = (CameraGlSurfaceView) findViewById(R.id.glsurfaceview_camera);
        mTitleLayout = (RelativeLayout) findViewById(R.id.title_layout);
        mCameraSwitch = (CheckBox) findViewById(R.id.record_camera_switcher);
        mTitleNext = (ImageView) findViewById(R.id.title_next);
        mRecordController = (ImageView) findViewById(R.id.record_controller);
        mUploadlocalImge = (ImageView) findViewById(R.id.title_upload_local);
        mDeleteImage = (ImageView) findViewById(R.id.title_dete);
        mRecordLed = (CheckBox) findViewById(R.id.record_camera_led);
        mTimeLayout = (LinearLayout) findViewById(R.id.record_time_lay);
        mTimeTextView = (TextView) findViewById(R.id.record_length);

        mTitleNext.setOnClickListener(this);
        findViewById(R.id.title_back).setOnClickListener(this);
        //mRecordDelete.setOnClickListener(this);
        // mRecordController.setOnTouchListener(mOnVideoControllerTouchListener);
        mRecordController.setOnClickListener(mOnVideoContrlllerListener);
        mUploadlocalImge.setOnClickListener(this);
        mDeleteImage.setOnClickListener(this);

        mCurrentStatus = RecorderStatus.PREPARE;

        // 是否支持前置摄像头
        if (isSupportFrontCamera()) {
            mCameraSwitch.setOnClickListener(this);
        } else {
            mCameraSwitch.setVisibility(View.GONE);
        }
        // 是否支持闪光灯
        if (DeviceUtils.isSupportCameraLedFlash(getPackageManager())) {
            mRecordLed.setOnClickListener(this);
        } else {
            mRecordLed.setVisibility(View.GONE);
        }

        mCameraGlSurfaceView.setOnErrorListener(new OnErrorListener() {
            @Override
            public void onError(String msg) {
                Toast.makeText(MediaRecorderActivity.this.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                finish();
            }
        });

        mCameraGlSurfaceView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onRecord(Bitmap bitmap) {

            }

            @Override
            public void onRecordFinished(String filePath) {
                Log.i(TAG, "onRecordFinished: " + filePath);
                mSaveMp4Path = filePath;
                onEncodeComplete();

                MediaRecorderActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse
                        ("file://" + filePath)));
            }

            @Override
            public void onRecordIOException(IOException e) {
                Toast.makeText(MediaRecorderActivity.this, R.string.record_some_error, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onRecordIllegalArgumentException(IllegalArgumentException e) {
                Toast.makeText(MediaRecorderActivity.this, R.string.record_some_error, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onRecordStarted() {
                Log.i(TAG, "onRecordStarted");
                if (mHandler != null) {
                    mHandler.removeMessages(HANDLE_INVALIDATE_PROGRESS);
                    mHandler.sendEmptyMessage(HANDLE_INVALIDATE_PROGRESS);
                }
            }

            @Override
            public void onRecordResume() {
                if (mHandler != null) {
                    mHandler.removeMessages(HANDLE_INVALIDATE_PROGRESS);
                    mHandler.sendEmptyMessage(HANDLE_INVALIDATE_PROGRESS);
                }
            }

            @Override
            public void onRecordPause() {
                if (mHandler != null) {
                    mHandler.removeMessages(HANDLE_INVALIDATE_PROGRESS);
                }
            }
        });

    }

    /**
     * 是否支持前置摄像头
     */
    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isSupportFrontCamera() {
        if (!DeviceUtils.hasGingerbread()) {
            return false;
        }
        int numberOfCameras = Camera.getNumberOfCameras();
        if (2 == numberOfCameras) {
            return true;
        }
        return false;
    }

    private void permissionCheck() {

        if (Build.VERSION.SDK_INT >= 23) {
            boolean permissionState = true;
            for (String permission : permissionManifest) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissionState = false;
                }
            }
            if (!permissionState) {
                ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE);
            } else {
                init();
            }
        } else {
            init();
        }
    }

    private void showNotShouldShowDialog(String permission) {
        int msg;
        if (permission.equals(Manifest.permission.CAMERA)) {
            msg = R.string.record_not_should_show_request_camera;
        } else if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
            msg = R.string.record_not_should_show_request_mic;
        } else if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE) || permission.equals(Manifest
                .permission.WRITE_EXTERNAL_STORAGE)) {
            msg = R.string.record_not_should_show_request_storage;
        } else {
            msg = R.string.record_not_should_show_request_camera;
        }

        new AlertDialog.Builder(this).setTitle(R.string.record_request_perission)
                .setMessage(msg)
                .setNegativeButton(R.string.record_request_dialog_cancle, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.record_request_dialog_sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", MediaRecorderActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                        //finish();
                    }
                }).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            int count = 0;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "requestpermission success  " + permissions[i]);
                    count++;
                } else {
                    Log.e(TAG, "requestpermission failed");
                    showNotShouldShowDialog(permissions[i]);
                    break;
                }
            }

            if (count >= 4)
                init();
        }
    }


    /**
     * 点击屏幕录制
     */

    private OnClickListener mOnVideoContrlllerListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (null == mCameraGlSurfaceView)
                return;

            if (RecorderStatus.RECORDING != mCurrentStatus) {
                // 检测是否手动对焦
                // 判断是否已经超时

                if (RecorderStatus.PREPARE == mCurrentStatus) {
                    Log.i(TAG, "startRecord");
                    startRecord();
                    mUploadlocalImge.setVisibility(View.GONE);
                } else {
                    Log.i(TAG, "resumeRecord");
                    setStartUI();
                    mCameraGlSurfaceView.resumeRecord();
                    mCurrentStatus = RecorderStatus.RECORDING;
                }
            } else {
                Log.i(TAG, "pauseRecord");
                mCameraGlSurfaceView.pauseRecord();
                setStopUI();
                mCurrentStatus = RecorderStatus.PAUSE;
                mHandler.removeMessages(HANDLE_INVALIDATE_PROGRESS);
            }
        }
    };


    private void init() {
        initSmallVideo();
        loadViews();
    }


    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
        permissionCheck();
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        if (mCameraGlSurfaceView != null) {
            //mCameraGlSurfaceView.resumeRecord();
            mCameraGlSurfaceView.resume();
            //mCameraGlSurfaceView.setFilter(MagicFilterType.SUNRISE);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause:");
        super.onPause();
        if (mCameraGlSurfaceView != null) {
            // mCameraGlSurfaceView.pauseRecord();
            mCameraGlSurfaceView.pause();
        }
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraGlSurfaceView != null) {
            mCameraGlSurfaceView.stopRecord();
            mCameraGlSurfaceView.stop();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 开始录制
     */
    private void startRecord() {
        Log.i(TAG, "startRecord");
        mVideoCachePath = mVideoCachePath + System.currentTimeMillis();
        File file = new File(mVideoCachePath);
        if (file.exists())
            file.delete();

        if (file.mkdirs()) {
            Log.i(TAG, "startRecord");
            mCameraGlSurfaceView.startRecord(mVideoCachePath + File.separator + "" + System.currentTimeMillis() + "" +
                    ".mp4");
            setStartUI();
            mCurrentStatus = RecorderStatus.RECORDING;
        }
    }

    private void setStartUI() {
        Log.i(TAG, "setStartUI:");
        mPressedStatus = true;
        //TODO 开始录制的图标
        //mRecordController.animate().scaleX(0.8f).scaleY(0.8f).setDuration(500).start();
        mRecordController.setBackgroundResource(R.drawable.paisheing);
        mDeleteImage.setVisibility(View.GONE);
        mTitleNext.setVisibility(View.GONE);

        //mRecordDelete.setVisibility(View.GONE);
        mCameraSwitch.setEnabled(false);
        mRecordLed.setEnabled(false);
        mTimeLayout.setVisibility(View.VISIBLE);
        mTitleLayout.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onBackPressed() {

        /*if (mRecordDelete != null && mRecordDelete.isChecked()) {
            cancelDelete();
            return;
        }*/

        if (RecorderStatus.PREPARE == mCurrentStatus) {
            finish();
            return;
        }
        deleteRecord();

    }

    /**
     * 提示是否删除录制
     */

    private void deleteRecord() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.hint)
                .setMessage(R.string.record_camera_exit_dialog_message)
                .setNegativeButton(
                        R.string.record_camera_cancel_dialog_yes,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mCameraGlSurfaceView.setOnRecordListener(null);
                                mCameraGlSurfaceView.stopRecord();
                                finish();
                            }

                        })
                .setPositiveButton(R.string.record_camera_cancel_dialog_no, null).setCancelable
                (false).show();
        return;

    }

    /**
     * 停止录制
     */
    private void stopRecord() {
        Log.i(TAG, "stopRecord");
        mCameraGlSurfaceView.stopRecord();
        setStopUI();
        showProgress("", getString(R.string.record_camera_progress_message));

    }

    private void setStopUI() {
        Log.i(TAG, "setStopUI");
        mPressedStatus = false;
        //mRecordController.animate().scaleX(1).scaleY(1).setDuration(500).start();
        mRecordController.setBackgroundResource(R.drawable.paishe);

        mCameraSwitch.setEnabled(true);
        mRecordLed.setEnabled(true);

        mHandler.removeMessages(HANDLE_STOP_RECORD);

        mTimeLayout.setVisibility(View.INVISIBLE);
        mTitleLayout.setVisibility(View.VISIBLE);
        runOnUiThread(new Runnable() { //三星c5000手机不加这个会不显示，what tf
            @Override
            public void run() {
                mDeleteImage.setVisibility(View.VISIBLE);
                mTitleNext.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onClick(View v) {

        final int id = v.getId();
        if (mHandler.hasMessages(HANDLE_STOP_RECORD)) {
            mHandler.removeMessages(HANDLE_STOP_RECORD);
        }

        if (id == R.id.title_back) {
            onBackPressed();
        } else if (id == R.id.record_camera_switcher) {// 前后摄像头切换

            if (mRecordLed.isChecked()) {
                mCameraGlSurfaceView.toggleFlashMode();
                mRecordLed.setChecked(false);
            }

            if (mCameraGlSurfaceView.isFrontCamera()) {
                mCameraGlSurfaceView.switchCamera(null);
                mRecordLed.setEnabled(true);
            } else {
                mCameraGlSurfaceView.switchCamera(null);
                mRecordLed.setEnabled(false);
            }

        } else if (id == R.id.record_camera_led) {// 闪光灯
            // 开启前置摄像头以后不支持开启闪光灯

            if (mCameraGlSurfaceView.isFrontCamera()) {
                mRecordLed.setChecked(false);
                return;
            }

            mCameraGlSurfaceView.toggleFlashMode();

        } else if (id == R.id.title_next) {// 停止录制
            stopRecord();
            /*finish();
            overridePendingTransition(R.anim.push_bottom_in,
					R.anim.push_bottom_out);*/
        } else if (R.id.title_upload_local == id) {
            try {
                Intent intent = new Intent(this, Class.forName(getIntent().getStringExtra(UPLOAD_LOCAL_ACTIVITY_NAME)));
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } else if (R.id.title_dete == id) {
            deleteRecord();
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_INVALIDATE_PROGRESS:
                    if (!isFinishing()) {
                        second++;
                        // if (mPressedStatus)
                        // titleText.setText(String.format("%.1f",
                        // mMediaRecorder.getDuration() / 1000F));
                        if (RecorderStatus.RECORDING != mCurrentStatus)
                            return;

                        mTimeTextView.setText(stringForTime(second * 1000));
                        if (mPressedStatus)
                            sendEmptyMessageDelayed(HANDLE_INVALIDATE_PROGRESS, 1000);
                    }
                    break;
            }
        }
    };

    private String stringForTime(int timeMs) {
        StringBuilder builder = new StringBuilder();
        Formatter formatter = new Formatter(builder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    public void onEncodeComplete() {
        Log.i(TAG, "onEncodeComplete");
        if (isFinishing())
            return;
        hideProgress();
        Intent intent = null;
        try {
            intent = new Intent(this, Class.forName(getIntent().getStringExtra(OVER_ACTIVITY_NAME)));
            intent.putExtra(MediaRecorderActivity.OUTPUT_DIRECTORY, mVideoCachePath);
            intent.putExtra(MediaRecorderActivity.VIDEO_URI, mSaveMp4Path);
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("需要传入录制完成后跳转的Activity的全类名");
        }

        finish();
    }


    protected ProgressDialog mProgressDialog;

    public ProgressDialog showProgress(String title, String message) {
        return showProgress(title, message, -1);
    }

    public ProgressDialog showProgress(String title, String message, int theme) {
        Log.i(TAG, "showProgress");
        if (mProgressDialog == null) {
            if (theme > 0)
                mProgressDialog = new ProgressDialog(this, theme);
            else
                mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setCanceledOnTouchOutside(false);// 不能取消
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);// 设置进度条是否不明确
        }

        if (!StringUtils.isEmpty(title))
            mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
        return mProgressDialog;
    }

    public void hideProgress() {
        Log.i(TAG, "hideProgress");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

}
