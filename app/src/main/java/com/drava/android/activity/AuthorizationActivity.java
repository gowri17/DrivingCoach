package com.drava.android.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drava.android.BuildConfig;
import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.activity.contacts.CustomContactActivity;
import com.drava.android.activity.mentor_mentee.UserInformation;
import com.drava.android.base.BaseActivity;
import com.drava.android.base.Log;
import com.drava.android.parser.ContentParser;
import com.drava.android.parser.Signup;
import com.drava.android.preference.DravaPreference;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.social_networks.SocialNetworksUpdateDataInterface;
import com.drava.android.social_networks.facebook.FacebookApi;
import com.drava.android.social_networks.google_plus.GooglePlusApi;
import com.drava.android.social_networks.instagram.InstagramApi;
import com.drava.android.social_networks.linkedin.LinkedinApi;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.TextUtils;
import com.facebook.internal.CallbackManagerImpl;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;
import com.linkedin.platform.LISessionManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class AuthorizationActivity extends BaseActivity implements View.OnClickListener, SocialNetworksUpdateDataInterface {

    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private LinearLayout layoutFaceBook, layoutGoogle, layoutLinkedin, layoutInstagram;
    private FacebookApi facebookApi;
    private GooglePlusApi googlePlusApi;
    private LinkedinApi linkedinApi;
    private InstagramApi instagramApi;
    private String TAG = AuthorizationActivity.class.getSimpleName();
    private String facebookId, googlePlusId, linkedInId, instagramId;
    private ProgressDialog customProgress;
    private DravaPreference dravaPreference;
    private Signup signup;
    private String deviceId;
    private final int REQ_PHONE_STATE = 2;
    private String socialNetworkType, socialAppId, email, userName, firstName, lastName, birthday, gender, userImg;
    private String instagramUrl;
    private UserInformation userInformation;
    private TextView txtTermsOfUse, txtPrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);
        init();
        setDefaults();
        setupEvents();
    }

    private void init() {
        layoutFaceBook = (LinearLayout) findViewById(R.id.lnr_layout_fb);
        layoutGoogle = (LinearLayout) findViewById(R.id.lnr_layout_google_plus);
        layoutLinkedin = (LinearLayout) findViewById(R.id.lnr_layout_linkedin);
        layoutInstagram = (LinearLayout) findViewById(R.id.lnr_layout_instagram);
        Toolbar toolbar = (Toolbar) findViewById(R.id.default_toolbar);
        txtTermsOfUse = (TextView) findViewById(R.id.txt_terms_of_use);
        txtPrivacyPolicy = (TextView) findViewById(R.id.txt_private_policy);
        setSupportActionBar(toolbar);
        setStatusBarColor();
    }

    private void setDefaults() {
        userInformation = new UserInformation(AuthorizationActivity.this);
        layoutFaceBook.setOnClickListener(this);
        layoutGoogle.setOnClickListener(this);
        layoutLinkedin.setOnClickListener(this);
        layoutInstagram.setOnClickListener(this);
        txtPrivacyPolicy.setOnClickListener(this);
        txtTermsOfUse.setOnClickListener(this);

        facebookApi = new FacebookApi();
        facebookApi.initFacebook(AuthorizationActivity.this);
        facebookApi.setListener(this);

        googlePlusApi = new GooglePlusApi();
        googlePlusApi.initGooglePlus(AuthorizationActivity.this);

        linkedinApi = new LinkedinApi();
        linkedinApi.initLinkedin(AuthorizationActivity.this);
        linkedinApi.setListener(this);

        if(BuildConfig.IS_CUSTOMER_VERSION){    //R.L v1.2
            instagramUrl = "https://api.instagram.com/oauth/authorize/?client_id=" + INSTAGRAM_SDC_CLIENT_ID + "&redirect_uri=" + INSTAGRAM_SDC_REDIRECT_URI + "&response_type=token";
        }else {
            instagramUrl = "https://api.instagram.com/oauth/authorize/?client_id=" + INSTAGRAM_CLIENT_ID + "&redirect_uri=" + INSTAGRAM_REDIRECE_URI + "&response_type=token";
        }
        instagramApi = new InstagramApi(AuthorizationActivity.this);
        instagramApi.setListener(this);

        dravaPreference = getApp().getUserPreference();
        email = "";

        txtTermsOfUse.setPaintFlags(txtTermsOfUse.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtPrivacyPolicy.setPaintFlags(txtPrivacyPolicy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        getSettingData();
    }

    private void setupEvents() {
        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();
        Log.e("Country ", countryCode);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.lnr_layout_fb:
                if (DeviceUtils.isInternetConnected(AuthorizationActivity.this)) {
                    facebookApi.callFacebookSignIn();
                } else {
                    AlertUtils.showAlert(AuthorizationActivity.this, getString(R.string.check_your_internet_connection));
                }
                break;
            case R.id.lnr_layout_google_plus:
                if (DeviceUtils.isInternetConnected(AuthorizationActivity.this)) {
                    googlePlusApi.callGooglePlusSignInIntent();
                } else {
                    AlertUtils.showAlert(AuthorizationActivity.this, getString(R.string.check_your_internet_connection));
                }
                break;

            case R.id.lnr_layout_linkedin:
                if (DeviceUtils.isInternetConnected(AuthorizationActivity.this)) {
                    linkedinApi.initLinkedinSignIn();
                    linkedinApi.callLinkedInSignIn();
                } else {
                    AlertUtils.showAlert(AuthorizationActivity.this, getString(R.string.check_your_internet_connection));
                }
                break;
            case R.id.lnr_layout_instagram:
                if (DeviceUtils.isInternetConnected(AuthorizationActivity.this)) {
                    instagramApi.callInstagramSignIn(instagramUrl);
                } else {
                    AlertUtils.showAlert(AuthorizationActivity.this, getString(R.string.check_your_internet_connection));
                }
                break;
            case R.id.txt_terms_of_use:
                Intent termsIntent = new Intent(AuthorizationActivity.this, WebViewActivity.class);
                termsIntent.putExtra("WEB_URL", getApp().getUserPreference().getTermsConditions());
                termsIntent.putExtra("TITLE", "Term of Use");
                startActivity(termsIntent);
                break;
            case R.id.txt_private_policy:
                Intent policyIntent = new Intent(AuthorizationActivity.this, WebViewActivity.class);
                policyIntent.putExtra("WEB_URL", getApp().getUserPreference().getPrivacyPolicy());
                policyIntent.putExtra("TITLE", "Privacy Policy");
                startActivity(policyIntent);
                break;
        }
    }

    @Override
    public void socialNetworksUpdateUserDataInterface(String socialNetworkType, String id, String email, String userName, String firstName, String lastName, String birthday, String gender, String userImg) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.userImg = userImg;

        Log.d(TAG, "socialNetworksUpdateUserDataInterface: " + email + "=>" + userName + "=>" + lastName + "=>" + firstName + "=>" + birthday);
        facebookId = "";
        googlePlusId = "";
        linkedInId = "";
        instagramId="";

        switch (socialNetworkType) {
            case GOOGLE_PLUS:
                googlePlusId = id;
                break;
            case FACEBOOK:
                facebookId = id;
                break;
            case LINKEDIN:
                linkedInId = id;
                break;
            case INSTAGRAM:
                instagramId = id;
                break;
        }

        String path = getFilesDir().getPath();
        File file = new File(path, "contact.jpg");

        if (!TextUtils.isEmpty(userImg)) {
            Log.e(TAG, "User Image : "+userImg);
            new DownloadTask().execute(userImg, file.getAbsolutePath());
        }else{
            FileOutputStream outputStream = null;
            try {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user);
                outputStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    if(outputStream !=null){
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!TextUtils.isEmpty(facebookId)) {
            dravaPreference.setAuthenticatedVia("FaceBook");
        } else if (!TextUtils.isEmpty(googlePlusId)) {
            dravaPreference.setAuthenticatedVia("Google Plus");
        } else if (!TextUtils.isEmpty(linkedInId)) {
            dravaPreference.setAuthenticatedVia("Linked In");
        } else if (!TextUtils.isEmpty(instagramId)) {
            dravaPreference.setAuthenticatedVia("Instagram");
        } else {
            dravaPreference.setAuthenticatedVia("named");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(AuthorizationActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AuthorizationActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQ_PHONE_STATE);
            } else {
                setDeviceId();
            }
        } else {
            setDeviceId();
        }

        if (!TextUtils.isEmpty(userName) && TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) {
            userName = userName.trim();
            int firstIndex = userName.indexOf(" ");
            this.firstName = userName.substring(0, firstIndex);
            this.lastName = userName.substring(firstIndex + 1, userName.length());
            firstName = this.firstName;
            lastName = this.lastName;
        }

        if(!TextUtils.isNullOrEmpty(this.firstName)){    //some time the lastname might be empty    //R.L v1.1
            if(TextUtils.isNullOrEmpty(this.lastName)){
                this.lastName = this.firstName;
                lastName = this.firstName;
            }
        }

        if(TextUtils.isNullOrEmpty(this.firstName)){
            if(!TextUtils.isNullOrEmpty(userName)){
                this.firstName = userName;
                firstName = userName;
            }
        }

        if (!TextUtils.isEmpty(email)) {
            callUserValidationWebService();
        } else {
            if(dravaPreference.getAuthenticatedVia().equals("Instagram")){      //R.L v1.1
                callUserValidationWebService();
            }else {
                Intent intent = new Intent(AuthorizationActivity.this, CustomContactActivity.class);
                intent.putExtra(FIRST_NAME, firstName);
                intent.putExtra(LAST_NAME, lastName);
                intent.putExtra(FACEBOOK_ID, facebookId);
                intent.putExtra(GOOGLE_PLUS_ID, googlePlusId);
                intent.putExtra(LINKEDIN_ID, linkedInId);
                intent.putExtra(INSTAGRAM_ID, instagramId);
                intent.putExtra(DEVICE_ID, deviceId);
                intent.putExtra(EMAIL, email);
                startActivity(intent);
            }
        }
    }

    private void callUserValidationWebService() {

//        String path = getFilesDir().getPath();
//        File file = new File(path, "contact.jpg");
//        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), file);       // create RequestBody instance from file
//        MultipartBody.Part fbody = MultipartBody.Part.createFormData("Photo", file.getName(), requestFile);
//        HashMap<String, RequestBody> map = new HashMap<>();
//        map.put("FirstName", createPartFromString(firstName));
//        map.put("LastName", createPartFromString(lastName));
//        map.put("Email", createPartFromString(email));
//        map.put("FBId", createPartFromString(facebookId));
//        map.put("LinkedInId", createPartFromString(linkedInId));
//        map.put("GooglePlusId", createPartFromString(googlePlusId));
//        map.put("InstagramId", createPartFromString(instagramId));
//        map.put("Platform", createPartFromString("android"));
//        map.put("DeviceToken", createPartFromString(deviceId));
//        map.put("Token", createPartFromString(getApp().getUserPreference().getGcmTokenId()));
//        map.put("UserData", createPartFromString("userData"));
//        getApp().getRetrofitInterface().registerUser(map,fbody).enqueue(new RetrofitCallback<ResponseBody>() {
//        either the above part or below line
        getApp().getRetrofitInterface().authenticateUser(firstName, lastName, email, facebookId, linkedInId, googlePlusId, instagramId, "android", deviceId, DravaApplication.getSnsPreference().getDeviceToken()).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                signup = new Gson().fromJson(content, Signup.class);
                if (signup != null) {
                    if (signup.getNotifications() != null) {
                        Snackbar.make(findViewById(android.R.id.content), signup.getNotifications()[0].toString(), Snackbar.LENGTH_LONG).show();
                    }
                    if (signup.getMeta() != null) {
                        if (signup.getMeta().getCode().equalsIgnoreCase("201")) {
                            DeviceUtils.hideSoftKeyboard(AuthorizationActivity.this);
//                            AlertUtils.showToast(AuthorizationActivity.this, "Authenticated Existing user");
                            if (signup.getLogin() != null) {
                                getApp().getUserPreference().setAccessToken(signup.getLogin().getAccessToken());
                                getApp().getUserPreference().setUserLoggedIn(true);
                                Log.e(TAG, "Storing User Infos in Preference");
                                userInformation.callUserInformationWebService(AUTHORIZATION_ACTIVITY);
                            } else {
                                AlertUtils.showAlert(AuthorizationActivity.this, "Access Token is not Valid");
                            }
                        } else if (signup.getMeta().getCode().equalsIgnoreCase("1000")) {
                            DeviceUtils.hideSoftKeyboard(AuthorizationActivity.this);
                            getApp().getUserPreference().setFirstName(firstName);
                            getApp().getUserPreference().setLastName(lastName);
                            getApp().getUserPreference().setFbId(facebookId);
                            getApp().getUserPreference().setGplusId(googlePlusId);
                            getApp().getUserPreference().setLinkedinId(linkedInId);
                            getApp().getUserPreference().setInstagramId(instagramId);
                            getApp().getUserPreference().setDeviceId(deviceId);

                            Intent customPhoneIntent = new Intent(AuthorizationActivity.this, CustomContactActivity.class);
                            customPhoneIntent.putExtra(FIRST_NAME, firstName);      //R.L v1.1
                            customPhoneIntent.putExtra(LAST_NAME, lastName);
                            customPhoneIntent.putExtra(FACEBOOK_ID, facebookId);
                            customPhoneIntent.putExtra(GOOGLE_PLUS_ID, googlePlusId);
                            customPhoneIntent.putExtra(LINKEDIN_ID, linkedInId);
                            customPhoneIntent.putExtra(INSTAGRAM_ID, instagramId);
                            customPhoneIntent.putExtra(DEVICE_ID, deviceId);
                            customPhoneIntent.putExtra(EMAIL, email);
                            startActivity(customPhoneIntent);
                        } else if (signup.getMeta().getCode().equalsIgnoreCase("1005")) {
                            AlertUtils.showAlert(AuthorizationActivity.this, getResources().getString(R.string.app_name), signup.getMeta().getErrorMessage());
                        } else if (signup.getMeta().getCode().equalsIgnoreCase("1")) {
                            AlertUtils.showAlert(AuthorizationActivity.this, getResources().getString(R.string.app_name), signup.getMeta().getErrorMessage());
                        }
                    }
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == googlePlusApi.RC_SIGN_IN) {
            if (/*resultCode == RESULT_OK && */data != null) {
                customProgress = new ProgressDialog(AuthorizationActivity.this);
                customProgress.setCancelable(false);
                customProgress.show();

                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
            }
        } else if (resultCode == RESULT_OK && data != null && CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode() == requestCode) {
            facebookApi.callbackManager.onActivityResult(requestCode, resultCode, data);
        } else {
            LISessionManager.getInstance(getApplicationContext()).onActivityResult(this, requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();
            String gPlusId, email, userName, userImg, birthday;

            try {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(googlePlusApi.googleApiClient);

                Log.d(TAG, "currentPerson: " + currentPerson);
//            Log.d(TAG, "Plus.PeopleApi.getCurrentPerson(googlePlusApi.googleApiClient): " + Plus.PeopleApi.getCurrentPerson(googlePlusApi.googleApiClient));
                Log.d(TAG, "acct.getId(): " + acct.getId());
                Log.d(TAG, "acct.getEmail(): " + acct.getEmail());
                Log.d(TAG, "acct.getDisplayName(): " + acct.getDisplayName());
                Log.d(TAG, "acct.getPhotoUrl(): " + acct.getPhotoUrl());

                if (currentPerson != null) {
                    Log.d(TAG, "currentPerson.getBirthday()" + currentPerson.getBirthday());
                    Log.d(TAG, "currentPerson.getGender()" + currentPerson.getGender());
                }
            }catch (Exception e){}
            gPlusId = (acct.getId() != null) ? acct.getId().toString() : "";
            email = (acct.getEmail() != null) ? acct.getEmail().toString() : "";
            userName = (acct.getDisplayName() != null) ? acct.getDisplayName().toString() : "";
            userImg = (acct.getPhotoUrl() != null) ? acct.getPhotoUrl().toString() : "";
//             birthday = (currentPerson.getBirthday() != null) ? currentPerson.getBirthday().toString() : "";
//             String gender = (currentPerson.getGender() != null) ? currentPerson.getBirthday().toString() : ""
            socialNetworksUpdateUserDataInterface(GOOGLE_PLUS, gPlusId, email, userName, "", "", "", "", userImg);
            googlePlusApi.callGooglePlusSignOut();

        } else {
            android.util.Log.d(TAG, "handleSignInResult: " + result.getStatus().getStatusMessage());
        }

        if (!isFinishing() && customProgress.isShowing()) {
            customProgress.dismiss();
        }
    }

    private void setDeviceId() {
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = tm.getDeviceId();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PHONE_STATE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setDeviceId();

            }
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
                promptSettings("Phone Status");
            } else {
                promptSettings("Phone Status");
            }
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
        builder.setNegativeButton("Cancel", null);
        builder.setCancelable(false);
        builder.show();
    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + this.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(myAppSettings);
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
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


    private class DownloadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            String sourceUrl = params[0];
            String fileLocalPath = params[1];
            File file = new File(fileLocalPath);

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sourceUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(file);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    output.write(data, 0, count);
                }
                output.flush();
                input.close();
                output.close();
                return fileLocalPath;
            } catch (Exception e) {
                e.printStackTrace();
                if (file.exists()) {
                    file.delete();
                }
                return null;
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ignored) {
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        protected void onPostExecute(String path) {
            super.onPostExecute(path);
        }
    }
}
