
#include "USBCamera.h"
#include "utils/V4l2Utils.h"

extern V4l2::V4L2Buffer *frame_buffers;

static int fd = -1;
static const int s_default_time_out = 3000;

camera::USBCamera::USBCamera() {
    _device = new V4l2::V4l2Device();
    _capture = new V4l2::V4l2Capture(_device);
}

camera::USBCamera::~USBCamera() {
    if (NULL != _device) {
        delete _device;
        _device = NULL;
    }
    if (NULL != _capture) {
        delete _capture;
        _capture = NULL;
    }
}

int camera::USBCamera::open_camera(const char *name, int width, int height, int rate1, int rate2) {
    int result = _device->open_device(name, &fd);
    if (result == ERROR_LOCAL) {
        return result;
    }

    result = _device->set_format(fd, width, height);
    if (result == ERROR_LOCAL) {
        return result;
    }

    result = _device->set_frame_rate(fd, rate1);
    if (result == ERROR_LOCAL) {
        return result;
    }


    result = _device->init_device(fd);
    if (result == ERROR_LOCAL) {
        return result;
    }

    result = _capture->start_capture(fd);

    return result;
}

int camera::USBCamera::load_preview_frame(unsigned char **preview_buffer, int *length,
                                          void **buf_id) {
    return _capture->process_camera(fd, frame_buffers, preview_buffer, length, buf_id,
                                    s_default_time_out);
}

int camera::USBCamera::release_preview_frame(void *buf_id) {
    return _capture->release_capture_buffer(fd, buf_id);
}

int camera::USBCamera::is_camera_attached() {
    return fd != -1;
}

void camera::USBCamera::close_camera() {
    return _capture->stop_camera(&fd);
}