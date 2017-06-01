package com.drava.android.social_networks.twitter;

import android.app.Activity;

import com.drava.android.base.AppConstants;
import com.drava.android.base.Log;
import com.drava.android.social_networks.SocialNetworksUpdateDataInterface;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import io.fabric.sdk.android.Fabric;

public class TwitterApi {

    private static String TAG = TwitterApi.class.getSimpleName();
    private Activity activity;
    public TwitterAuthClient twitterAuthClient;
    private TwitterSession session;
    private String twitterId, screenName, userName, userImage, location, description,email;
    private int timeZone;
    private SocialNetworksUpdateDataInterface mListener;

    public void initTwitter(Activity activity) {
        this.activity = activity;
        twitterAuthClient = new TwitterAuthClient();
    }

    public void setListener(SocialNetworksUpdateDataInterface listener) {
        mListener = listener;
    }

    public void callTwitterSignIn() {

        Log.d(TAG, "callTwitterSignIn");

        twitterAuthClient.authorize(activity, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                session = Twitter.getSessionManager().getActiveSession();
                Twitter.getApiClient(session).getAccountService().verifyCredentials(true, false, new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        Log.d(TAG, "Success  -- Result: " + result);
                        if (result != null && result.data != null) {
                            Log.d(TAG, "Success  -- user detail: " + result.data.toString());
                            User user = result.data;
                            twitterId = String.valueOf(user.id);
                            screenName = user.screenName;
                            userName = user.name;
                            location = user.location;
                            userImage = user.profileImageUrl.replace("_normal", "");
                            description = user.description;
                            timeZone = user.followersCount;
                            email = "";
                            twitterAuthClient.requestEmail(session, new Callback<String>() {
                                @Override
                                public void success(Result<String> result) {
                                    // Do something with the result, which provides the email address
                                    android.util.Log.d(TAG, "success: Email=>"+result.data);
                                    email = result.data;
                                    mListener.socialNetworksUpdateUserDataInterface(AppConstants.TWITTER, twitterId, email, screenName, userName, "", "", "", userImage);
                                }

                                @Override
                                public void failure(TwitterException exception) {
                                    // Do something on failure
                                    android.util.Log.d(TAG, "failure: Email=>"+exception.toString());
                                    mListener.socialNetworksUpdateUserDataInterface(AppConstants.TWITTER, twitterId, email, screenName, userName, "", "", "", userImage);
                                }
                            });

//                            mListener.socialNetworksUpdateUserDataInterface(AppConstants.TWITTER, twitterId, email, screenName, userName, "", "", "", userImage);
                        } else {
                            Log.d(TAG, "Success  -- Result is null: ");
                        }
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Log.d(TAG, "getApiClient failure: " + e);
                    }
                });

            }

            @Override
            public void failure(TwitterException e) {
                Log.d(TAG, "Login with Twitter failure: " + e);
            }
        });
    }

    public void callTwitterSignOut() {
        Twitter.getInstance();
        Twitter.logOut();
    }
}
