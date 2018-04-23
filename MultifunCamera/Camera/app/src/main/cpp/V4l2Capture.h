#ifndef CAMERA_V4L2CAPTURE_H
#define CAMERA_V4L2CAPTURE_H

#include "V4L2Buffer.h"
#include "V4l2Device.h"


namespace V4l2 {
    class V4l2Capture {
    public:
        V4l2Capture(V4l2::V4l2Device *device);

        ~V4l2Capture();

        int start_capture(int fd);

        int read_frame(int fd, V4l2::V4L2Buffer *frame_buffers,
                       unsigned char **preview_buffer, int *length, void **buf_id);

        int stop_capturing(int fd);

        int process_camera(int fd, V4l2::V4L2Buffer *frame_buffers,
                           unsigned char **preview_buffer, int *length, void **buf_id,
                           int timeout_ms);

        int release_capture_buffer(int fd, void *buf_id);

        void stop_camera(int *fd);

    private:
        V4l2::V4l2Device *_device;
    };
}

#endif //CAMERA_V4L2CAPTURE_H
