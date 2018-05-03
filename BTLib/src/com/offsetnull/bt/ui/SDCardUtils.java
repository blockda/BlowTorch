package com.offsetnull.bt.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


import com.offsetnull.bt.R;
import com.offsetnull.bt.launcher.Launcher;
import com.offsetnull.bt.settings.ConfigurationLoader;

public class SDCardUtils {

    public static String getSDCardRoot(AppCompatActivity a, boolean external) {
        try {
            String exportDir = ConfigurationLoader.getConfigurationValue("exportDirectory",a);
            Context c = a.createPackageContext(a.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            String dir = (external == true) ? "/" + exportDir : c.getExternalFilesDir(null).getAbsolutePath();
            return dir;
        } catch(PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasPermissions(final AppCompatActivity activity, View root, final int code) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {


                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Snackbar.make(root, R.string.sdcardpermissions,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(activity,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        code);
                            }
                        })
                        .show();


            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        code);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false;
        } else {
            // Permission has already been granted
            return true;
        }


    }

}
