package com.example.camera.camera;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

public class CameraGL20Renderer implements GLSurfaceView.Renderer {
    private static final String TAG = CameraGL20Renderer.class.getSimpleName();

    private Bitmap mBitmap;

    private int mBitmapWidth;
    private int mBitmapHeight;

    private int mGlWidth;
    private int mGlHeight;

    private boolean mRebuildAspect = false;
    private ScaleType mScaleType = ScaleType.ASPECT_FILL;

    private Context mContext;
    private int mProgram;
    private String vertex;
    private String fragment;

    private int glHPosition;
    private int glHTexture;
    private int glHCoordinate;
    private int glHMatrix;

    private FloatBuffer bPos;
    private FloatBuffer bCoord;
    private FloatBuffer mirrorCoord;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    private final float[] sPos = {
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f
    };

    private final float[] sCoord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    private static final float[] sMirrorCoords = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f
    };

    public CameraGL20Renderer(Context context, ScaleType scaleType) {
        mContext = context;

        this.fragment = "precision mediump float;"
                + "uniform sampler2D vTexture;"
                + "uniform int vIsHalf;"
                + "varying vec2 aCoordinate;"
                + "void main(){"
                + "gl_FragColor=texture2D(vTexture,aCoordinate);"
                + "}";

        this.vertex = "attribute vec4 vPosition;"
                + "attribute vec2 vCoordinate;"
                + "uniform mat4 vMatrix;"
                + "varying vec2 aCoordinate;"
                + "void main(){"
                + "gl_Position=vMatrix*vPosition;"
                + "aCoordinate=vCoordinate;"
                + "}";

        ByteBuffer bb = ByteBuffer.allocateDirect(sPos.length * 4);
        bb.order(ByteOrder.nativeOrder());
        bPos = bb.asFloatBuffer();
        bPos.put(sPos);
        bPos.position(0);

        ByteBuffer cc0 = ByteBuffer.allocateDirect(sCoord.length * 4);
        cc0.order(ByteOrder.nativeOrder());
        bCoord = cc0.asFloatBuffer();
        bCoord.put(sCoord);
        bCoord.position(0);

        ByteBuffer cc1 = ByteBuffer.allocateDirect(sMirrorCoords.length * 4);
        cc1.order(ByteOrder.nativeOrder());
        mirrorCoord = cc1.asFloatBuffer();
        mirrorCoord.put(sMirrorCoords);
        mirrorCoord.position(0);

        mScaleType = scaleType;
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        mProgram = createProgram(vertex, fragment);
        glHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        glHCoordinate = GLES20.glGetAttribLocation(mProgram, "vCoordinate");
        glHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
        glHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");

        createTexture();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mGlWidth = width;
        mGlHeight = height;

        mRebuildAspect = true;
    }

    private void changeAspect(Bitmap bitmap) {
        if (bitmap.getWidth() != mBitmapWidth || bitmap.getHeight() != mBitmapHeight) {
            mBitmapWidth = bitmap.getWidth();
            mBitmapHeight = bitmap.getHeight();
            mRebuildAspect = true;
        }

        if (!mRebuildAspect) {
            return;
        }

        if (mBitmapWidth < 0 || mBitmapHeight < 0 || mGlWidth < 0 || mGlHeight < 0) {
            return;
        }

        float bRadio = (float) mBitmapWidth / mBitmapHeight;
        float gRadio = (float) mGlWidth / mGlHeight;

        if (mScaleType.equals(ScaleType.ASPECT_FILL)) {
            aspectFill(bRadio, gRadio);
        } else {
            aspectFit(bRadio, gRadio);
        }

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 5.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

        mRebuildAspect = false;
    }

    private void aspectFit(float bRadio, float gRadio) {
        if (mGlWidth > mGlHeight) {
            if (bRadio > gRadio) {
                Matrix.orthoM(mProjectMatrix, 0,
                        -1, 1, -bRadio / gRadio, bRadio / gRadio,
                        3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0,
                        -gRadio / bRadio, gRadio / bRadio, -1, 1,
                        3, 5);
            }
        } else {
            if (bRadio > gRadio) {
                Matrix.orthoM(mProjectMatrix, 0,
                        -1, 1, -bRadio / gRadio, bRadio / gRadio,
                        3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0,
                        -gRadio / bRadio, gRadio / bRadio, -1, 1,
                        3, 5);
            }
        }
    }

    private void aspectFill(float bRadio, float gRadio) {
        if (mGlWidth > mGlHeight) {
            if (bRadio > gRadio) {
                Matrix.orthoM(mProjectMatrix, 0,
                        -gRadio / bRadio, gRadio / bRadio, -1, 1,
                        3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0,
                        -1, 1, -bRadio / gRadio, bRadio / gRadio,
                        3, 5);
            }
        } else {
            if (bRadio > gRadio) {
                Matrix.orthoM(mProjectMatrix, 0,
                        -gRadio / bRadio, gRadio / bRadio, -1, 1,
                        3, 5);
            } else {
                Matrix.orthoM(mProjectMatrix, 0,
                        -1, 1, -bRadio / gRadio, bRadio / gRadio,
                        3, 5);
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mBitmap == null) {
            return;
        }

        try {
            Bitmap bitmap = mBitmap;
            changeAspect(bitmap);
            onDraw(bitmap);
        } finally {
        }
    }

    private void onDraw(Bitmap bitmap) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glUseProgram(mProgram);
        GLES20.glUniformMatrix4fv(glHMatrix, 1, false, mMVPMatrix, 0);
        GLES20.glEnableVertexAttribArray(glHPosition);
        GLES20.glEnableVertexAttribArray(glHCoordinate);
        GLES20.glUniform1i(glHTexture, 0);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glVertexAttribPointer(glHPosition, 2, GLES20.GL_FLOAT, false, 0, bPos);
        if (CameraConfig.getInstance().isMirror()) {
            GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, mirrorCoord);
        } else {
            GLES20.glVertexAttribPointer(glHCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoord);
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisable(GLES20.GL_TEXTURE_2D);
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertex = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertex == 0) {
            return 0;
        }

        int fragment = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (fragment == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertex);
            GLES20.glAttachShader(program, fragment);
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program:" + GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }

        return program;
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);

        if (0 != shader) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader:" + shaderType);
                Log.e(TAG, "GLES20 Error:" + GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }

        return shader;
    }

    private void createTexture() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
    }
}
