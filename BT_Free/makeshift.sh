ant clean
python update_manifest.py
cp -rv ../../btscripts/button_ex/src/ ./assets/share/lua/5.1/
rm -rf ./assets/share/lua/5.1/button_window_ex.xml 
python integrate_button_ex.py 
ant release
adb install -r bin/BlowTorch-release.apk
adb shell am start -n com.happygoatstudios.bt/com.offsetnull.btfree.FreeLauncher 
