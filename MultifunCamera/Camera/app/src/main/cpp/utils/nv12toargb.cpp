/**
 * libyuv introduction
 * https://chromium.googlesource.com/libyuv/libyuv/+/master/docs/rotation.md
 */

#define __STDC_CONSTANT_MACROS

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>
#include <android/bitmap.h>
#include <utils/logger.h>
#include <string.h>
#include <include/libyuv.h>

char *g_dst_i420_y = NULL;
char *g_dst_i420_u = NULL;
char *g_dst_i420_v = NULL;

char *g_dst_argb = NULL;

int g_width = 0;
int g_height = 0;
int g_rotate = 0;


JNIEXPORT void JNICALL
Java_com_example_camera_camera_YUVDecoder_nativeInit(JNIEnv *env,
                                         jclass clazz,
                                         jint width,
                                         jint height,
                                         jint rotate) {
    g_width = width;
    g_height = height;
    g_rotate = rotate;

    g_dst_i420_y = (char *) malloc(width * height);
    g_dst_i420_u = (char *) malloc(width * height / 2);
    g_dst_i420_v = (char *) malloc(width * height / 2);
}


JNIEXPORT void JNICALL
Java_com_example_camera_camera_YUVDecoder_nativeRelease(JNIEnv *env, jclass clazz) {
    free(g_dst_i420_y);
    free(g_dst_i420_u);
    free(g_dst_i420_v);

    g_dst_i420_y = NULL;
    g_dst_i420_u = NULL;
    g_dst_i420_v = NULL;
}

void rotate_and_convert(const uint8 *src_nv21, uint8 *dst_argb, libyuv::RotationMode mode) {
    if (mode == libyuv::RotationMode::kRotate0
        || mode == libyuv::RotationMode::kRotate180) {
        libyuv::NV12ToI420Rotate((const uint8 *) src_nv21, g_width,
                                 (const uint8 *) src_nv21 + g_width * g_height, g_width,
                                 (uint8 *) g_dst_i420_y, g_width,
                                 (uint8 *) g_dst_i420_u, g_width / 2,
                                 (uint8 *) g_dst_i420_v, g_width / 2,
                                 g_width,
                                 g_height,
                                 mode);

        libyuv::I420ToARGB((const uint8 *) g_dst_i420_y, g_width,
                           (const uint8 *) g_dst_i420_u, g_width / 2,
                           (const uint8 *) g_dst_i420_v, g_width / 2,
                           (uint8 *) dst_argb, g_width * 4,
                           g_width,
                           g_height);
    } else if (mode == libyuv::RotationMode::kRotate90 ||
               mode == libyuv::RotationMode::kRotate270) {
        libyuv::NV12ToI420Rotate((const uint8 *) src_nv21, g_width,
                                 (const uint8 *) src_nv21 + g_width * g_height, g_width,
                                 (uint8 *) g_dst_i420_y, g_height,
                                 (uint8 *) g_dst_i420_u, g_height / 2,
                                 (uint8 *) g_dst_i420_v, g_height / 2,
                                 g_width,
                                 g_height,
                                 mode);

        libyuv::I420ToARGB((const uint8 *) g_dst_i420_y, g_height,
                           (const uint8 *) g_dst_i420_u, g_height / 2,
                           (const uint8 *) g_dst_i420_v, g_height / 2,
                           (uint8 *) dst_argb, g_height * 4,
                           g_height,
                           g_width);
    }
}

void nv12_to_argb(const uint8 *src_nv21, uint8 *dst_argb) {
    if (g_rotate == 0) {
        rotate_and_convert(src_nv21, dst_argb, libyuv::RotationMode::kRotate0);
    } else if (g_rotate == 90) {
        rotate_and_convert(src_nv21, dst_argb, libyuv::RotationMode::kRotate90);
    } else if (g_rotate == 180) {
        rotate_and_convert(src_nv21, dst_argb, libyuv::RotationMode::kRotate180);
    } else if (g_rotate == 270) {
        rotate_and_convert(src_nv21, dst_argb, libyuv::RotationMode::kRotate270);
    } else {
        rotate_and_convert(src_nv21, dst_argb, libyuv::RotationMode::kRotate0);
    }
}

JNIEXPORT void JNICALL
Java_com_example_camera_camera_YUVDecoder_nativeNV12ToAgb(JNIEnv *env,
                                              jclass clazz,
                                              jbyteArray yuvArray,
                                              jobject bitmap) {
    AndroidBitmapInfo bitmap_info;
    int ret;

    if ((ret = AndroidBitmap_getInfo(env, bitmap, &bitmap_info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }

    if (bitmap_info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return;
    }

    if ((ret = AndroidBitmap_lockPixels(env, bitmap, (void **) &g_dst_argb)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
        return;
    }

    jboolean copy = 0;
    jbyte *src_argb = env->GetByteArrayElements(yuvArray, &copy);

    nv12_to_argb((uint8 *) src_argb, (uint8 *) g_dst_argb);

    env->ReleaseByteArrayElements(yuvArray, src_argb, 0);
    AndroidBitmap_unlockPixels(env, bitmap);
}

#ifdef __cplusplus
}
#endif