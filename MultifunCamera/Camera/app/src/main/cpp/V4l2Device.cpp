#include "V4l2Device.h"
#include <sys/stat.h>
#include <sys/mman.h>
#include <malloc.h>
#include <unistd.h>
#include <errno.h>
#include <fcntl.h>
#include <string.h>

#include <linux/videodev2.h>
#include "utils/Logger.h"
#include "utils/V4l2Utils.h"

V4l2::V4L2Buffer *frame_buffers = 0;//帧缓存
unsigned int BUFFER_COUNT = 0;

V4l2::V4l2Device::V4l2Device() {

}

V4l2::V4l2Device::~V4l2Device() {

}

/**
 * 打开设备
 * @param dev_name
 * @param fd
 * @return
 */
int V4l2::V4l2Device::open_device(const char *dev_name, int *fd) {
    struct stat st;

    /**
     * 函数说明:通过文件名获取文件信息，并保存在buf所值的结构stat中
     * 返回值：0 成功 -1 失败
     *
     * 错误代码:
     *  ENOENT         参数file_name指定的文件不存在
     *  ENOTDIR        路径中的目录存在但却非真正的目录
     *  ELOOP          欲打开的文件有过多符号连接问题，上限为16符号连接
     *  EFAULT         参数buf为无效指针，指向无法存在的内存空间
     *  EACCESS        存取文件时被拒绝
     *  ENOMEM         核心内存不足
     *  ENAMETOOLONG   参数file_name的路径名称太长
     */
    if (-1 == stat(dev_name, &st)) {
        LOGE("Cannot identify '%s':%d, %s", dev_name, errno, strerror(errno));
        return ERROR_LOCAL;
    }

    /**
     * 判断是否为字符装置文件
     */
    if (!S_ISCHR(st.st_mode)) {
        LOGE("%s is not a valid device", dev_name);
        return ERROR_LOCAL;
    }

    /**
     * 打开文件。在文件与文件句柄之间建立联系
     *
     * #define O_ACCMODE       00003     //文件访问模式屏蔽码
     * #define O_RDONLY        00        //只读方式打开
     * #define O_WRONLY        01        //只写方式打开
     * #define O_RDWR          02        //以读写方式打开文件
     * #define O_CREAT         00100     //文件不存在，就创建
     * #define O_EXCL          00200     //独占使用文件标志
     * #define O_NOCTTY        00400     //不分配终端
     * #define O_TRUNC         01000     //若文件已存在且是写操作,则长度截为0
     * #define O_APPEND        02000     //追加方式打开,文件指针置为文件尾
     * #define O_NONBLOCK  04000         //非阻塞方式打开和操作文件
     * #define O_NDELAY        O_NONBLOCK//非阻塞方式打开和操作文件
                              //
     * #define F_DUPFD         0         //拷贝文件句柄为最小值且没有使用的句柄
     * #define F_GETFD         1         //取文件句柄标志
     * #define F_SETFD         2         //设置文件句柄标志
     * #define F_GETFL         3         //取文件状态标志和访问模式
     * #define F_SETFL         4         //设置文件状态标志和访问模式
     * #define F_GETLK         5         //返回阻止锁定的flock结构
     * #define F_SETLK         6         //设置(F_RDLCK或F_WRLCK)或清除锁定
     * #define F_SETLKW        7         //等待设置或清除锁定
     */
    *fd = open(dev_name, O_RDWR | O_NONBLOCK, 0);

    if (-1 == *fd) {
        LOGE("Cannot open '%s': %d, %s", dev_name, errno, strerror(errno));
        if (EACCES == errno) {
            LOGE("Insufficient permissions on '%s': %d, %s", dev_name, errno, strerror(errno));
        }

        return ERROR_LOCAL;
    }

    return SUCCESS_LOACL;
}
/**
 * 映射文件到内存中(初始化内存)
 * @param fd
 * @return
 */
int V4l2::V4l2Device::init_mmap(int fd) {
    struct v4l2_requestbuffers req;

    req.memory = V4L2_MEMORY_MMAP;
    /**
     * 只有使用V4L2_MEMORY_MMAP这个类型
     * count字段才启作用
     * 注意：count是个输入输出函数。因为你所申请到的Buffer个数不一定就是你所输入的Number。所以在ioctl执行后，
     * driver会将真实申请到的buffer个数填充到此field. 这个数目有可能大于你想要申请的，也可能小与，甚至可能是0个。
     * 应用程序可以再次调用ioctl--VIDIOC_REQBUFS 来修改buffer个数。
     * 但前提是必须先释放已经 mapped 的 buffer ，可以先 munmap ，然后设置参数 count 为 0 来释放所有的 buffer。
     */
    req.count = 4;
    req.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

    if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_REQBUFS, &req)) {
        if (EINVAL == errno) {
            LOGE("device does not support memory mapping");
            return ERROR_LOCAL;
        }

        return V4l2::V4l2Utils::errnoexit("VIDIOC_REQBUFS");
    }

    if (req.count < 2) {
        LOGE("devices dose not support memory mapping");
        return ERROR_LOCAL;
    }

    frame_buffers = (V4L2Buffer *) calloc(req.count, sizeof(*frame_buffers));

    for (BUFFER_COUNT = 0; BUFFER_COUNT < req.count; ++BUFFER_COUNT) {
        struct v4l2_buffer buf;

        buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        buf.memory = V4L2_MEMORY_MMAP;
        buf.index = BUFFER_COUNT;

        if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_QUERYBUF, &buf)) {
            return V4l2::V4l2Utils::errnoexit("VIDOC_QUERYBUF");
        }

        frame_buffers[BUFFER_COUNT]._length = buf.length;
        frame_buffers[BUFFER_COUNT]._start = mmap(NULL, buf.length,
                                                PROT_READ | PROT_WRITE, MAP_SHARED,
                                                  fd, buf.m.offset);

        if (MAP_FAILED == frame_buffers[BUFFER_COUNT]._start) {
            return V4l2::V4l2Utils::errnoexit("mmap");
        }
    }

    return SUCCESS_LOACL;
}

/**
 * 初始化设备
 * @param fd
 * @return
 */
int V4l2::V4l2Device::init_device(int fd) {
    struct v4l2_capability cap;
    struct v4l2_cropcap cropcap;
    struct v4l2_crop crop;

    if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_QUERYCAP, &cap)) {
        if (EINVAL == errno) {
            LOGE("not a valid V4L2 device");
            return ERROR_LOCAL;
        }

        return V4l2::V4l2Utils::errnoexit("VIDIOC_QUERYCAP");
    }

    if (!(cap.capabilities & V4L2_CAP_VIDEO_CAPTURE)) {
        LOGE("devices is not a video capture device");
        return ERROR_LOCAL;
    }

    if (!(cap.capabilities & V4L2_CAP_STREAMING)) {
        LOGE("devices is not a video capture device");
        return ERROR_LOCAL;
    }

    CLEAR(cropcap);
    cropcap.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

    if (0 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_CROPCAP, &cropcap)) {
        crop.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        crop.c = cropcap.defrect;

        if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_S_CROP, &crop)) {
            switch (errno) {
                case EINVAL:
                    break;
                default:
                    break;
            }
        }
    }

    return init_mmap(fd);
}
/**
 *
 */
int V4l2::V4l2Device::uninit_device() {
    if (!frame_buffers) {
        return SUCCESS_LOACL;
    }
    for (unsigned int i = 0; i < BUFFER_COUNT; ++i) {
        if (-1 == munmap(frame_buffers[i]._start, frame_buffers[i]._length)) {
            return V4l2::V4l2Utils::errnoexit("munmap");
        }
    }

    free(frame_buffers);
    frame_buffers = NULL;
    return SUCCESS_LOACL;
}
/**
 * 关闭设备
 * @param fd
 * @return
 */
int V4l2::V4l2Device::close_device(int *fd) {
    int result = SUCCESS_LOACL;

    if (-1 != *fd && -1 == close(*fd)) {
        result = V4l2::V4l2Utils::errnoexit("close");
    }
    *fd = -1;
    return result;
}
/**
 * 设置格式
 * @param fd
 * @param width
 * @param height
 * @return
 */
int V4l2::V4l2Device::set_format(int fd, int width, int height) {
    struct v4l2_format fmt;
    unsigned int min;

    CLEAR(fmt);
    fmt.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    fmt.fmt.pix.width = width;
    fmt.fmt.pix.height = height;
    fmt.fmt.pix.pixelformat = V4L2_PIX_FMT_MJPEG;
    fmt.fmt.pix.field = V4L2_FIELD_INTERLACED;

    if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_S_FMT, &fmt)) {
        return V4l2::V4l2Utils::errnoexit("VIDIOC_S_FMT");
    }

    min = fmt.fmt.pix.width * 2;
    if (fmt.fmt.pix.bytesperline < min) {
        fmt.fmt.pix.bytesperline = min;
    }

    min = fmt.fmt.pix.bytesperline * fmt.fmt.pix.height;
    if (fmt.fmt.pix.sizeimage < min) {
        fmt.fmt.pix.sizeimage = min;
    }

    return SUCCESS_LOACL;
}
/**
 * 获取格式
 * @param fd
 * @return
 */
int V4l2::V4l2Device::get_format(int fd) {
    struct v4l2_format fmt;
    CLEAR(fmt);

    fmt.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_S_FMT, &fmt)) {
        return V4l2::V4l2Utils::errnoexit("VIDIOC_G_FMT");
    }

    return SUCCESS_LOACL;
}
/**
 * 设置帧角度
 * @param fd
 * @param fps
 * @return
 */
int V4l2::V4l2Device::set_frame_rate(int fd, int fps) {
    struct v4l2_streamparm sfps;

    CLEAR(sfps);
    sfps.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    sfps.parm.capture.timeperframe.numerator = 1;
    sfps.parm.capture.timeperframe.denominator = fps;

    if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_S_PARM, &sfps)) {
        return V4l2::V4l2Utils::errnoexit("VIDIOC_S_PARM");
    }

    return SUCCESS_LOACL;
}
/**
 * 获取帧角度
 * @param fd
 * @return
 */
int V4l2::V4l2Device::get_frame_rate(int fd) {
    struct v4l2_streamparm sfps;

    CLEAR(sfps);
    sfps.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;

    if (-1 == V4l2::V4l2Utils::xioctl(fd, VIDIOC_G_PARM, &sfps)) {
        return V4l2::V4l2Utils::errnoexit("VIDIOC_G_PARM");
    }

    return SUCCESS_LOACL;
}
