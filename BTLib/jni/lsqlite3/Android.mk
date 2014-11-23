LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_ARCH_ABI),armeabi)
include $(CLEAR_VARS)
LOCAL_MODULE := lsqlite3
LOCAL_MODULE_FILENAME := lsqlite3
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../sqlite3 $(LOCAL_PATH)/../luajava
LOCAL_SRC_FILES := ./lsqlite3.c
LOCAL_SHARED_LIBRARIES := sqlite3 lua 
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared
include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
include $(CLEAR_VARS)
LOCAL_MODULE := lsqlite3
LOCAL_MODULE_FILENAME := lsqlite3
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../sqlite3 $(LOCAL_PATH)/../luajava
LOCAL_SRC_FILES := ./lsqlite3.c
LOCAL_SHARED_LIBRARIES := sqlite3 lua
LOCAL_CFLAGS := -O3 -fpic -std=c99 -shared
include $(BUILD_SHARED_LIBRARY)
endif

