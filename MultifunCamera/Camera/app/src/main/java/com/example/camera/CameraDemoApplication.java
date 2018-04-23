package com.example.camera;

import com.example.camera.camera.CameraConfig;
import com.example.camera.camera.CameraId;
import com.example.camera.camera.CameraRotate;
import com.example.camera.camera.CameraType;

import android.app.Application;

public class CameraDemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CameraConfig.getInstance().setCameraType(CameraType.Android)
                .setCameraId(CameraId.REAR)
                .setCameraRotate(CameraRotate.ROTATE270);
    }
}
