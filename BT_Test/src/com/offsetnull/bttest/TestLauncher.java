package com.offsetnull.bttest;

import com.offsetnull.bt.R;
import com.offsetnull.bt.launcher.Launcher;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TestLauncher extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
        Intent launch = new Intent(this,com.offsetnull.bt.launcher.Launcher.class);
        launch.putExtra("LAUNCH_MODE","com.happygoatstudios.bttest");
        this.startActivity(launch);
        
        this.finish();
    }
}