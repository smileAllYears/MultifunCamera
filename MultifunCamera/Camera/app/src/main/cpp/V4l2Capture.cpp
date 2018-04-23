#include <assert.h>
#include <fcntl.h>
#include <malloc.h>
#include <errno.h>
#include <stdio.h>
#include <linux/videodev2.h>

#include "V4l2Capture.h"
#include "utils/Logger.h"
#include "utils/V4l2Utils.h"

extern unsigned int BUFFER_COUNT;

V4l2::V4l2Capture::V4l2Capture(V4l2::V4l2Device *device):_device(device) {
}

V4l2::V4l2Capture::~V4l2Capture() {
}

int V4l2::V4l2Capture::start_capture(int fd) {
    enum v4l2_buf_type type;

    for (unsigned int i = 0; i < BUFFER_COUNT; ++i) {
        struct v4l2_buffer buf;
        CLEAR(buf);
        buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        buf.index = i;
        buf.memory = V4L2_MEMORY_MMAP;

        if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_QBUF, &buf)) {
            return V4l2::V4l2Utils::errnoexit("VIDIOC_QBUF");
        }
    }

    type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_STREAMON, &type)) {
        return V4l2::V4l2Utils::errnoexit("VIDIOC_STREAMON");
    }

    return SUCCESS_LOACL;
}

int V4l2::V4l2Capture::read_frame(int fd, V4l2::V4L2Buffer *frame_buffers,
                                  unsigned char **preview_buffer, int *length, void **buf_id) {
    struct v4l2_buffer *buf;

    *buf_id = 0;

    buf = (struct v4l2_buffer *) malloc(sizeof(struct v4l2_buffer *));
    if (!buf) {
        return ERROR_LOCAL;
    }

    if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_DQBUF, buf)) {
        free(buf);
        switch (errno) {
            case EAGAIN:
                break;
            case EIO:
            default:
                return V4l2::V4l2Utils::errnoexit("VIDIOC_DQBUF");
        }
    }

    assert(buf->index < BUFFER_COUNT);

    *preview_buffer = (unsigned char *) frame_buffers[buf->index]._start;
    *length = frame_buffers[buf->index]._length;

    *buf_id = buf;

    return SUCCESS_LOACL;
}

int V4l2::V4l2Capture::release_capture_buffer(int fd, void *buf_id) {
    struct v4l2_buffer *buf = (struct v4l2_buffer *) buf_id;

    if (buf) {
        if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_QBUF, buf)) {
            free(buf);
            return V4l2::V4l2Utils::errnoexit("VIDIOC_QBUF");
        } else {
            free(buf);
        }
    }
    return SUCCESS_LOACL;
}

int V4l2::V4l2Capture::stop_capturing(int fd) {
    enum v4l2_buf_type type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

    if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_STREAMOFF, &type)) {
        return V4l2::V4l2Utils::errnoexit("VIDIOC_STREAMOFF");
    }

    return SUCCESS_LOACL;
}

int V4l2::V4l2Capture::process_camera(int fd, V4l2::V4L2Buffer *frame_buffers,
                                      unsigned char **preview_buffer, int *length, void **buf_id,
                                      int timeout_ms) {
    int ret = 0;
    fd_set fds;
    struct timeval tv;

    *buf_id = 0;
    if (fd == -1) {
        return ERROR_LOCAL;
    }

    FD_ZERO(&fds);
    FD_SET(fd, &fds);

    tv.tv_sec = timeout_ms / 1000;
    tv.tv_usec = (timeout_ms % 1000) * 1000;

    ret = select(fd + 1, &fds, NULL, NULL, &tv);
    if (-1 == ret) {
        return V4l2::V4l2Utils::errnoexit("select");
    } else if (0 == ret) {
        LOGE("select timeout");
        return ERROR_LOCAL;
    } else {
        ret = read_frame(fd, frame_buffers, preview_buffer, length, buf_id);
        if (ret == SUCCESS_LOACL) {
            return SUCCESS_LOACL;
        } else {
            if (ret == ENODEV) {
                LOGE("device lost");
                return ERROR_LOCAL;
            } else {
                LOGE("read frame failed");
                return  ERROR_LOCAL;
            }
        }
    }
}


void V4l2::V4l2Capture::stop_camera(int *fd) {
    stop_capturing(*fd);
    if (NULL != _device) {
        _device->uninit_device();
        _device->close_device(fd);
    }
}
