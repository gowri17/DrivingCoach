package com.drava.android.activity.mentor_mentee;

import android.content.Context;
import android.content.Intent;

import com.drava.android.DravaApplication;
import com.drava.android.activity.HomeActivity;
import com.drava.android.activity.contacts.GetContactsActivity;
import com.drava.android.parser.UserInformationParser;
import com.drava.android.preference.DravaPreference;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.welcome.InviteActivity;
import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by evuser on 03-11-2016.
 */

public class UserInformation
{
    private Context context;
    private DravaPreference userPreference;

    public UserInformation(Context context){
        this.context = context;
        userPreference = new DravaPreference(context);
    }

    public void callUserInformationWebService(final String fromActivity){
        DravaApplication.getApp().getRetrofitInterface().getUserInformation().enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
//                Log.e("User Info :", content);
                UserInformationParser userInformationParser = new Gson().fromJson(content, UserInformationParser.class);
                if(userInformationParser.getMeta().code == 200){
                    userPreference.setUserId(userInformationParser.getUserDetails().UserId);
                    userPreference.setFirstName(userInformationParser.getUserDetails().FirstName);
                    userPreference.setLastName(userInformationParser.getUserDetails().LastName);
                    userPreference.setEmail(userInformationParser.getUserDetails().Email);
                    userPreference.setFbId(userInformationParser.getUserDetails().FBId);
                    userPreference.setGplusId(userInformationParser.getUserDetails().GooglePlusId);
                    userPreference.setLinkedinId(userInformationParser.getUserDetails().LinkedInId);
                    userPreference.setInstagramId(userInformationParser.getUserDetails().InstagramId);
                    if(userInformationParser.getUserDetails().UserType.equalsIgnoreCase("1")){
                        userPreference.setMentorOrMentee(MENTOR);
                    }else if(userInformationParser.getUserDetails().UserType.equalsIgnoreCase("2")){
                        userPreference.setMentorOrMentee(MENTEE);
                    }
                    userPreference.setPhoneNumber(userInformationParser.getUserDetails().PhoneNumber);
                    userPreference.setReferralCode(userInformationParser.getUserDetails().ReferralCode);
                    userPreference.setUserStatus(userInformationParser.getUserDetails().Status);
                    userPreference.setPhoto(userInformationParser.getUserDetails().Photo);
                    userPreference.setReferralPoints(userInformationParser.getUserDetails().ReferralPoints);
                    userPreference.setCurrentLat(userInformationParser.getUserDetails().CurrentLatitude);
                    userPreference.setCurrentLong(userInformationParser.getUserDetails().CurrentLongitude);
                    userPreference.setCurrentLocation(userInformationParser.getUserDetails().CurrentLocation);
                    userPreference.setOverallScores(userInformationParser.getUserDetails().OverallScores);
                    userPreference.setThumbnailPhoto(userInformationParser.getUserDetails().ThumbnailPhoto);
                    switch (fromActivity){
                        case AUTHORIZATION_ACTIVITY:
                        case CUSTOM_CONTACT_ACTIVITY:
                            if (userPreference.getIsHomePageShown()) {
                                context.startActivity(new Intent(context, HomeActivity.class));
                            } else if (userPreference.getIsContactPageShown()){
                                context.startActivity(new Intent(context, GetContactsActivity.class));
                            } else if(userPreference.getIsInvitePageShown()){
                                context.startActivity(new Intent(context, InviteActivity.class));
                            } else{
                                context.startActivity(new Intent(context, HomeActivity.class));
                            }
                            break;
                        case REFERRAL_ACTIVITY:
                            context.startActivity(new Intent(context, InviteActivity.class));
                            break;
                    }
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
            }
        });
    }
}
