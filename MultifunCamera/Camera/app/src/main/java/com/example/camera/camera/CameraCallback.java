package com.example.camera.camera;

import android.graphics.Bitmap;

public interface CameraCallback {
    void onPreviewFrame(Bitmap data);

    void onState(boolean result, double videoFps, double decodeFps);
}
