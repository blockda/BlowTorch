LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_ARCH_ABI),armeabi)
LOCAL_MODULE := libbit
LOCAL_MODULE_FILENAME := libbit
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
LOCAL_SRC_FILES := ./bit.c
LOCAL_SHARED_LIBRARIES := lua
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared

include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
LOCAL_MODULE := libbit
LOCAL_MODULE_FILENAME := libbit
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
LOCAL_SRC_FILES := ./bit.c
LOCAL_SHARED_LIBRARIES := lua 
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared

include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),mips)
LOCAL_MODULE := libbit
LOCAL_MODULE_FILENAME := libbit
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
LOCAL_SRC_FILES := ./bit.c
LOCAL_SHARED_LIBRARIES := lua
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared

include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),x86)
LOCAL_MODULE := libbit
LOCAL_MODULE_FILENAME := libbit
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
LOCAL_SRC_FILES := ./bit.c
LOCAL_SHARED_LIBRARIES := lua 
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared

include $(BUILD_SHARED_LIBRARY)

endif
