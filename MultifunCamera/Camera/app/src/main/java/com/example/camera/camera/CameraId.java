package com.example.camera.camera;

public enum CameraId {
    FRONT(1), REAR(0);
    private int cameraId;
    CameraId(int cameraId){
        this.cameraId = cameraId;
    }

    public int getValue() {
        return this.cameraId;
    }
}
