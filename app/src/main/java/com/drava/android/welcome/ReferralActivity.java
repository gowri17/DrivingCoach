package com.drava.android.welcome;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.activity.AuthorizationActivity;
import com.drava.android.activity.mentor_mentee.UserInformation;
import com.drava.android.base.AppConstants;
import com.drava.android.base.BaseActivity;
import com.drava.android.base.Log;
import com.drava.android.parser.Signup;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.TextUtils;
import com.google.gson.Gson;

import java.io.File;
import java.sql.Ref;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;
import retrofit2.Call;

/**
 * Created by admin on 10/19/2016.
 */

public class ReferralActivity extends BaseActivity implements AppConstants {
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private TextView txtContinue, txtCode, txtMentorDesc, txtTitleCode;
    private ImageView imgCode;
    private EditText edtReferalCode;
    private String referalCode = "";
    private String firstName, lastName, email, phoneNumber, facebookId, linkedInId, googlePlusId, instagramId, deviceId, userType;
    private UserInformation userInformation;
    private ProgressDialog progDailog;
    private String TAG = ReferralActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referal);
        init();
        setupDefaults();
        setupEvents();
    }

    private void init() {
        txtTitleCode = (TextView) findViewById(R.id.txt_title_code);
        txtContinue = (TextView) findViewById(R.id.txt_continue);
        txtCode = (TextView) findViewById(R.id.txt_code);
        txtMentorDesc = (TextView) findViewById(R.id.txt_mentor_desc);
        imgCode = (ImageView) findViewById(R.id.img_code);
        edtReferalCode = (EditText) findViewById(R.id.edt_referal_code);
    }

    private void setupDefaults() {
        userInformation = new UserInformation(ReferralActivity.this);
        setStatusBarColor();
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTOR)) {
            setToolbar(getString(R.string.referral_code));
            txtTitleCode.setText(getResources().getString(R.string.let_your_referral));
            imgCode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.gift));
            txtMentorDesc.setVisibility(View.GONE);
            txtCode.setText(getResources().getString(R.string.enter_referal_code));


        } else if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
            setToolbar(getString(R.string.mentor_code));
            txtTitleCode.setText(getResources().getString(R.string.link_with_your_mentor));
            imgCode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.mentor_code));
            txtMentorDesc.setVisibility(View.VISIBLE);
            txtCode.setText(getResources().getString(R.string.enter_mentor_code));
        }
        edtReferalCode.setImeOptions(EditorInfo.IME_ACTION_DONE);

        firstName = getApp().getUserPreference().getFirstName();
        lastName = getApp().getUserPreference().getLastName();
        if(!TextUtils.isNullOrEmpty(firstName)){    //some time the lastname might be empty     //R.L v1.1
            if(TextUtils.isNullOrEmpty(lastName)){
                lastName = firstName;
            }
        }
        email = getApp().getUserPreference().getEmail();
        facebookId = getApp().getUserPreference().getFbId();
        linkedInId = getApp().getUserPreference().getLinkedinId();
        googlePlusId = getApp().getUserPreference().getGplusId();
        instagramId = getApp().getUserPreference().getInstagramId();
        userType = getApp().getUserPreference().getMentorOrMentee().equalsIgnoreCase(MENTOR) ? "1" : "2";
        deviceId = getApp().getUserPreference().getDeviceId();
        phoneNumber = getApp().getUserPreference().getPhoneNumber();

        progDailog = new ProgressDialog(ReferralActivity.this);
        progDailog.setMessage("Loading ... ");
        progDailog.setIndeterminate(false);
        progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDailog.setCancelable(false);
    }

    private void setupEvents() {
        txtContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeviceUtils.hideSoftKeyboard(ReferralActivity.this);
                if (edtReferalCode != null) {
                    referalCode = edtReferalCode.getText().toString().trim();
                    if (!TextUtils.isEmpty(referalCode)) {
                        if(null != progDailog && !progDailog.isShowing()) {
                            progDailog.show();
                        }
                        registerNewUserWebService();
                    } else {
                        AlertUtils.showAlert(ReferralActivity.this, getResources().getString(R.string.str_enter_referal), new DialogInterface.OnClickListener() {   //R.L
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        }, false);

                    }
                }
            }
        });

        edtReferalCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    txtContinue.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (getApp().getUserPreference().getMentorOrMentee().equalsIgnoreCase(MENTOR)) {        //R.L v1.1
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_skip, menu);
        return true;
//        } else
//            return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_skip) {
            registerNewUserWebService();
        }
        return super.onOptionsItemSelected(item);
    }

    private void registerNewUserWebService() {

        String path = getFilesDir().getPath();
        File file = new File(path, "contact.jpg");
        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), file);
        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part fbody = MultipartBody.Part.createFormData("Photo", file.getName(), requestFile);

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("FirstName", createPartFromString(firstName));
        map.put("LastName", createPartFromString(lastName));
        map.put("Email", createPartFromString(email));
        map.put("FBId", createPartFromString(facebookId));
        map.put("LinkedInId", createPartFromString(linkedInId));
        map.put("GooglePlusId", createPartFromString(googlePlusId));
        map.put("InstagramId", createPartFromString(instagramId));
        map.put("UserType", createPartFromString(userType));
        map.put("Platform", createPartFromString("android"));
        map.put("DeviceToken", createPartFromString(deviceId));
        map.put("Token", createPartFromString(DravaApplication.getSnsPreference().getDeviceToken()));
        map.put("UserData", createPartFromString("userData"));
        map.put("PhoneNumber", createPartFromString(phoneNumber));
        map.put("ReferralCode", createPartFromString(referalCode));
        //Token=e3uN8SKI27Q%3AAPA91bEje4l90OjBtX_qXI2XM19i0iTzdUOqCyKu0F26tBoHCrVDEIQHgf2vrlfD3LJGMR7M94ilGnVeghUkCR2ipuvosAn7QiGGr_SWaCBDGZjCF6gs8e-rHztakJL9LT76xnatRhrs
        // fdRZTiSuxJY:APA91bHgVVoicuH9eTfhzz_3wQaqCMXJVNRct50h4GfISiHYLA45wwBwJoYF8BojFEkKF73hcSfY7Q_6Bvj_cgWImU_34SdtVK0IFhREOZts1DqfIGe7wDuLXYxchGSyBJrIRrKuY3pJ
        Log.d("Retrofit", "getApp().getUserPreference().getGcmTokenId(): "+DravaApplication.getSnsPreference().getDeviceToken()+" -- createPartFromString(getApp().getUserPreference().getGcmTokenId(): "+createPartFromString(DravaApplication.getSnsPreference().getDeviceToken()));
        getApp().getRetrofitInterface().registerUser(map,fbody)
                .enqueue(new RetrofitCallback<ResponseBody>() {
                    @Override
                    public void onSuccessCallback(Call<ResponseBody> call, String content) {
                        super.onSuccessCallback(call, content);

                        if (null != progDailog && progDailog.isShowing()) {
                            progDailog.dismiss();
                        }

                        Signup signup = new Gson().fromJson(content, Signup.class);
                        if (signup != null) {
                            if (signup.getNotifications() != null) {
//                                Snackbar.make(findViewById(android.R.id.content), signup.getNotifications()[0].toString(), Snackbar.LENGTH_LONG).show();
                            }
                            if (signup.getMeta() != null) {
                                if (signup.getMeta().getCode().equalsIgnoreCase("201")) {
//                                    AlertUtils.showToast(ReferralActivity.this, "New User Registration");
                                    if (signup.getLogin() != null) {
                                        getApp().getUserPreference().setAccessToken(signup.getLogin().getAccessToken());
                                        getApp().getUserPreference().setUserLoggedIn(true);
                                        Log.e(TAG, "Storing User Infos in Preference");
                                        userInformation.callUserInformationWebService(REFERRAL_ACTIVITY);

                                    } else {
                                        AlertUtils.showAlert(ReferralActivity.this, "Access Token is not Valid");
                                    }
                                } else {
                                    showUserRegisterErrorMessage(signup.getMeta().getErrorMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                        super.onFailureCallback(call, t, message, code);
                        if (null != progDailog && progDailog.isShowing()) {
                            progDailog.dismiss();
                        }
                        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void showUserRegisterErrorMessage(String message) {
        AlertUtils.showAlert(ReferralActivity.this, getResources().getString(R.string.app_name), message);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // finishAffinity();
        // startActivity(new Intent(ReferralActivity.this, AuthorizationActivity.class));
        Intent intent = new Intent(ReferralActivity.this, AuthorizationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }

}
