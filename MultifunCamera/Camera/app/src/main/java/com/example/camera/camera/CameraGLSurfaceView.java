package com.example.camera.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CameraGLSurfaceView extends GLSurfaceView {
    private CameraGL20Renderer mRenderer;

    public CameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        mRenderer = new CameraGL20Renderer(this.getContext(),
                CameraConfig.getInstance().getScaleType());

        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void setBitmap(Bitmap bitmap) {
        mRenderer.setBitmap(bitmap);
        requestRender();
    }

    public void onResume() {
        super.onResume();
        requestRender();
    }

    public void onPause() {
        super.onPause();
    }
}
