package com.seu.magicfilter.camera;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;

import com.android.grafika.TextureMovieEncoder;
import com.seu.magicfilter.R;
import com.seu.magicfilter.camera.base.BaseGlSurfaceView;
import com.seu.magicfilter.camera.interfaces.OnErrorListener;
import com.seu.magicfilter.camera.interfaces.OnFocusListener;
import com.seu.magicfilter.camera.interfaces.OnRecordListener;
import com.seu.magicfilter.camera.interfaces.OnSwitchCameraListener;
import com.seu.magicfilter.filter.base.MagicCameraInputFilter;
import com.seu.magicfilter.filter.base.MagicRecordFilter;
import com.seu.magicfilter.utils.OpenGlUtils;
import com.seu.magicfilter.utils.TextureRotationUtil;

import net.ossrs.yasea.DeviceUtils;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * CameraGlSurfaceView
 *
 * @author Created by jz on 2017/5/2 11:21
 */
public class CameraGlSurfaceView extends BaseGlSurfaceView implements GLSurfaceView.Renderer,
        Camera.AutoFocusCallback {

    private static final String TAG = "CameraGlSurfaceView";

    public static int RECORD_WIDTH = 544, RECORD_HEIGHT = 960;

    private final FloatBuffer mRecordCubeBuffer;//顶点坐标
    private final FloatBuffer mRecordTextureBuffer;//纹理坐标

    private MagicCameraInputFilter mCameraInputFilter;//绘制到屏幕上
    private SurfaceTexture mSurfaceTexture;//surface纹理

    private CameraHelper mCameraHelper;

    private int mOrientation;
    private boolean mIsInversion;

    private ThreadHelper mThreadHelper;

    private OnFocusListener mOnFocusListener;
    private OnRecordListener mOnRecordListener;

    //focus
    private int mFocusAreaSize;

    // this is static so it survives activity restarts
    private boolean mRecordingEnabled;
    private int mRecordingStatus;
    private File mOutputFile;
    private static TextureMovieEncoder sVideoEncoder = new TextureMovieEncoder();
    private static final int RECORDING_OFF = 0;
    private static final int RECORDING_ON = 1;
    private static final int RECORDING_RESUMED = 2;
    private static final int RECORDING_PAUSE = 3;
    private static final int RECORDING_NO_FEED = 4;

    private boolean isHightVersion = false;
    private RecordHelper mRecordHelper;

    public CameraGlSurfaceView(Context context) {
        this(context, null);
    }

    public CameraGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mCameraHelper = new CameraHelper();
        mThreadHelper = new ThreadHelper();

        mScaleType = CENTER_CROP;

        mRecordCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mRecordTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mFocusAreaSize = getResources().getDimensionPixelSize(R.dimen.camera_focus_area_size);

        if (Build.VERSION.SDK_INT >= 18) {
            isHightVersion = true;
            RECORD_WIDTH = 720;
            RECORD_HEIGHT = 1280;
        }
    }

    private OrientationEventListener orientationEventListener = new OrientationEventListener(getContext()) {
        @Override
        public void onOrientationChanged(int orientation) {

            //手机平放时，检测不到有效的角度
            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;
            }

            //只检测是否有四个角度的改变
            if (orientation > 350 || orientation < 10) { //0度
                mOrientation = 0;
            } else if (orientation > 80 && orientation < 100) { //90度
                mOrientation = 90;
            } else if (orientation > 170 && orientation < 190) { //180度
                mOrientation = 180;
            } else if (orientation > 260 && orientation < 280) { //270度
                mOrientation = 270;
            }

            Log.i(TAG, "onOrientationChanged=" + mOrientation);

        }
    };

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        super.onSurfaceCreated(gl, config);

        if (mCameraInputFilter == null) {
            mCameraInputFilter = new MagicCameraInputFilter();
            mCameraInputFilter.init(getContext());
        }

        if (mTextureId == OpenGlUtils.NO_TEXTURE) {
            mTextureId = OpenGlUtils.getExternalOESTextureID();
            if (mTextureId != OpenGlUtils.NO_TEXTURE) {
                mSurfaceTexture = new SurfaceTexture(mTextureId);
                mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        requestRender();
                    }
                });
            }
        }
        mCameraHelper.startPreview(mSurfaceTexture);

        mRecordingEnabled = sVideoEncoder.isRecording();
        Log.i(TAG, "recording=" + mRecordingEnabled);
        if (mRecordingEnabled) {
            mRecordingStatus = RECORDING_RESUMED;
        } else {
            mRecordingStatus = RECORDING_OFF;
        }
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged width=" + width + " height=" + height);
        super.onSurfaceChanged(gl, width, height);

        CameraHelper.CameraItem info = mCameraHelper.getCameraAngleInfo();
        adjustSize(info.orientation, info.isFront, !info.isFront);

        //重新计算录制顶点、纹理坐标
        float[][] data = adjustSize(mRecordWidth, mRecordHeight, info.orientation,
                info.isFront, !info.isFront);
        mRecordCubeBuffer.clear();
        mRecordCubeBuffer.put(data[0]).position(0);
        mRecordTextureBuffer.clear();
        mRecordTextureBuffer.put(data[1]).position(0);
    }

    long currentTime = System.currentTimeMillis();

    @Override
    public void onDrawFrame(GL10 gl) {
        //Log.i(TAG, "onDrawFrame=" + (System.currentTimeMillis() - currentTime));
        currentTime = System.currentTimeMillis();
        super.onDrawFrame(gl);
        if (mSurfaceTexture == null)
            return;
        mSurfaceTexture.updateTexImage();

        float[] mtx = new float[16];
        mSurfaceTexture.getTransformMatrix(mtx);
        mFilter.setTextureTransformMatrix(mtx);

        // If the recording state is changing, take care of it here.  Ideally we wouldn't
        // be doing all this in onDrawFrame(), but the EGLContext sharing with GLSurfaceView
        // makes it hard to do elsewhere.

        int id = 0;
        if (isHightVersion) {
            //先将纹理绘制到fbo同时过滤镜
            id = mFilter.onDrawToTexture(mTextureId);
            //绘制到屏幕上
            mCameraInputFilter.onDrawFrame(id, mGLCubeBuffer, mGLTextureBuffer);
        } else {
            mFilter.onDrawFrameAndReadPix(mTextureId, mRecordCubeBuffer, mRecordTextureBuffer);
        }

        if (mRecordingEnabled) {
            switch (mRecordingStatus) {
                case RECORDING_OFF:
                    Log.i(TAG, "START recording");
                    CameraHelper.CameraItem info = mCameraHelper.getCameraAngleInfo();
                    Log.i(TAG, "rotation=" + info.orientation);
                    // start recording
                    if (isHightVersion) {
                        float[] textureArray = new float[TextureRotationUtil.TEXTURE_NO_ROTATION.length];
                        mGLTextureBuffer.get(textureArray);
                        sVideoEncoder.startRecording(new TextureMovieEncoder.EncoderConfig(
                                mOutputFile, RECORD_WIDTH, RECORD_HEIGHT, 2048000, EGL14.eglGetCurrentContext(),
                                textureArray, mOrientation));
                        if (mOnRecordListener != null)
                            mOnRecordListener.onRecordStarted();
                    } else {
                        mRecordHelper = new RecordHelper();
                        mRecordHelper.setOnRecordListener(mOnRecordListener);
                        mRecordHelper.start(RECORD_WIDTH, RECORD_HEIGHT, mOrientation, mOutputFile);
                        mRecordHelper.setPreview(mPreviewWidth, mPreviewHeight);
                    }
                    mRecordingStatus = RECORDING_ON;
                    break;

                case RECORDING_ON:
                    // yay
                    if (isHightVersion) {
                        //Log.i(TAG, "onDrawToFbo=" + (System.currentTimeMillis() - currentTime));
                        currentTime = System.currentTimeMillis();
                        // TODO: be less lame.
                        sVideoEncoder.setTextureId(id);
                        // Tell the video encoder thread that a new frame is available.
                        // This will be ignored if we're not actually recording.
                        sVideoEncoder.frameAvailable(mSurfaceTexture);

                    } else {
                        if (mRecordHelper != null) {
                            mRecordHelper.onRecord(mFilter.getGLFboBuffer());
                        }
                    }
                    break;

                case RECORDING_PAUSE:
                    if (isHightVersion) {
                        sVideoEncoder.pauseRecording();
                        if (mOnRecordListener != null)
                            mOnRecordListener.onRecordPause();
                    } else {
                        if (mRecordHelper != null)
                            mRecordHelper.pause();
                    }
                    mRecordingStatus = RECORDING_NO_FEED;
                    break;

                case RECORDING_RESUMED:
                    Log.i(TAG, "RESUME recording");
                    if (isHightVersion) {
                        sVideoEncoder.resumeRecording();
                        if (mOnRecordListener != null)
                            mOnRecordListener.onRecordResume();
                    } else {
                        if (mRecordHelper != null)
                            mRecordHelper.resume();
                    }
                    //sVideoEncoder.updateSharedContext(EGL14.eglGetCurrentContext());
                    mRecordingStatus = RECORDING_ON;
                    break;

                case RECORDING_NO_FEED:
                    Log.i(TAG, "RECORDING_NO_FEED");
                    break;

                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        } else {
            switch (mRecordingStatus) {
                case RECORDING_ON:
                case RECORDING_RESUMED:
                case RECORDING_NO_FEED:
                    // stop recording
                    Log.i(TAG, "STOP recording");
                    if (isHightVersion) {
                        sVideoEncoder.resumeRecording();
                        sVideoEncoder.stopRecording();
                        if (mOnRecordListener != null)
                            mOnRecordListener.onRecordFinished(mOutputFile.getAbsolutePath());
                    } else {
                        mRecordHelper.stop();
                    }
                    mRecordingStatus = RECORDING_OFF;
                    break;
                case RECORDING_OFF:
                    // yay
                    break;
                default:
                    throw new RuntimeException("unknown status " + mRecordingStatus);
            }
        }

        // Set the video encoder's texture name.  We only need to do this once, but in the
        // current implementation it has to happen after the video encoder is started, so
        // we just do it here.

    }

    @Override
    protected void onFilterChanged() {
        super.onFilterChanged();
        mCameraInputFilter.onInputSizeChanged(mPreviewWidth, mPreviewHeight);
        mCameraInputFilter.onDisplaySizeChanged(mSurfaceWidth, mSurfaceHeight);
    }

   /* @Override
    public void onSensor(int orientation, boolean isInversion) {
        Log.i(TAG, "onSensor: orientation=" + orientation + " isInversion=" + isInversion);
        mOrientation = orientation;
        mIsInversion = isInversion;
    }*/

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (mOnFocusListener != null)
            mOnFocusListener.onFocusEnd();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*if (event.getAction() == MotionEvent.ACTION_DOWN && mSurfaceWidth > 0 && mSurfaceHeight > 0) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (mOnFocusListener != null)
                mOnFocusListener.onFocusStart(x, y);
            int centerX = (x - mSurfaceWidth / 2) * 1000 / (mSurfaceWidth / 2);
            int centerY = (y - mSurfaceHeight / 2) * 1000 / (mSurfaceHeight / 2);
            mCameraHelper.selectCameraFocus(new Rect(centerX - 100, centerY - 100, centerX + 100, centerY + 100), this);
        }*/

        if (event.getAction() == MotionEvent.ACTION_DOWN && mSurfaceWidth > 0 && mSurfaceHeight > 0) {
            Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);
            Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f);
            mCameraHelper.selectCameraFocus(focusRect, meteringRect, this);
        }
        return true;
    }

    /**
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to 1000:1000.
     * <p>
     * Rotate, scale and translate touch rectangle using matrix configured in
     */
    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(mFocusAreaSize * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, mSurfaceWidth - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, mSurfaceHeight - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        //matrix.mapRect(rectF);

        Log.i(TAG, "rect left=" + rectF.left + " top=" + rectF.top + " right=" + rectF.right + " bottom=" + rectF
                .bottom);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF
                .bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    //调整view大小
    private void review() {
        mPreviewWidth = mCameraHelper.getPreviewWidth();
        mPreviewHeight = mCameraHelper.getPreviewHeight();
        mRecordWidth = mCameraHelper.getRecordWidth();
        mRecordHeight = mCameraHelper.getRecordHeight();
    }

    /**
     * 开始录制
     */
    public void startRecord(String filePath) {
        //mRecordFilter.startRecord(filePath);
        mOutputFile = new File(filePath);
        mRecordingEnabled = true;
    }

    /**
     * 停止录制
     */
    public void stopRecord() {
        mRecordingEnabled = false;
    }


    public void resumeRecord() {
        mRecordingStatus = RECORDING_RESUMED;
    }

    public void pauseRecord() {
        mRecordingStatus = RECORDING_PAUSE;
    }


    public boolean isRecording() {
        return mRecordingEnabled && mRecordingStatus == RECORDING_ON;
    }

    /**
     * 恢复摄像头，对应Activity生命周期
     */
    public void resume() {
        Log.i(TAG, "resume");
        boolean rel = mCameraHelper.openCamera();
        if (rel) {
            review();
            if (mSurfaceTexture != null)
                mCameraHelper.startPreview();
        } else {
            mThreadHelper.sendError(getResources().getString(R.string.record_open_camera_failed));
        }

        if (orientationEventListener != null)
            orientationEventListener.enable();
    }

    /**
     * 暂停摄像头，对应Activity生命周期
     */
    public void pause() {
        Log.i(TAG, "pause");
        mCameraHelper.stopCamera();

        if (orientationEventListener != null)
            orientationEventListener.disable();
    }

    /**
     * 停止摄像头，对应Activity的onDestroy
     */
    public void stop() {
        mCameraHelper.stopCamera();

        mFilter.destroy();
        mCameraInputFilter.destroy();
    }

    /**
     * 切换前后摄像头
     */
    public void switchCamera(OnSwitchCameraListener l) {
        mThreadHelper.setOnSwitchCameraListener(l);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean rel = mCameraHelper.switchCamera();
                mThreadHelper.sendSwitchCamera(rel, getResources().getString(R.string.record_switch_camera_failed));
            }
        }).start();
    }

    /**
     * 获得摄像头
     */
    public CameraHelper getCamera() {
        return mCameraHelper;
    }

    /**
     * 获得摄像头数量
     */
    public int getCameraCount() {
        return Camera.getNumberOfCameras();
    }

    /**
     * 是否横屏
     */
    public int getOrientation() {
        return mOrientation;
    }

    /**
     * 是否倒置
     */
    public boolean isInversion() {
        return mIsInversion;
    }

    /**
     * 当前是否前置摄像头
     */
    public boolean isFrontCamera() {
        return mCameraHelper.isFrontCamera();
    }

    public boolean toggleFlashMode() {
        mCameraHelper.switchFlash();
        return true;
    }

    /**
     * 返回录制宽度
     */
    public int getRecordWidth() {
        return mRecordWidth;
    }

    /**
     * 返回录制高度
     */
    public int getRecordHeight() {
        return mRecordHeight;
    }

    /**
     * 设置浏览回调
     *
     * @param l 回调
     */
    public void setOnRecordListener(OnRecordListener l) {
        this.mOnRecordListener = l;
    }

    /**
     * 设置摄像头焦点回调
     *
     * @param l 回调
     */
    public void setOnFocusListener(OnFocusListener l) {
        this.mOnFocusListener = l;
    }


    /**
     * 设置错误回调
     *
     * @param l 回调
     */
    public void setOnErrorListener(OnErrorListener l) {
        mThreadHelper.setOnErrorListener(l);
    }
}
