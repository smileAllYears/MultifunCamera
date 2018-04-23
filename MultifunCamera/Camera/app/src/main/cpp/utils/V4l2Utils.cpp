
#include "V4l2Utils.h"
#include <errno.h>
#include <string.h>
#include <sys/stat.h>

#include "../utils/Logger.h"

V4l2::V4l2Utils::V4l2Utils() {

}

V4l2::V4l2Utils::~V4l2Utils() {

}

int V4l2::V4l2Utils::xioctl(int fd, int request, void *arg) {
    int r;

    do {
        r = ioctl(fd, request, arg);
    }while (-1 == r && EINTR == errno);

    return r;
}

void V4l2::V4l2Utils::buffer_log(unsigned char *src, size_t length) {

    for (int i = 0; i < length; ++i) {
        unsigned char h1, h2, h3, h4;
        unsigned char h5, h6, h7, h8;

        h1 = src[i];
        h2 = src[i + 1];
        h3 = src[i + 2];
        h4 = src[i + 3];
        h5 = src[i + 4];
        h6 = src[i + 5];
        h7 = src[i + 6];
        h8 = src[i + 7];


        LOGI("mjpeg_buffer_log...%X%X, %X%X, %X%X, %X%X", h1, h2, h3, h4, h5, h6, h7,h8);
    }

    unsigned char end1, end2;

    end1 = src[length - 2];
    end2 = src[length - 1];

    int i = 1000;
    unsigned char m1, m2, m3, m4, m5, m6, m7, m8;

    m1 = src[i];
    m2 = src[i + 1];
    m3 = src[i + 2];
    m4 = src[i + 3];
    m5 = src[i + 4];
    m6 = src[i + 5];
    m7 = src[i + 6];
    m8 = src[i + 7];

    LOGI("mjpeg_buffer_log...\n");
    LOGI("mjpeg_buffer_log...%X%X,%X%X,%X%X,%X%X\n", m1, m2, m3, m4, m5, m6, m7, m8);
    LOGI("mjpeg_buffer_log...\n");
    LOGI("mjpeg_buffer_log...%X%X\n", end1, end2);
    LOGI("mjpeg_buffer_log...\n");
    LOGI("mjpeg_buffer_log...%d\n", length);
}

int V4l2::V4l2Utils::errnoexit(const char *s) {
    LOGE("%s error %d, %s", s, errno, strerror(errno));
    return errno;
}