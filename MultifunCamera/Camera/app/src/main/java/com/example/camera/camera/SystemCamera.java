package com.example.camera.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

public class SystemCamera {
    private static final String TAG = SystemCamera.class.getSimpleName();

    private static final int MAGIC_TEXTURE_ID = 10;

    private Camera mCamera;
    private SurfaceTexture mSurfaceTexture;
    private YUVDecoder mYUVDecoder;

    private Camera.Parameters mParameters;
    private CameraCallback mCallback;

    private int mCameraId = 0;
    private int mRotate = 0;
    private int maxPreviewSize = 720;

    public void setCallback(CameraCallback callback) {
        mCallback = callback;
    }

    public void setMaxPreviewSize(int maxSize) {
        maxPreviewSize = maxSize;
    }

    public boolean openCamera(int cameraId, int rotate) {
        mCameraId = cameraId;
        mRotate = rotate;

        try {
            if (mCamera != null) {
                Log.e(TAG, "camera has running");
                return false;
            }

            mCamera = Camera.open(mCameraId);
        } catch (Exception e) {
            Log.e(TAG, "camera not available, detail : " + e.toString());
            return false;
        }

        mParameters = mCamera.getParameters();
        List<Camera.Size> sizes = mParameters.getSupportedPreviewSizes();
        CameraSize cameraSize = getSuitableCameraSize(sizes);
        if (cameraSize == null) {
            return false;
        }

        Log.e(TAG, "previewSize : width:" + cameraSize.width + ", height:" + cameraSize.height);
        mYUVDecoder = new YUVDecoder(cameraSize.width, cameraSize.height, mRotate);
        mYUVDecoder.setCallback(mDecoderCallback);

        setSuitableFocusMode(true);
        mParameters.setPreviewSize(cameraSize.width, cameraSize.height);
        mCamera.setParameters(mParameters);

        return true;
    }

    public boolean startPreview() {
        try {
            if (mCamera == null) {
                return false;
            }

            mSurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (Exception e) {
            Log.e(TAG, "setPreviewTexture error, detail : " + e.toString());
            return false;
        }

        mCamera.setPreviewCallback(mPreviewCallback);
        mCamera.startPreview();

        startMonitor();

        return true;
    }

    public boolean stopPreview() {
        stopMonitor();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
        }
        mSurfaceTexture = null;
//        try {
//            mCamera.setPreviewTexture(null);
//
//        } catch (IOException e) {
//            Log.e(TAG, "setPreviewTexture error, detail : " + e.toString());
//            return false;
//        }
        return true;
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        if (mYUVDecoder != null) {
            mYUVDecoder.release();
        }
    }

    private void startMonitor() {
        stopMonitor();
        mMonitorTimer = new Timer();
        mMonitorTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    onMonitor();
                } catch (Throwable ex) {
                    // pass through
                }
            }
        };
        mMonitorTimer.schedule(mMonitorTask, 10000, 6000);
    }

    private void stopMonitor() {
        if (mMonitorTimer != null) {
            mMonitorTask.cancel();
            mMonitorTask = null;
            mMonitorTimer.cancel();
            mMonitorTimer = null;
        }
    }

    private Timer mMonitorTimer;
    private TimerTask mMonitorTask;

    private void onMonitor() {
        double videoFps = getPreviewFps();
        double decodeFps = getPushFps();
        onState(true, videoFps, decodeFps);
    }

    private void onState(boolean result, double videoFps, double decodeFps) {
        if (mCallback != null) {
            mCallback.onState(result, videoFps, decodeFps);
        }
    }

    private double getPreviewFps() {
        return FpsUtils.getPreviewFps();
    }

    private double getPushFps() {
        return FpsUtils.getPushFps();
    }

    private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, final Camera camera) {
            if (mYUVDecoder != null) {
                FpsUtils.previewFps();
                mYUVDecoder.addData(data);
            }
        }
    };

    private YUVDecoder.DecoderCallback mDecoderCallback = new YUVDecoder.DecoderCallback() {
        @Override
        public void onDataDecoded(Bitmap bitmap) {
            if (bitmap == null || bitmap.isRecycled()) {
                return;
            }

            if (mYUVDecoder != null) {
                FpsUtils.pushFps();
                if (mCallback != null) {
                    mCallback.onPreviewFrame(bitmap);
                }
            }
        }
    };

    private CameraSize getSuitableCameraSize(List<Camera.Size> sizes) {
        if (sizes == null || sizes.size() <= 0) {
            Log.e(TAG, "getSuitableCameraSize, no supported camera size");
            return null;
        }

        ArrayList<CameraSize> sizeList = new ArrayList();
        for (Camera.Size size : sizes) {
            Log.e(TAG, "supportedSize: " + size.width + "x" + size.height);
            sizeList.add(new CameraSize(size.width, size.height));
        }
        Collections.sort(sizeList);

        Log.e(TAG, "================================================");

        for (CameraSize size : sizeList) {
            Log.e(TAG, "sortedSize: " + size.width + "x" + size.height);
        }

        Log.e(TAG, "================================================");

        for (CameraSize size : sizeList) {
            if (size.getHeight() > maxPreviewSize) {
                continue;
            }
            return size;
        }

        return sizeList.get(0);
    }

    private void setSuitableFocusMode(boolean autoFocus) {
        final List<String> modes = mParameters.getSupportedFocusModes();
        if (modes == null && modes.size() <= 0) {
            Log.e(TAG, "getSupportedFocusMode, no supported focus mode");
            return;
        }

        if (autoFocus && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        } else if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        } else {
            mParameters.setFocusMode(modes.get(0));
        }
    }

    private class CameraSize implements Comparable<CameraSize> {
        private int width;
        private int height;

        public CameraSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }

        @Override
        public int compareTo(CameraSize o) {
            if (this.height > o.getHeight()) {
                return -1;
            } else if (this.height < o.getHeight()) {
                return 1;
            } else {
                if (this.width > o.getWidth()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }
}
