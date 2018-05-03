LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_ARCH_ABI),armeabi)
include $(CLEAR_VARS)
LOCAL_MODULE := libmarshal
LOCAL_MODULE_FILENAME := libmarshal
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
LOCAL_SRC_FILES := ./lmarshal.c
LOCAL_SHARED_LIBRARIES := lua
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared
include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
include $(CLEAR_VARS)
LOCAL_ARM_MODE := thumb
LOCAL_MODULE := libmarshal
LOCAL_MODULE_FILENAME := libmarshal
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
LOCAL_SRC_FILES := ./lmarshal.c
LOCAL_SHARED_LIBRARIES := lua 
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared -D__ARM_V7__ -g
include $(BUILD_SHARED_LIBRARY)
endif

ifeq ($(TARGET_ARCH_ABI),mips)
include $(CLEAR_VARS)
LOCAL_MODULE := libmarshal
LOCAL_MODULE_FILENAME := libmarshal
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
LOCAL_SRC_FILES := ./lmarshal.c
LOCAL_SHARED_LIBRARIES := lua
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared
include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),x86)
include $(CLEAR_VARS)
LOCAL_ARM_MODE := thumb
LOCAL_MODULE := libmarshal
LOCAL_MODULE_FILENAME := libmarshal
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../luajava
LOCAL_SRC_FILES := ./lmarshal.c
LOCAL_SHARED_LIBRARIES := lua 
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared -D__ARM_V7__ -g
include $(BUILD_SHARED_LIBRARY)
endif

