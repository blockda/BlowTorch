package com.happygoatstudios.btpro;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ProLauncher extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Intent launch = new Intent(this,com.happygoatstudios.bt.launcher.Launcher.class);
        launch.putExtra("LAUNCH_MODE","com.happygoatstudios.btpro");
        this.startActivity(launch);
        
        this.finish();
    }
}