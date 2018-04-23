#ifndef CAMERA_USBCAMERA_H
#define CAMERA_USBCAMERA_H

#include "V4l2Device.h"
#include "V4l2Capture.h"
namespace camera {
    class USBCamera {
    public:
        USBCamera();
        ~USBCamera();

        int open_camera(const char *name, int width, int height, int rate1, int rate2);

        int load_preview_frame(unsigned char **preview_buffer, int *length, void** buf_id);

        int release_preview_frame(void *buf_id);

        int is_camera_attached();

        void close_camera();

    private:
        V4l2::V4l2Device *_device;
        V4l2::V4l2Capture *_capture;
    };
}

#endif //CAMERA_USBCAMERA_H
