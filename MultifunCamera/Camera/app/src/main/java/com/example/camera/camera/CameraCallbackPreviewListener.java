package com.example.camera.camera;

import android.graphics.Bitmap;

public interface CameraCallbackPreviewListener {
    void onPreviewFrame(Bitmap bitmap);
}
