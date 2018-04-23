#ifndef CAMERA_V4L2BUFFER_H
#define CAMERA_V4L2BUFFER_H

#include <cwchar>

namespace V4l2 {
    class V4L2Buffer {
    public:
        void *_start;
        size_t _length;
    };
}
#endif //CAMERA_V4L2BUFFER_H
