LOCAL_PATH := $(call my-dir)

ifeq ($(TARGET_ARCH_ABI),armeabi)

include $(CLEAR_VARS)
LOCAL_MODULE := lua
LOCAL_ARM_MODE := arm
LOCAL_CFLAGS := -O2 -fno-omit-frame-pointer -shared -std=c99 -DLUA_USE_DLOPEN
LOCAL_SRC_FILES :=  lapi.c \
	lauxlib.c \
	lbaselib.c \
	lcode.c \
	ldblib.c \
	ldebug.c \
	ldo.c \
	ldump.c \
	lfunc.c \
	lgc.c \
	linit.c \
	liolib.c \
	llex.c \
	lmathlib.c \
	lmem.c \
	loadlib.c \
	lobject.c \
	lopcodes.c \
	loslib.c \
	lparser.c \
	lstate.c \
	lstring.c \
	lstrlib.c \
	ltable.c \
	ltablib.c \
	ltm.c \
	lundump.c \
	lvm.c \
	lzio.c \
	print.c \
   	luajava.c	
include $(BUILD_SHARED_LIBRARY)

endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)

include $(CLEAR_VARS)
LOCAL_MODULE := lua
LOCAL_CFLAGS := -O3 -shared -std=c99 -DLUA_USE_DLOPEN
LOCAL_SRC_FILES :=  lapi.c \
	lauxlib.c \
	lbaselib.c \
	lcode.c \
	ldblib.c \
	ldebug.c \
	ldo.c \
	ldump.c \
	lfunc.c \
	lgc.c \
	linit.c \
	liolib.c \
	llex.c \
	lmathlib.c \
	lmem.c \
	loadlib.c \
	lobject.c \
	lopcodes.c \
	loslib.c \
	lparser.c \
	lstate.c \
	lstring.c \
	lstrlib.c \
	ltable.c \
	ltablib.c \
	ltm.c \
	lundump.c \
	lvm.c \
	lzio.c \
	print.c \
	luajava.c
include $(BUILD_SHARED_LIBRARY)

endif
