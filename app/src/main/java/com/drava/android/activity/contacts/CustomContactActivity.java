package com.drava.android.activity.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.activity.mentor_mentee.UserInformation;
import com.drava.android.base.BaseActivity;
import com.drava.android.base.Log;
import com.drava.android.parser.Signup;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.ui.DravaEditText;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.TextUtils;
import com.drava.android.welcome.ProfileActivity;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class CustomContactActivity extends BaseActivity {
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";
    private EditText edtPhoneNumber, edtCountryCode, edtEmailID;
    private Button btnContinue;
    private String userPhoneNumber;
    private String email;
    private RelativeLayout rlvEmailLayout;
    private final String TAG = CustomContactActivity.class.getName();
    private boolean isEmailIdExists;
    private String firstName, lastName, facebookId, linkedInId, googlePlusId, instagramId, deviceId;
    private UserInformation userInformation;
    private String standardCardMask = "## ### ####";
    //    private String standardCardMask = "### ### ####";
    private TextWatcher cardNumberTextWatcher;
    private InputFilter[] fArray = new InputFilter[1];
    private int MAX_LENGTH_STANDARD = 9;
    //    private int MAX_LENGTH_STANDARD = 10;
    private int selection = 0, starter = 0, beforer = 0;
    private String cardName = "", cardNumber = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_contact);
        init();
        setDefaults();
        setupEvents();
    }

    private void init(){
        edtPhoneNumber = (DravaEditText)findViewById(R.id.edt_phone);
        btnContinue = (Button)findViewById(R.id.btn_continue);
        edtCountryCode = (EditText)findViewById(R.id.txt_code);
        edtEmailID = (EditText)findViewById(R.id.edt_email);
        rlvEmailLayout = (RelativeLayout)findViewById(R.id.rlv_email_layout);
    }

    private void setDefaults(){
        if(getIntent() != null) {
            email = getIntent().getStringExtra(EMAIL);
            if(TextUtils.isEmpty(email)){
                isEmailIdExists = false;
                firstName = getIntent().getStringExtra(FIRST_NAME);
                lastName = getIntent().getStringExtra(LAST_NAME);
                facebookId = getIntent().getStringExtra(FACEBOOK_ID);
                googlePlusId = getIntent().getStringExtra(GOOGLE_PLUS_ID);
                linkedInId = getIntent().getStringExtra(LINKEDIN_ID);
                instagramId = getIntent().getStringExtra(INSTAGRAM_ID);
                deviceId = getIntent().getStringExtra(DEVICE_ID);
            }else{
                isEmailIdExists = true;
            }
        }
        edtCountryCode.setEnabled(false);
        setStatusBarColor();
        edtPhoneNumber.requestFocus();
        userInformation = new UserInformation(CustomContactActivity.this);
    }

    private void setupEvents(){

        cardNumberTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String mask = "";
                mask = standardCardMask;
                if (start < mask.length()) {
                    if(s.length()>0) {      // R.L v1.2
                        if (s.charAt(s.length() - 1) != ' ') {
                            selection = start + count;
                            starter = start;
                            beforer = before;
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()>0) {      // R.L v1.2
                    if (s.charAt(s.length() - 1) != ' ') {
                        String tmp = s.toString();
                        int cardMaxLength = 0;
                        fArray[0] = new InputFilter.LengthFilter(11);
//                      fArray[0] = new InputFilter.LengthFilter(12);
                        edtPhoneNumber.setFilters(fArray);
                        cardMaxLength = MAX_LENGTH_STANDARD + 3;
                        cardNumber = validateCreditCardFormat(s, standardCardMask);
                        edtPhoneNumber.removeTextChangedListener(cardNumberTextWatcher);
                        edtPhoneNumber.setText(cardNumber);
                        edtPhoneNumber.setSelection(selection);
                        edtPhoneNumber.addTextChangedListener(cardNumberTextWatcher);
                    }else{
                        edtPhoneNumber.setText(edtPhoneNumber.getText().toString().trim());
                    }
                }
            }
        };

        edtPhoneNumber.addTextChangedListener(cardNumberTextWatcher);



        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPhoneNumber = edtPhoneNumber.getText().toString();
                if(validatePhoneNumber()){
                    if(isEmailIdExists)
                    {
                        getApp().getUserPreference().setEmail(email);
                        getApp().getUserPreference().setPhoneNumber(TextUtils.getOnlyDigits(userPhoneNumber));
                        userPhoneNumber = "+60"+userPhoneNumber;
//                        userPhoneNumber = "+91"+userPhoneNumber;
                        userPhoneNumber = userPhoneNumber.replaceAll("\\s+","");
                        startActivity(new Intent(CustomContactActivity.this, ProfileActivity.class));
                    }
                    else
                    {
//                        String path = getFilesDir().getPath();
//                        File file = new File(path, "contact.jpg");
//                        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpg"), file);       // create RequestBody instance from file
//                        MultipartBody.Part fbody = MultipartBody.Part.createFormData("Photo", file.getName(), requestFile);
//                        HashMap<String, RequestBody> map = new HashMap<>();
//                        map.put("FirstName", createPartFromString(firstName));
//                        map.put("LastName", createPartFromString(lastName));
//                        map.put("Email", createPartFromString(email));
//                        map.put("FBId", createPartFromString(facebookId));
//                        map.put("LinkedInId", createPartFromString(linkedInId));
//                        map.put("GooglePlusId", createPartFromString(googlePlusId));
//                        map.put("InstagramId", createPartFromString(instagramId));
//                        map.put("Platform", createPartFromString("android"));
//                        map.put("DeviceToken", createPartFromString(deviceId));
//                        map.put("Token", createPartFromString(getApp().getUserPreference().getGcmTokenId()));
//                        map.put("UserData", createPartFromString("userData"));
//                        getApp().getRetrofitInterface().registerUser(map,fbody).enqueue(new RetrofitCallback<ResponseBody>() {
//                        either the above part or below line
                        getApp().getRetrofitInterface().authenticateUser(firstName, lastName, email, facebookId, linkedInId, googlePlusId, instagramId, "android", deviceId, DravaApplication.getSnsPreference().getDeviceToken()).enqueue(new RetrofitCallback<ResponseBody>() {
                            @Override
                            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                                super.onSuccessCallback(call, content);
                                Signup signup = new Gson().fromJson(content, Signup.class);
                                if (signup != null) {
                                    getApp().getUserPreference().setFirstName(firstName);
                                    getApp().getUserPreference().setLastName(lastName);
                                    getApp().getUserPreference().setFbId(facebookId);
                                    getApp().getUserPreference().setGplusId(googlePlusId);
                                    getApp().getUserPreference().setLinkedinId(linkedInId);
                                    getApp().getUserPreference().setInstagramId(instagramId);
                                    getApp().getUserPreference().setDeviceId(deviceId);
                                    getApp().getUserPreference().setEmail(email);
                                    getApp().getUserPreference().setPhoneNumber(TextUtils.getOnlyDigits(userPhoneNumber));

                                    if (signup.getNotifications() != null) {
                                        Snackbar.make(findViewById(android.R.id.content), signup.getNotifications()[0].toString(), Snackbar.LENGTH_LONG).show();
                                    }
                                    if (signup.getMeta() != null) {
                                        if (signup.getMeta().getCode().equalsIgnoreCase("201")) {
                                            DeviceUtils.hideSoftKeyboard(CustomContactActivity.this);
//                                            AlertUtils.showToast(CustomContactActivity.this, "Authenticated Existing user");
                                            if(signup.getLogin() != null) {
                                                getApp().getUserPreference().setAccessToken(signup.getLogin().getAccessToken());
                                                getApp().getUserPreference().setUserLoggedIn(true);
                                                Log.e(TAG, "Storing User Infos in Preference");
                                                userInformation.callUserInformationWebService(CUSTOM_CONTACT_ACTIVITY);
                                            }else{
                                                AlertUtils.showToast(CustomContactActivity.this, "Access Token is not Valid");
                                            }
                                        }
                                        else if(signup.getMeta().getCode().equalsIgnoreCase("1000")){
                                            DeviceUtils.hideSoftKeyboard(CustomContactActivity.this);
//                                            AlertUtils.showToast(CustomContactActivity.this, "New User Registration");
                                            Intent profileIntent = new Intent(CustomContactActivity.this, ProfileActivity.class);
                                            startActivity(profileIntent);
                                        }
                                        else if(signup.getMeta().getCode().equalsIgnoreCase("1005")){
                                            AlertUtils.showBackAlert(CustomContactActivity.this, getResources().getString(R.string.app_name), signup.getMeta().getErrorMessage());
                                        }
                                        else if(signup.getMeta().getCode().equalsIgnoreCase("1")){
                                            AlertUtils.showBackAlert(CustomContactActivity.this, getResources().getString(R.string.app_name), signup.getMeta().getErrorMessage());
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
                    Log.e("PhoneNumber", userPhoneNumber);
                }
            }
        });

        TextView.OnEditorActionListener imeListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    btnContinue.performClick();	//perform user action
                    return true;
                }
                return false;
            }
        };

        if(!TextUtils.isValidEmail(email)){
            edtPhoneNumber.setImeOptions(EditorInfo.IME_ACTION_NONE);
            rlvEmailLayout.setVisibility(View.VISIBLE);
            edtEmailID.setImeOptions(EditorInfo.IME_ACTION_DONE);
            edtEmailID.setImeActionLabel("Done", KeyEvent.KEYCODE_ENTER);
            edtEmailID.setOnEditorActionListener(imeListener);
        }else{
            Log.e(TAG, "User e-mail id : "+email);
            edtPhoneNumber.setImeOptions(EditorInfo.IME_ACTION_DONE);
            edtPhoneNumber.setImeActionLabel("Done", KeyEvent.KEYCODE_ENTER);
            edtPhoneNumber.setOnEditorActionListener(imeListener);
        }
    }

    private String validateCreditCardFormat(Editable s, String mask) {
        int position = starter - beforer;
        String typedText = "";
        if (position >= 0) {
            if (s.length() <= mask.length()) {
                if (mask.charAt(position) == '#') {
                    typedText = s.toString().trim();
                } else {
                    if (s.charAt(position) != ' ') {
                        typedText = s.subSequence(0, position) + " " + s.toString().substring(position);
                        selection += 1;
                    } else {
                        typedText = s.toString().trim();
                    }
                }
            }
        } else {
            if (s.length() > 0 && s.length() <= mask.length()) {
                typedText = s.toString();
            }
        }

        return formatText(typedText, mask);
    }

    private String formatText(String typedText, String mask) {
        StringBuilder formattedText = new StringBuilder(typedText.replace(" ", ""));
        for (int i = 0; i < typedText.length(); i++) {
            if (mask.charAt(i) != '#') {
                formattedText.insert(i, ' ');
            }
        }
        return formattedText.toString();
    }

    private boolean validatePhoneNumber(){
        if(TextUtils.isEmpty(userPhoneNumber)){
            AlertUtils.showAlert(CustomContactActivity.this, "Enter Phone Number");
            return false;
        }else if(userPhoneNumber.length() != 11){
//        }else if(userPhoneNumber.length() != 12){
            AlertUtils.showAlert(CustomContactActivity.this, "Invalid Phone Number");
            return false;
        }else if(rlvEmailLayout.getVisibility() == View.VISIBLE) {
            email = edtEmailID.getText().toString();
            if (!TextUtils.isValidEmail(email)) {
                AlertUtils.showAlert(CustomContactActivity.this, "Invalid e-mail id");
                return false;
            }
        }
        return true;
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), descriptionString);
    }

}
