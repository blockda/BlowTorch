#BlowTorch Source Code Repository

To Build BlowTorch you will need the following things downloaded:

- [x] Android SDK (latest version is fine)
- [x] Android NDK, specifically any version that isn't the latest. r15c is what I use.
- [x] Unpack these, note the paths, they will be needed later

External libraries like sqlite and luajit have sources checked into this tree. That may change in the future.

now set the following Environment Variables:

- [x] export ANDROID_SDK_ROOT=<path to sdk root>
- [x] export NDK_HOME=<path to ndk root>
- [x] export NDK_HOST_CC_TARGET=<darwin-x86_64 for mac, linux-x86_64 for unix, i dont know for windows>
- [x] NDKABI=14 is defined in build_ndk_libraries.sh this should be moved.

Now execute the NDK build script:

- [x] ./build_ndk_libraries.sh

With the native libraries built, the android project can be built with gradle:

- [x] ./gradlew :BT_Free:assembleRelease
- [x] ./gradlew :BT_Aard:aasembleRelease

note: the gradle project will attempt to sign the apk with a non existant certificate, drop your certificate into the appropriate location and the gradle project will pull the password from an environment variable, BT_RELEASE_PASS for the stock BlowTorch and BT_AARD_PASS for the Aardwolf client. If you do not know about apk signing, please see the android developer documentation.

The output from the gradle build is in BT_[Free|Aard]/build/outputs/apk.
 

