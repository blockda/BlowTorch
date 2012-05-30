LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_ARCH_ABI),armeabi)
LOCAL_MODULE := bit
LOCAL_MODULE_FILENAME := bit
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../lua
LOCAL_SRC_FILES := ./bit.c
LOCAL_SHARED_LIBRARIES := liblua
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared

include $(BUILD_SHARED_LIBRARY)

endif
