
#define CLEAR(x) memset(&(x), 0, sizeof(x))

#define ERROR_LOCAL -1
#define SUCCESS_LOACL 0

#ifndef CAMERA_V4L2UTILS_H
#define CAMERA_V4L2UTILS_H
#include <stdio.h>
#include <sys/ioctl.h>
#include <android/log.h>

namespace V4l2 {
    class V4l2Utils {
    public:
        V4l2Utils();
        ~V4l2Utils();

        static int errnoexit(const char *s);
        static int xioctl(int fd, int request, void  *arg);

        static void buffer_log(unsigned char *src, size_t length);
    };
}


#endif //CAMERA_V4L2UTILS_H
