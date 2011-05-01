package com.happygoatstudios.aardwolf;

import com.happygoatstudios.bt.R;
import com.happygoatstudios.bt.settings.ConfigurationLoader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;

public class AardLauncher extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        String windowAction = ConfigurationLoader.getConfigurationValue("windowAction", this);
        
        Intent launch = new Intent(windowAction);
        launch.putExtra("DISPLAY","Aardwolf RPG");
        launch.putExtra("HOST", "aardmud.org");
        launch.putExtra("PORT", "4010");
        //launch.putExtra("LAUNCH_MODE","com.happygoatstudios.bt");
        
        SharedPreferences prefs = this.getSharedPreferences("SERVICE_INFO",0);
    	Editor edit = prefs.edit();
    	
    	
    	edit.putString("SETTINGS_PATH", "Aardwolf RPG");
    	edit.commit();
        
        this.startActivity(launch);
        
        this.finish();
    }
}