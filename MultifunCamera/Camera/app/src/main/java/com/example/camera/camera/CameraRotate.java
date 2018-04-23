package com.example.camera.camera;

public enum CameraRotate {
    ROTATE0(0), ROTATE90(90), ROTATE180(180), ROTATE270(270);

    private int rotate;

    CameraRotate(int rotate) {
        this.rotate = rotate;
    }

    public int getValue() {
        return this.rotate;
    }
}
