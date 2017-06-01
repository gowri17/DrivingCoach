package com.drava.android.social_networks.google_plus;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.drava.android.base.AppConstants;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;

public class GooglePlusApi implements GoogleApiClient.ConnectionCallbacks, AppConstants {
    private static String TAG = GooglePlusApi.class.getSimpleName();
    private Activity activity;
    private GoogleApiAvailability google_api_availability;
    private GoogleSignInOptions googleSignInOptions;
    public GoogleApiClient googleApiClient;
    public int RC_SIGN_IN = 1000;
    public int RC_SIGN_IN_ERROR = 1001;
    public int GOOGLEPLUS_REQ_CODE = 6;

    public void initGooglePlus(FragmentActivity activity) {
        this.activity = activity;
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestScopes(new Scope(Scopes.PROFILE)).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(activity).enableAutoManage(activity, onConnectionFailedListener)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).addApi(Plus.API).build();
        googleApiClient.stopAutoManage(activity);
    }

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult result) {
            if (!result.hasResolution()) {
                google_api_availability.getErrorDialog(activity, result.getErrorCode(), RC_SIGN_IN_ERROR).show();
                return;
            }
               callGooglePlusSignInIntent();
        }
    };

    public void callGooglePlusSignInIntent() {
       Log.d(TAG, "callGooglePlusSignInIntent");
        googleApiClient.connect();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void callGooglePlusSignOut() {
        if (googleApiClient.isConnected()) {
            Log.d(TAG, "callGooglePlusSignOut");
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            Auth.GoogleSignInApi.signOut(googleApiClient);
        }
    }

    public void clearAccount(){
        if(googleApiClient != null && googleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(googleApiClient);
            Auth.GoogleSignInApi.signOut(googleApiClient);
            googleApiClient.disconnect();
            googleApiClient.connect();
        }
    }

    public void callShareContent(Uri filePath) {
        Intent shareIntent = new PlusShare.Builder(activity)
                .setType("image/png")
                .addStream(filePath)
                .setText("Hello from AndroidSRC.net").getIntent();
        activity.startActivityForResult(shareIntent, GOOGLEPLUS_REQ_CODE);
    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Log.d(TAG, "onConnectionSuspended");
        googleApiClient.connect();
    }
}
