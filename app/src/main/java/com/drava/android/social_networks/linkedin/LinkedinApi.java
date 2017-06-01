package com.drava.android.social_networks.linkedin;

import android.app.Activity;
import android.widget.Toast;

import com.drava.android.base.AppConstants;
import com.drava.android.base.Log;
import com.drava.android.rest.DravaURL;
import com.drava.android.social_networks.SocialNetworksUpdateDataInterface;
import com.linkedin.platform.APIHelper;
import com.linkedin.platform.AccessToken;
import com.linkedin.platform.LISession;
import com.linkedin.platform.LISessionManager;
import com.linkedin.platform.errors.LIApiError;
import com.linkedin.platform.errors.LIAuthError;
import com.linkedin.platform.listeners.ApiListener;
import com.linkedin.platform.listeners.ApiResponse;
import com.linkedin.platform.listeners.AuthListener;
import com.linkedin.platform.utils.Scope;

import org.json.JSONObject;

/**
 * Created by evuser on 26-10-2016.
 */

public class LinkedinApi {
    private static String TAG = LinkedinApi.class.getSimpleName();
    private Activity activity;
    private AccessToken linkedinAccessToken = null;
    private APIHelper linkedinApiHelper;
    private SocialNetworksUpdateDataInterface listener;

    private static Scope buildScope() {
        return Scope.build(Scope.R_BASICPROFILE, Scope.R_EMAILADDRESS, Scope.W_SHARE);
    }

    public void initLinkedin(Activity activity){
        this.activity = activity;
    }

    public void initLinkedinSignIn(){
        LISessionManager.getInstance(activity.getApplicationContext()).init(activity, buildScope(), new AuthListener() {
            @Override
            public void onAuthSuccess() {
                linkedinAccessToken = LISessionManager.getInstance(activity.getApplicationContext()).getSession().getAccessToken();
                makeLinkedinProfileRequest();
            }
            @Override
            public void onAuthError(LIAuthError error) {
                Toast.makeText(activity.getApplicationContext(), "failed " + error.toString(), Toast.LENGTH_LONG).show();
            }
        }, true);
    }

    public void setListener(SocialNetworksUpdateDataInterface listener){
        this.listener = listener;
    }

    public void callLinkedInSignIn(){
        linkedinApiHelper = APIHelper.getInstance(activity);
        if(linkedinAccessToken != null){
            makeLinkedinProfileRequest();
        }else{
            initLinkedinSignIn();
        }
    }

    private void makeLinkedinProfileRequest(){
        linkedinApiHelper.getRequest(activity, DravaURL.LINKED_IN_PEOPLE_PROFILE, new ApiListener() {
            @Override
            public void onApiSuccess(ApiResponse apiResponse) {
                String linkeinId = "", name = "", firstName = "", lastName = "", email = "", pictureUrl = "", gender = "", birthday = "", userName = "";
                if(apiResponse != null){
                    JSONObject jsonObject = apiResponse.getResponseDataAsJson();
                    Log.e(TAG, "LinkedIn Success json response -->  " + jsonObject.toString());
                    if (jsonObject != null) {
                        linkeinId = jsonObject.has("id") ? jsonObject.optString("id") : "";
                        firstName = jsonObject.has("firstName") ? jsonObject.optString("firstName") : "";
                        lastName = jsonObject.has("lastName") ? jsonObject.optString("lastName") : "";
                        userName = jsonObject.has("userName") ? jsonObject.optString("userName") : "";
                        email = jsonObject.has("emailAddress") ? jsonObject.optString("emailAddress") : "";
                        birthday = jsonObject.has("birthDay") ? jsonObject.optString("birthDay") : "";
                        gender = jsonObject.has("gender") ? jsonObject.optString("gender") : "";
                        pictureUrl = jsonObject.has("pictureUrl") ? jsonObject.optString("pictureUrl") : "";
                        listener.socialNetworksUpdateUserDataInterface(AppConstants.LINKEDIN, linkeinId, email, userName, firstName, lastName, birthday, gender, pictureUrl);
                    }
                }
            }

            @Override
            public void onApiError(LIApiError LIApiError) {
                Log.e(TAG, "LinkedIn failure response -->  " + LIApiError.getApiErrorResponse().toString());
            }
        });
    }
}
