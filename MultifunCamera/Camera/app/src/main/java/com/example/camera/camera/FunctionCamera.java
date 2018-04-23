package com.example.camera.camera;

import android.graphics.Bitmap;

public class FunctionCamera implements CameraCallback {

    private SystemCamera systemCamera;
    private CameraCallbackPreviewListener cameraCallbackPreviewListener;

    public FunctionCamera() {

    }

    public CameraCallbackPreviewListener getCameraCallbackPreviewListener() {
        return cameraCallbackPreviewListener;
    }

    public void setCameraCallbackPreviewListener(
            CameraCallbackPreviewListener cameraCallbackPreviewListener) {
        this.cameraCallbackPreviewListener = cameraCallbackPreviewListener;
    }

    public synchronized void openCamera() {
        CameraType cameraType = CameraConfig.getInstance().getCameraType();
        CameraId cameraId = CameraConfig.getInstance().getCameraId();
        CameraRotate cameraRotate = CameraConfig.getInstance().getCameraRotate();

        switch (cameraType) {
            case Android:
                systemCamera = new SystemCamera();
                systemCamera.openCamera(cameraId.getValue(), cameraRotate.getValue());
                startPreview();
                break;
            case USB:
                break;
            case UVC:
                break;
            case NetworkCamera:
                break;
        }
    }
    public synchronized void closeCamera() {
        CameraType cameraType = CameraConfig.getInstance().getCameraType();

        if (cameraType.equals(CameraType.Android)) {
            stopPreview();
            systemCamera.closeCamera();
        }
    }

    public synchronized void reopenCamera() {
        closeCamera();
        openCamera();
    }


    private synchronized void startPreview() {
        CameraType cameraType = CameraConfig.getInstance().getCameraType();

        if (cameraType.equals(CameraType.Android)) {
            systemCamera.setCallback(this);
            systemCamera.startPreview();
        }
    }

    private synchronized void stopPreview() {
        CameraType cameraType = CameraConfig.getInstance().getCameraType();

        if (cameraType.equals(CameraType.Android)) {
            systemCamera.stopPreview();
            systemCamera.setCallback(null);
        }
    }

    @Override
    public void onPreviewFrame(Bitmap data) {
        if (cameraCallbackPreviewListener != null) {
            cameraCallbackPreviewListener.onPreviewFrame(data);
        }
    }

    @Override
    public void onState(boolean result, double videoFps, double decodeFps) {

    }
}
