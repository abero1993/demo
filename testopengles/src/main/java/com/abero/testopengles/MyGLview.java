package com.abero.testopengles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by abero on 2018/2/7.
 */

public class MyGLview extends GLSurfaceView implements GLSurfaceView.Renderer {

    private static final String TAG = "MyGLview";

    private static final String VERTEX_SHADER =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    " gl_Position = uMVPMatrix * vPosition;" +
                    " v_texCoord = a_texCoord;" +
                    "}";
    private static final String FRAGMENT_SHADER =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    " gl_FragColor = texture2D(s_texture, v_texCoord);" +
                    "}";

    private static final float[] VERTEX = {   // in counterclockwise order:
            1, 1, 0,   // top right
            -1, 1, 0,  // top left
            -1, -1, 0, // bottom left
            1, -1, 0,  // bottom right
    };
    private static final short[] VERTEX_INDEX = {
            0, 1, 2, 0, 2, 3
    };
    private static final float[] TEX_VERTEX = {   // in clockwise order:
            1f, 0,  // bottom right
            0.5f, 0.5f,  // bottom left
            0, 1f,  // top left
            1f, 1f,  // top right
    };

    private Context mContext;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTexVertexBuffer;
    private ShortBuffer mVertexIndexBuffer;
    private final float[] mMVPMatrix = new float[16];

    private int mProgram;
    private int mPositionHandle;
    private int mMatrixHandle;
    private int mTexCoordHandle;
    private int mTexSamplerHandle;
    private int mTexName;

    public MyGLview(Context context) {
        super(context);
        init(context);
    }

    public MyGLview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexBuffer.position(0);

        mVertexIndexBuffer = ByteBuffer.allocateDirect(VERTEX_INDEX.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_INDEX);
        mVertexIndexBuffer.position(0);

        mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_VERTEX);
        mTexVertexBuffer.position(0);

        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.i(TAG, "onSurfaceCreated:");

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        mProgram = GLES20.glCreateProgram();
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
         mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                12, mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0,
                mTexVertexBuffer);

        int[] texNames = new int[1];
        GLES20.glGenTextures(1, texNames, 0);
        mTexName = texNames[0];
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.db);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        Log.i(TAG, "onSurfaceChanged w="+width+" h="+height);

        GLES20.glViewport(0, 0, width, height);

        Matrix.perspectiveM(mMVPMatrix, 0, 45, (float) width / height, 0.1f, 100f);
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -5f);

        String s="";
        for(float x:mMVPMatrix)
        {
          s=s+" "+x;
        }

        Log.i(TAG, "m4= "+s);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        Log.i(TAG, "onDrawFrame");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniform1i(mTexSamplerHandle, 0);

        // 用 glDrawElements 来绘制，mVertexIndexBuffer 指定了顶点绘制顺序
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.length,
                GLES20.GL_UNSIGNED_SHORT, mVertexIndexBuffer);
    }
}
