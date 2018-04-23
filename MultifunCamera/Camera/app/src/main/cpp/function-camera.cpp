#include <jni.h>
#include <string>
#include "USBCamera.h"

extern "C"
JNIEXPORT jstring

JNICALL
Java_com_example_camera_camera_FunctionCamera_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
