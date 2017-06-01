package com.drava.android.base;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.drava.android.BuildConfig;
import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.activity.AuthorizationActivity;
import com.drava.android.activity.HomeActivity;
import com.drava.android.activity.WebViewActivity;
import com.drava.android.activity.contacts.GetContactsActivity;
import com.drava.android.parser.ContentParser;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.welcome.InviteActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class SplashScreenActivity extends BaseActivity {

    private static final long SLEEP_DURATION = 3000;
    private boolean isDestroyed = false;
    private MyHandler mHandler;
    private static String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private GoogleCloudMessaging gcm;
    private String tokenID;
    private RelativeLayout layoutPoweredBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setStatusBarColor();
        init();
        requestPhoneStatePermission();
        getSettingData();
    }

    private void init(){
        layoutPoweredBy = (RelativeLayout) findViewById(R.id.relative_poweredby);
        if(BuildConfig.IS_CUSTOMER_VERSION) {       //R.L v1.1
            layoutPoweredBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.drava.biz"));
                    if(browserIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(browserIntent);
                    }
                }
            });
        }else{
            layoutPoweredBy.setVisibility(View.GONE);
        }
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
            initSplashScreen();
        }
    }

    private void requestContactsReadPermission(){
        for(String PERMISSION : PERMISSIONS){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(PERMISSION.equalsIgnoreCase(Manifest.permission.READ_CONTACTS)){
                    if((checkSelfPermission(PERMISSION))!= 0){
                        requestPermissions(new String[]{PERMISSION}, 2);
                    }else{
                        requestCoarseLocationPermission();
                    }
                }
            }
        }
    }

    private void requestCoarseLocationPermission(){
        for(String PERMISSION : PERMISSIONS){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(PERMISSION.equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION)){
                    if((checkSelfPermission(PERMISSION))!= 0){
                        requestPermissions(new String[]{PERMISSION}, 3);
                    }else{
                        requestFineLocationPermission();
                    }
                }
            }
        }
    }

    private void requestFineLocationPermission(){
        for(String PERMISSION : PERMISSIONS){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(PERMISSION.equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION)){
                    if((checkSelfPermission(PERMISSION))!= 0){
                        requestPermissions(new String[]{PERMISSION}, 4);
                    }else{
                        initSplashScreen();
                    }
                }
            }
        }
    }


    private void initSplashScreen()
    {
        mHandler = new MyHandler(this);
        mHandler.sendEmptyMessageDelayed(1, SLEEP_DURATION);
    }

    private void launchNextActivity(){
//        startRegisterService();
        if(isGooglePlayServiceAvailable()) {        //R.L v1.2
            if (getApp().getUserPreference().getIsHomePageShown()) {
                startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
            } else if (getApp().getUserPreference().getIsContactPageShown()) {
                startActivity(new Intent(SplashScreenActivity.this, GetContactsActivity.class));
            } else if (getApp().getUserPreference().getIsInvitePageShown()) {
                startActivity(new Intent(SplashScreenActivity.this, InviteActivity.class));
            } else {
//            startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
                startActivity(new Intent(SplashScreenActivity.this, AuthorizationActivity.class));
            }
        }
        finish();
    }

    private void startRegisterService(){
        if(!DravaApplication.getSnsPreference().isRegistered()){
            //startService(new Intent(this, RegisterService.class));
            Log.e("DraVA", "Register Service received...");
//            new AsyncCall().execute();        // worked for getting the Device Token for GCM Push Notification
        }
    }

    private boolean isGooglePlayServiceAvailable(){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(SplashScreenActivity.this);
        if(status != ConnectionResult.SUCCESS){
            if(googleApiAvailability.isUserResolvableError(status)){
                googleApiAvailability.getErrorDialog(SplashScreenActivity.this, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    public class AsyncCall extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            String msg = "";
            String projectNumber = "1086644501694";
            try {
                if (gcm == null) {
                    InstanceID instanceID = InstanceID.getInstance(getApplicationContext());
                    tokenID = instanceID.getToken(projectNumber, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                }
                DravaApplication.getSnsPreference().register(tokenID);
                getApp().getUserPreference().setGcmTokenId(tokenID);
                Log.e("tag", "splash Device Token for push notification: ---- " + tokenID);
                Log.e("tagis", "done");
            } catch (IOException ex) {
                msg = "Error :" + ex.getMessage();
                Log.e("tagis", "not done -- "+msg);
                // If there is an error, don't just keep trying to register.
                // Require the user to click a button again, or perform
                // exponential back-off.
            }
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Phone State Permission Granted", Toast.LENGTH_SHORT).show();
                    requestContactsReadPermission();
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
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Read Contacts Permission Granted", Toast.LENGTH_SHORT).show();
                    requestCoarseLocationPermission();
                }else{
                    Toast.makeText(this, "Read Contacts Permission Denied", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                        promptSettings("Read Contacts");
                    } else {
                        promptSettings("Read Contacts");
                    }
                }
                break;
            case 3:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Coarse Location Permission Granted", Toast.LENGTH_SHORT).show();
                    requestFineLocationPermission();
                } else{
                    Toast.makeText(this, "Coarse Location Permission Denied", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        promptSettings("Location Permission");
                    } else {
                        promptSettings("Location Permission");
                    }
                }
                break;
            case 4:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Fine Location Permission Granted", Toast.LENGTH_SHORT).show();
                    initSplashScreen();
                } else{
                    Toast.makeText(this, "Fine Location Permission Denied", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        promptSettings("Location Permission");
                    } else {
                        promptSettings("Location Permission");
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
//        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
//        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivityForResult(myAppSettings, PERMISSION_REQ_CODE);
//        this.startActivity(myAppSettings);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }

    private static class MyHandler extends Handler {
        WeakReference<SplashScreenActivity> splash;

        MyHandler(SplashScreenActivity splashScreen) {
            splash = new WeakReference<>(splashScreen);
        }

        @Override
        public void handleMessage(Message msg) {
            SplashScreenActivity activity = splash.get();
            if (activity != null && msg.what == 1 && !activity.isDestroyed) {
                activity.launchNextActivity();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PERMISSION_REQ_CODE) {
            requestPhoneStatePermission();
        }
    }

    public void getSettingData(){
        getApp().getRetrofitInterface().getSettings().enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                ContentParser contentParser = new Gson().fromJson(content,ContentParser.class);
                if (contentParser != null){
                    getApp().getUserPreference().setAboutUs(contentParser.Settings.About);
                    getApp().getUserPreference().setContactUs(contentParser.Settings.ContactUs);
                    getApp().getUserPreference().setPrivacyPolicy(contentParser.Settings.PrivacyPolicy);
                    getApp().getUserPreference().setTermsConditions(contentParser.Settings.TermsCondition);
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
            }
        });
    }
}
