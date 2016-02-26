#!/bin/bash
#THIS SCRIPT ASSUMES THAT THE ANDROID NDK IS INSTALLED AND UNPACKED AT THE LOCATION BELOW

NDK=$NDK_HOME

function print_pwd()
{
PWD=`pwd`
echo $PWD
}

#The target LuaJit Library source archive unpacked location.
LUAJIT="LuaJIT-2.0.3"
cd ./$LUAJIT
echo `pwd` 
#Make sure that the luaconf.h file has been appropriately modified to include the search path ./lib
#This is very important.

echo "**********************************************"
echo "********* Cleaning prior builds. *************"
echo "**********************************************"
make clean
cd ..
cd BTLib
$NDK/ndk-build clean
rm -rf ./jni/luajava/luaconf.h
rm -rf ./jni/luajava/lualib.h
rm -rf ./jni/luajava/luajit.h
rm -rf ./jni/luajava/lua.h
rm -rf ./jni/luajava/libluajit-armeabi.so
rm -rf ./jni/luajava/libluajit-armeabi.a
rm -rf ./jni/luajava/libluajit-armv7-a.a
rm -rf ./jni/luajava/libluajit-armv7-a.so
rm -rf ./jni/luajava/lauxlib.h
cd ..

echo "**********************************************"
echo "*************  STARTING BUILD ****************"
echo "**********************************************"
cd ./$LUAJIT
#start building.
NDKABI=8

NDKVER=$NDK/toolchains/arm-linux-androideabi-4.9
#NDK_HOST_CC_TARGET <- darwin-x86_64 for osx, linux-x86_64 for ubuntu
ARMEABI_NDKP=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/$NDK_HOST_CC_TARGET/bin/arm-linux-androideabi-

NDKF="-march=armv5te -mtune=xscale --sysroot $NDK/platforms/android-$NDKABI/arch-arm"
echo "Building ARMEABI Targets"
make HOST_CC="gcc -m32" CROSS=$ARMEABI_NDKP TARGET_FLAGS="$NDKF" TARGET_SYS=Linux
mv src/libluajit.a src/libluajit-armeabi.a
mv src/libluajit.so src/libluajit-armeabi.so

NDKF_V7A="--sysroot $NDK/platforms/android-$NDKABI/arch-arm"
echo "Building ARMv7A TARGETS"
make clean
make HOST_CC="gcc -m32" CROSS=$ARMEABI_NDKP TARGET_FLAGS="$NDKF_V7A" TARGET_SYS=Linux
mv src/libluajit.a src/libluajit-armv7-a.a
mv src/libluajit.so src/libluajit-armv7-a.so

#need to modify the above to produce output for the arm-v7 abi and the x86 and possibly mips.
#these libraries must be build with a "-<arch>.so" prefix that will match names in the android jni project.

#at this point the lua library should be ready to copy into the jni project in the BTLib subfolder to build luajava and the other lua extensions.
echo "Copying $LUAJIT output to LuaJava jni project in BTLib"
cp src/libluajit-armeabi.a ../BTLib/jni/luajava/libluajit-armeabi.a
cp src/libluajit-armeabi.so ../BTLib/jni/luajava/libluajit-armeabi.so
cp src/libluajit-armv7-a.a ../BTLib/jni/luajava/libluajit-armv7-a.a
cp src/libluajit-armv7-a.so ../BTLib/jni/luajava/libluajit-armv7-a.so


#copy the relevant header files into the luajava jni project folder in BTLib/jni/luajava, the other projects will reference it there.
echo "Copying $LUAJIT source headers to LuaJava jni project in BTLib"
cp src/lauxlib.h ../BTLib/jni/luajava/lauxlib.h
cp src/lua.h ../BTLib/jni/luajava/lua.h
cp src/luaconf.h ../BTLib/jni/luajava/luaconf.h
cp src/luajit.h ../BTLib/jni/luajava/luajit.h
cp src/lualib.h ../BTLib/jni/luajava/lualib.h

#move into the BTLib folder and build the android ndk projects (the lua extensions)
echo "************************************************"
echo "********** STARTING ANDROID NDK BUILD **********"
echo "************************************************"
cd ../BTLib
$NDK/ndk-build

#the libraries should all be nicely packaged into the libs folder in the BTLib project and we shouldn't need to do anything else, if the luaconfig trick worked.
