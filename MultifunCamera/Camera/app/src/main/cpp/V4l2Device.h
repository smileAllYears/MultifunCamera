#ifndef CAMERA_V4L2DEVICE_H
#define CAMERA_V4L2DEVICE_H

#include <iostream>
#include "V4L2Buffer.h"

namespace V4l2 {
    class V4l2Device {
    private:
        typedef struct {
            void *start;
            size_t length;
        };
    public:
        V4l2Device();
        ~V4l2Device();

        /**
         * 为视频帧在帧缓存区分配内存
         * @param fd
         * @return
         */
        int init_mmap(int fd);
        /**
         * 打开设备描述符。
         * @param dev_name
         * @param fd
         * @return
         */
        int open_device(const char *dev_name, int *fd);

        /**
         * 初识化设备。
         * @param fd
         * @return
         */
        int init_device(int fd);

        /**
         * 从设备取消映射并释放内存映射的帧缓冲区。
         * @return
         */
        int uninit_device();

        /**
         * 关掉一个文件描述符。
         * @param fd
         * @return
         */
        int close_device(int *fd);
        /**
         * 设置相机预览格式
         * @param fd
         * @param width
         * @param height
         * @return
         */
        int set_format(int fd, int width, int height);
        /**
         * 获取相机预览格式
         * @param fd
         * @return
         */
        int get_format(int fd);
        /**
         * 设置相机帧方向
         * @param fd
         * @param fps
         * @return
         */
        int set_frame_rate(int fd, int fps);
        /**
         * 获取相机帧方向
         * @return
         */
        int get_frame_rate(int fd);

    };
}


#endif //CAMERA_V4L2DEVICE_H
