NDK=/Users/render/android/ndk-r15c
NDKABI=14
NDKVER=$NDK/toolchains/x86-4.9
NDKP=$NDKVER/prebuilt/$NDK_HOST_CC_TARGET/bin/i686-linux-android-
NDKF="--sysroot=$NDK/platforms/android-14/arch-x86/"
NDKARCH="-march=armv7-a -mfloat-abi=softfp -Wl,--fix-cortex-a8"
NDKZ="--sysroot $NDK/platforms/android-14/arch-arm/"
#NDKZ="-Lsystem /Users/render/android/ndk-r16b/platforms/android-14/arch-arm/usr/lib/ --sysroot $NDK/sysroot/"
make HOST_CC="gcc-7 -m32" CROSS=$NDKP TARGET_FLAGS="$NDKF" TARGET_SYS=Other
