package com.drava.android.social_networks.facebook;

import android.app.Activity;
import android.os.Bundle;

import com.drava.android.base.AppConstants;
import com.drava.android.base.Log;
import com.drava.android.social_networks.SocialNetworksUpdateDataInterface;
import com.drava.android.utils.TextUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class FacebookApi {

    private static String TAG = FacebookApi.class.getSimpleName();
    private Activity activity;
    public CallbackManager callbackManager;
    private String fbId, email, firstName, lastName, userName, birthday, gender /*-- 1 -> MALE, 2 -> FEMALE*/, fbImg;
    private SocialNetworksUpdateDataInterface mListener;

    public void initFacebook(Activity activity) {
        this.activity = activity;
        FacebookSdk.sdkInitialize(activity);
        callbackManager = CallbackManager.Factory.create();
    }

    public void setListener(SocialNetworksUpdateDataInterface listener) {
        mListener = listener;
    }

    public void callFacebookSignIn() {
        Log.d(TAG, "callFacebookSignIn");

        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "email", "user_birthday"));

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                System.out.println("Success");
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject json, GraphResponse response) {
                        if (response.getError() != null) {
                            // handle error
                            System.out.println("ERROR");
                        } else {
                            System.out.println("Success");
                            try {

                                String jsonresult = String.valueOf(json);
                                System.out.println("JSON Result"+jsonresult);

                                Log.d(TAG, "loginResult.getAccessToken(): "+loginResult.getAccessToken().getToken());
                                Log.d(TAG, "access token: "+ AccessToken.getCurrentAccessToken().getToken());

                                if(json.has("email") && json.getString("email") != null) {
                                    email = json.getString("email");
                                    Log.d(TAG, "str_email: "+email);
                                }
                                if(json.has("id") && json.getString("id") != null) {
                                    fbId = json.getString("id");
                                    // String fbImg = "http://graph.facebook.com/"+json.optString("id")+"/picture";
                                    Log.d(TAG, "str_id: "+fbId);
                                }
                                if(json.has("first_name") && json.getString("first_name") != null) {
                                    firstName = json.getString("first_name");
                                    Log.d(TAG, "str_firstname: "+firstName);
                                }
                                if(json.has("last_name") && json.getString("last_name") != null) {
                                    lastName = json.getString("last_name");
                                    Log.d(TAG, "str_lastname: "+lastName);
                                }
                                userName = firstName+lastName;
                                if(json.has("birthday") && json.getString("birthday") != null) {
                                    try {
                                        birthday = json.getString("birthday");
                                        SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                        Date formatDate = format1.parse(birthday);
                                        birthday = format2.format(formatDate);
                                    }catch(Exception e){
                                        Log.d(TAG, "str_birthday Exception: "+e.toString());
                                    }
                                    Log.d(TAG, "str_birthday: "+birthday);
                                }
                                if(json.has("gender") && json.getString("gender") != null) {
                                    gender = json.getString("gender");
                                    if(!TextUtils.isNullOrEmpty(gender) && gender.equalsIgnoreCase("female")) {
                                        gender = "2";
                                    }
                                    else{
                                        gender = "1";
                                    }
                                    Log.d(TAG, "str_gender: "+gender);
                                }
                                if(json.has("picture") && json.getString("picture") != null && json.getJSONObject("picture").has("data") && json.getJSONObject("picture").getJSONObject("data") != null && json.getJSONObject("picture").getJSONObject("data").has("url") && json.getJSONObject("picture").getJSONObject("data").getString("url") != null) {
                                    fbImg = json.getJSONObject("picture").getJSONObject("data").getString("url");
                                    Log.d(TAG, " fbImg: " + fbImg);
                                }

                                //if(activity.getLocalClassName().toString().equalsIgnoreCase(LoginActivity.class.getSimpleName())) {
                                //((LoginActivity) activity).setUserNameOrEmail(((LoginActivity) activity).FACEBOOK, email, userName, fbId);
                                // }

                                mListener.socialNetworksUpdateUserDataInterface(AppConstants.FACEBOOK, fbId, email, userName, firstName, lastName, birthday, gender, fbImg);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }


                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, name, link, gender, birthday, email, first_name, last_name, location, locale, timezone, picture.type(large), cover");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG,"On cancel");
                if(AccessToken.getCurrentAccessToken() != null){
                    LoginManager.getInstance().logOut();
                    callFacebookSignIn();
                }
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG,error.toString());
                if(error instanceof FacebookAuthorizationException){
                    if(AccessToken.getCurrentAccessToken() != null){
                        LoginManager.getInstance().logOut();
                        callFacebookSignIn();
                    }
                }
            }
        });
    }

    public void callFacebookSignOut() {
        Log.d(TAG, "callFacebookSignOut");
        LoginManager.getInstance().logOut();
    }
}
