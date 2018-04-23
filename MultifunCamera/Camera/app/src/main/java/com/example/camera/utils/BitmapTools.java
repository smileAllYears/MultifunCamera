package com.example.camera.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

public class BitmapTools {

    private static final String TAG = "BitmapTools";

    private volatile static BitmapTools bitmapTools = null;

    public BitmapTools getInstance() {
        if (bitmapTools == null) {
            synchronized (BitmapTools.class) {
                if (bitmapTools == null) {
                    bitmapTools = new BitmapTools();
                }
            }
        }
        return bitmapTools;
    }

    private BitmapTools() {

    }

    public static Bitmap scale(Bitmap origin, int outputWidth, int outputHeight) {
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        float wScale = ((float) outputWidth) / width;
        float hScale = ((float) outputHeight) / height;
        matrix.postScale(wScale, hScale);
        try {
            Bitmap bitmap = Bitmap.createBitmap(origin, 0, 0,
                    width, height, matrix, true);
            return bitmap;

        } catch (Exception ex) {
            Log.d(TAG, "scale: " + ex.toString());
            return null;
        }
    }
}
