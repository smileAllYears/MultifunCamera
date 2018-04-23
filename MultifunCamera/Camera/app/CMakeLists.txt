# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

include_directories(src/main/cpp/)
include_directories(src/main/cpp/include)


# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library(avutil-lib STATIC IMPORTED)
set_target_properties(avutil-lib PROPERTIES IMPORTED_LOCATION
${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavutil.a)

add_library(avcodec-lib STATIC IMPORTED)
set_target_properties(avcodec-lib PROPERTIES IMPORTED_LOCATION
${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavcodec.a)

add_library(avformat-lib STATIC IMPORTED)
set_target_properties(avformat-lib PROPERTIES IMPORTED_LOCATION
${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libavformat.a)

add_library(yuv_static-lib STATIC IMPORTED)
set_target_properties(yuv_static-lib PROPERTIES IMPORTED_LOCATION
${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libyuv_static.a)

add_library( # Sets the name of the library.
             function-camera

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/function-camera.cpp
             src/main/cpp/V4l2Buffer.cpp
             src/main/cpp/V4l2Capture.cpp
             src/main/cpp/V4l2Device.cpp
             src/main/cpp/USBCamera.cpp
             src/main/cpp/utils/V4l2Utils.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library(log-lib log)
find_library(m-lib m)
find_library(z-lib z)
find_library(dl-lib dl)
find_library(stdc++-lib stdc++)
find_library(jnigraphics-lib jnigraphics)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries(function-camera avformat-lib avcodec-lib avutil-lib
                      yuv_static-lib ${log-lib} ${m-lib}
                      ${z-lib} ${dl-lib} ${stdc++-lib} ${jnigraphics-lib})
add_library(Codec SHARED src/main/cpp/utils/nv12toargb.cpp)

target_link_libraries( Codec yuv_static-lib ${log-lib} ${m-lib} ${z-lib} ${dl-lib}
                       ${stdc++-lib} ${jnigraphics-lib} )