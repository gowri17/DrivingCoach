package com.drava.android.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import com.drava.android.R;
import com.drava.android.base.BaseActivity;
import com.drava.android.welcome.WelcomeActivity;

public class MainActivity extends BaseActivity {

    private static String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setStatusBarColor();
        requestPhoneStatePermission();
    }

    private void requestPhoneStatePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String PERMISSION : PERMISSIONS) {
                if (PERMISSION.equalsIgnoreCase(Manifest.permission.READ_PHONE_STATE)) {
                    if ((checkSelfPermission(PERMISSION)) != 0) {
                        requestPermissions(new String[]{PERMISSION}, 1);
                    }else{
                        requestContactsReadPermission();
                    }
                }
            }
        }else{
            launchNextActivity();
        }
    }

    private void requestContactsReadPermission(){
        for(String PERMISSION : PERMISSIONS){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(PERMISSION.equalsIgnoreCase(Manifest.permission.READ_CONTACTS)){
                    if((checkSelfPermission(PERMISSION))!= 0){
                        requestPermissions(new String[]{PERMISSION}, 2);
                    }else{
                        launchNextActivity();
                    }
                }
            }
        }
    }

    private void launchNextActivity(){
        Intent welcomeIntent = new Intent(MainActivity.this, WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(welcomeIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Phone State Permission Granted", Toast.LENGTH_SHORT).show();
                    requestContactsReadPermission();
//                    launchNextActivity();
                } else {
                    Toast.makeText(this, "Phone State Permission Denied", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                        promptSettings("Phone State");
                    } else {
                        promptSettings("Phone State");
                    }
                }
                break;

            case 2:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(this, "Read Contacts Permission Granted", Toast.LENGTH_SHORT).show();
                    launchNextActivity();
                }else{
                    Toast.makeText(this, "Read Contacts Permission Denied", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                        promptSettings("Read Contacts");
                    } else {
                        promptSettings("Read Contacts");
                    }
                }
                break;
        }
    }

    private void promptSettings(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(String.format(getResources().getString(R.string.denied_title), type));
        builder.setMessage(String.format(getString(R.string.denied_msg), type));
        builder.setPositiveButton("go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                goToSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(myAppSettings);
    }
}
