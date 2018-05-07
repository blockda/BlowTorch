# Contributors Notice

I am accepting pull requests, feature requests and issues using the github project tracking tools. If you need more help setting up the code base for compilation, assistance in finding out how something works, or where to go to look for "x", please feel free to ask me. This is the first time I've ever managed an open source project so I could use feedback on how I could do things better. Or let me know if I've got something out of bad practice checked into source code, BlowTorch is a work in progress; It can always be better.

# BlowTorch Source Code Repository

## To Build BlowTorch you will need the following things downloaded:

* Android SDK (latest version is fine)
* Android NDK, specifically any version that isn't the latest. r15c is what I use.
* Unpack these, note the paths, they will be needed later
* For now the LuaJit, SQLite3, LuaJava, and Lua extension modules have their source code checked in for ease of use. I am pretty sure this is bad practice so it will probably be replaced by a downloader script or instructions here on what version to download and where to extract it.


## Now set the following Environment Variables:

* export ANDROID_SDK_ROOT=path to sdk root
* export NDK_HOME=path to ndk root
* export NDK_HOST_CC_TARGET=darwin-x86_64 for mac, linux-x86_64 for unix, i dont know for windows
* NDKABI=14 is defined in build_ndk_libraries.sh this should be moved.
* The current build script hard codes the compiler as GCC v.7. This should be pulled out into an environment variable.

## Now execute the NDK build script:

* ./build_ndk_libraries.sh

## With the native libraries built, the android project can be built with gradle:


* ./gradlew :BT_Free:assembleDebug
* ./gradlew :BT_Free:assembleDebug

or

* ./gradlew :BT_Free:assembleRelease
* ./gradlew :BT_Aard:aasembleRelease

note: the gradle project will attempt to sign the apk with a non existant certificate, drop your certificate into the appropriate location and the gradle project will pull the password from an environment variable, BT_RELEASE_PASS for the stock BlowTorch and BT_AARD_PASS for the Aardwolf client. If you do not know about apk signing, please see the android developer documentation.

double note: the location that the build scripts look for certificates is in the build.gradle for the respective project. For the stock BlowTorch client it looks for BTLib/key/bt_privatekey.keystore and for the aardwolf client it looks for BT_Aard/key/signiture_cert the passwords are passed via system environment variable.

If you want to set the password from the command line without seeing it use the following:
```shell
#!/bin/bash
 
read -s -p "Enter Password: " BT_RELEASE_PASS
export BT_RELEASE_PASS
```
Taking special care to source the script into your current environment, or the script will execute inside of its own session, set the variable and then the environment terminates with the program.
```shell
source ~/bt_enter.sh
```

The output from the gradle build is in BT_[Free|Aard]/build/outputs/apk.
 

