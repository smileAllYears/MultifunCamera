package com.example.camera.camera;

public class FpsUtils {
    private static final int MAX_COUNT = 20;

    private static double sPushFps = 0;
    private static long sPushStartTime = 0;
    private static int sPushCount = 0;

    private static double sPreviewFps = 0;
    private static long sPreviewStartTime = 0;
    private static int sPreviewCount = 0;

    public static void previewFps() {
        if (sPreviewCount < 0) {
            sPreviewStartTime = System.currentTimeMillis();
            sPreviewCount = 0;
            return;
        }

        if (sPreviewCount > MAX_COUNT) {
            long time = System.currentTimeMillis() - sPreviewStartTime;
            sPreviewFps = (double) sPreviewCount * 1000 / time;
            sPreviewCount = -1;

            return;
        }

        sPreviewCount++;
    }

    public static void pushFps() {
        if (sPushCount < 0) {
            sPushStartTime = System.currentTimeMillis();
            sPushCount = 0;
            return;
        }

        if (sPushCount > MAX_COUNT) {
            long time = System.currentTimeMillis() - sPushStartTime;
            sPushFps = (double) sPushCount * 1000 / time;
            sPushCount = -1;
            return;
        }

        sPushCount++;
    }

    public static double getPushFps() {
        return sPushFps;
    }

    public static double getPreviewFps() {
        return sPreviewFps;
    }
}
