LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_ARM_MODE := arm
LOCAL_MODULE := luajit
LOCAL_SRC_FILES := libluajit.a
include $(PREBUILT_STATIC_LIBRARY)


ifeq ($(TARGET_ARCH_ABI),armeabi)

include $(CLEAR_VARS)
LOCAL_MODULE := lua
LOCAL_STATIC_LIBRARIES := luajit
LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -O3 -shared -std=c99 -DLUA_USE_DLOPEN
LOCAL_SRC_FILES := luajava.c	
include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)

include $(CLEAR_VARS)
LOCAL_MODULE := lua
LOCAL_STATIC_LIBRARIES := luajit
LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -O3 -shared -std=c99 -DLUA_USE_DLOPEN
LOCAL_SRC_FILES := 	luajava.c
include $(BUILD_SHARED_LIBRARY)

endif
