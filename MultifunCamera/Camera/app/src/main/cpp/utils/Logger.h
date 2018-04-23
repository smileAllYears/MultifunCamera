#ifndef CAMERA_LOGGER_H
#define CAMERA_LOGGER_H "fuction-camera"

#ifndef LOG_TAG
#define LOG_TAG "V4l2"
#endif
#include <android/log.h>

#define LOG_PRINT(leve, fmt, ...) do { \
    __android_log_print(leve, LOG_TAG, "(%u) %s" fmt, \
                        __LINE__, __PRETTY_FUNCTION__, ##__VA_ARGS__);\
} while(0)


#define LOGI(fmt, ...) LOG_PRINT(ANDROID_LOG_INFO, fmt, ##__VA_ARGS__);
#define LOGW(fmt, ...) LOG_PRINT(ANDROID_LOG_WARN, fmt, ##__VA_ARGS__);
#define LOGD(fmt, ...) LOG_PRINT(ANDROID_LOG_DEBUG, fmt, ##__VA_ARGS__);
#define LOGE(fmt, ...) LOG_PRINT(ANDROID_LOG_ERROR, fmt, ##__VA_ARGS__);
#define LOGF(fmt, ...) LOG_PRINT(ANDROID_LOG_FATAL, fmt, ##__VA_ARGS__);

#endif //CAMERA_LOGGER_H
