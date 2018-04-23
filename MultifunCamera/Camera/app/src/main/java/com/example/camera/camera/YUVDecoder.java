package com.example.camera.camera;

import android.graphics.Bitmap;

public class YUVDecoder {
    private static final String TAG = YUVDecoder.class.getSimpleName();

    static {
        System.loadLibrary("Codec");
    }

    private Bitmap mBitmap = null;
    private DecoderCallback mCallback = null;

    private int mWidth = 0;
    private int mHeight = 0;
    private int mRotate = 0;

    private boolean mRunning = false;
    private Object mLock = new Object();
    private byte[] mData = null;

    public interface DecoderCallback {
        void onDataDecoded(Bitmap bitmap);
    }

    public YUVDecoder(int width, int height, int rotate) {
        mWidth = width;
        mHeight = height;
        mRotate = rotate;

        nativeInit(width, height, rotate);
        mRunning = true;
        new Thread(mDecodeTask).start();
    }

    public void setCallback(DecoderCallback callback) {
        mCallback = callback;
    }

    public void addData(byte[] data) {
        if (data == null || data.length <= 0) {
            return;
        }

        synchronized (mLock) {
            if (mData == null) {
                mData = data;
                mLock.notify();
            }
        }
    }

    public void release() {
        mRunning = false;
        nativeRelease();
        if (mBitmap == null) {
            return;
        }

        if (!mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

    private Runnable mDecodeTask = new Runnable() {
        @Override
        public void run() {
            while (mRunning) {
                synchronized (mLock) {
                    if (mData == null) {
                        try {
                            mLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    switch (mRotate) {
                        case 0:
                            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                            break;
                        case 90:
                            mBitmap = Bitmap.createBitmap(mHeight, mWidth, Bitmap.Config.ARGB_8888);
                            break;
                        case 180:
                            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                            break;
                        case 270:
                            mBitmap = Bitmap.createBitmap(mHeight, mWidth, Bitmap.Config.ARGB_8888);
                            break;
                        default:
                            mBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
                            break;
                    }

                    nativeNV12ToAgb(mData, mBitmap);

                    mCallback.onDataDecoded(mBitmap);
                    mData = null;
                }
            }
        }
    };

    private native void nativeInit(int width, int height, int rotate);

    private native void nativeNV12ToAgb(byte[] yuvArray, Bitmap bitmap);

    private native void nativeRelease();
}
