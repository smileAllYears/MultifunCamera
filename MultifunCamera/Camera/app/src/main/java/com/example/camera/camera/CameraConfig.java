package com.example.camera.camera;


public class CameraConfig {
    private static CameraConfig cameraConfig = null;

    private CameraType cameraType;
    private CameraId cameraId;
    private CameraRotate cameraRotate;
    private ScaleType scaleType = ScaleType.ASPECT_FILL;
    private static boolean isMirror = true;

    public static CameraConfig getInstance() {
        if (cameraConfig == null) {
            synchronized (CameraConfig.class) {
                if (cameraConfig == null) {
                    cameraConfig = new CameraConfig();
                }
            }
        }

        return cameraConfig;
    }

    public static CameraConfig getCameraConfig() {
        return cameraConfig;
    }

    public static CameraConfig setCameraConfig(CameraConfig cameraConfig) {
        CameraConfig.cameraConfig = cameraConfig;
        return CameraConfig.getInstance();
    }

    public CameraType getCameraType() {
        return cameraType;
    }

    public CameraConfig setCameraType(CameraType cameraType) {
        this.cameraType = cameraType;
        return CameraConfig.getInstance();
    }

    public CameraId getCameraId() {
        return cameraId;
    }

    public CameraConfig setCameraId(CameraId cameraId) {
        this.cameraId = cameraId;
        return CameraConfig.getInstance();
    }

    public CameraRotate getCameraRotate() {
        return cameraRotate;
    }

    public CameraConfig setCameraRotate(CameraRotate cameraRotate) {
        this.cameraRotate = cameraRotate;
        return CameraConfig.getInstance();
    }

    public static boolean isMirror() {
        return isMirror;
    }

    public static void setIsMirror(boolean isMirror) {
        CameraConfig.isMirror = isMirror;
    }

    public ScaleType getScaleType() {
        return scaleType;
    }

    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
    }
}
