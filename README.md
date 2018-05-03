#BlowTorch Source Code Repository

To Build BlowTorch you will need the following things downloaded:

- [x] Android SDK (latest version is fine)
- [x] Android NDK, specifically any version that isn't the latest. r15c is what I use.
- [x] Unpack these, note the paths, they will be needed later

External libraries like sqlite and luajit have sources checked into this tree. That may change in the future.

now set the following Environment Variables:

- [x] export ANDROID_SDK_ROOT=path to sdk root
- [x] export NDK_HOME=path to ndk root
- [x] export NDK_HOST_CC_TARGET=darwin-x86_64 for mac, linux-x86_64 for unix, i dont know for windows
- [x] NDKABI=14 is defined in build_ndk_libraries.sh this should be moved.

Now execute the NDK build script:

- [x] ./build_ndk_libraries.sh

With the native libraries built, the android project can be built with gradle:

- [x] ./gradlew :BT_Free:assembleRelease
- [x] ./gradlew :BT_Aard:aasembleRelease

note: the gradle project will attempt to sign the apk with a non existant certificate, drop your certificate into the appropriate location and the gradle project will pull the password from an environment variable, BT_RELEASE_PASS for the stock BlowTorch and BT_AARD_PASS for the Aardwolf client. If you do not know about apk signing, please see the android developer documentation.

double note: the location that the build scripts look for certificates is in the build.gradle for the respective project. For the stock BlowTorch client it looks for BTLib/key/bt_privatekey.keystore and for the aardwolf client it looks for BT_Aard/key/signiture_cert the passwords are passed via system environment variable.

If you want to set the password from the command line without seeing it use the following:
```shell
#!/bin/bash
 
read -s -p "Enter Password: " BT_RELEASE_PASS
export BT_RELEASE_PASS
```

The output from the gradle build is in BT_[Free|Aard]/build/outputs/apk.
 

