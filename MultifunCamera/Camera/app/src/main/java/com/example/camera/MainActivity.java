package com.example.camera;

import java.util.concurrent.atomic.AtomicInteger;

import com.example.camera.camera.CameraCallbackPreviewListener;
import com.example.camera.camera.CameraGLSurfaceView;
import com.example.camera.camera.FunctionCamera;
import com.example.camera.utils.BitmapTools;

import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    // Used to load the 'native-lib' library on application startup.

    private FunctionCamera mFunctionCamera;
    private ImageView mImageView;
    private CameraGLSurfaceView mGlSurfaceView;

    private int[] imagePiexls;
    private int[] convertPiexls;

    private AtomicInteger count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        count = new AtomicInteger();

        mImageView = (ImageView) findViewById(R.id.convertImage);

        mGlSurfaceView = (CameraGLSurfaceView) findViewById(R.id.previewSurface);

        mFunctionCamera = new FunctionCamera();

        mFunctionCamera.setCameraCallbackPreviewListener(new CameraCallbackPreviewListener() {
            @Override
            public void onPreviewFrame(final Bitmap bitmap) {
                Log.d(TAG, "onPreviewFrame:调用了");
                mGlSurfaceView.setBitmap(bitmap);
            }
        });

        mFunctionCamera.openCamera();
    }
}
