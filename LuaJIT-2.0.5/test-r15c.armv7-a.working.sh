NDK=/Users/render/android/ndk-r15c
NDKABI=14
NDKVER=$NDK/toolchains/arm-linux-androideabi-4.9
NDKP=$NDKVER/prebuilt/$NDK_HOST_CC_TARGET/bin/arm-linux-androideabi-
NDKF="--sysroot=$NDK/platforms/android-14/arch-arm/"
NDKARCH="-march=armv7-a -mfloat-abi=softfp -Wl,--fix-cortex-a8"
NDKZ="--sysroot $NDK/platforms/android-14/arch-arm/"
#NDKZ="-Lsystem /Users/render/android/ndk-r16b/platforms/android-14/arch-arm/usr/lib/ --sysroot $NDK/sysroot/"
make HOST_CC="gcc -m32" CROSS=$NDKP TARGET_FLAGS="$NDKF $NDKARCH" TARGET_SYS=Other
