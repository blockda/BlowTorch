NDK=/Users/render/android/ndk-r16b
NDKABI=14
NDKVER=$NDK/toolchains/arm-linux-androideabi-4.9
NDKP=$NDKVER/prebuilt/$NDK_HOST_CC_TARGET/bin/arm-linux-androideabi-
NDKF="-isystem /Users/render/android/ndk-r16b/sysroot/usr/include/arm-linux-androideabi/ --sysroot=$NDK/sysroot"
NDKZ="--sysroot $NDK/platforms/android-14/arch-arm/"
#NDKZ="-L/Users/render/android/ndk-r16b/platforms/android-14/arch-arm/usr/lib/ --sysroot $NDK/platforms/android-14/arch-arm/"
make HOST_CC="gcc -m32" CROSS=/Users/render/android/toolchain/bin/arm-linux-androideabi- TARGET_CFLAGS="$NDKF" TARGET_LDFLAGS="$NDKZ" TARGET_SYS=Linux
