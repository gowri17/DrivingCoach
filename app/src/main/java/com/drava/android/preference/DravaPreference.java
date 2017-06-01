package com.drava.android.preference;

import android.content.Context;
import android.content.SharedPreferences;

import com.drava.android.model.EndTrip;
import com.google.gson.Gson;

public class DravaPreference {
    private SharedPreferences mPreference;
    private SharedPreferences.Editor mEditor;
    // The name of the SharedPreferences file
    private static final String PREF_NAME = "pref_name";
    private static final String ACCESS_TOKEN = "access_token_for_user_authentication";
    private static final String USER_LOGGED = "logged_user_or_not";
    private static final String AUTHENTICATED_VIA = "authenticated_via";
    private static final String MENTOR_OR_MENTEE = "mentor_or_mentee";
    private static final String USER_ID = "user_id";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String EMAIL = "email";
    private static final String FB_ID = "fb_id";
    private static final String GPLUS_ID = "gplus_id";
    private static final String LINKEDIN_ID = "linkedin_id";
    private static final String INSTAGRAM_ID = "instagram_id";
    private static final String DEVICE_ID = "device_id";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String IS_HOME_PAGE_SHOWN = "is_home_page_shown";
    private static final String IS_INVITE_PAGE_SHOWN = "is_invite_page_shown";
    private static final String IS_CONTACT_PAGE_SHOWN = "is_contact_page_shown";
    private static final String REFERRAL_CODE = "referral_code";
    private static final String USER_STATUS = "user_status";
    private static final String PHOTO = "photo";
    private static final String GCM_TOKEN_ID = "gcm_token_id";
    private static final String TRIP_ID = "trip_id";
    private static final String ONLINE_TRIP_ID = "online_trip_id";
    private static final String START_TRIP = "start_trip";
    private static final String END_TRIP = "end_trip";
    private static final String VOILATE_TRIP = "voilate_trip";
    private static final String END_TRIP_INFO = "end_trip_info";
    private static final String REFERRAL_POINTS = "referral_points";
    private static final String CURRENT_LAT = "current_lat";
    private static final String CURRENT_LONG = "current_lng";
    private static final String CURRENT_LOCATION = "current_location";
    private static final String OVERALL_SCORES = "overall_scores";
    private static final String THUMBNAIL_PHOTO = "thumbnail_photo";
    private static final String ABOUT_US = "about_us";
    private static final String PRIVACY_POLICY = "privacy_policy";
    private static final String TERMS_CONDITIONS = "terms_condition";
    private static final String CONTACT_US = "contact_us";
    private static final String NOTIFICATION_SPEED_EXCEED = "notification_speed_exceed";
    private static final String NOTIFICATION_SWITCH_OFF = "notification_switch_off";
    private static final String NOTIFICATION_NOT_REACHABLE = "notification_not_reachable";
    private static final String NOTIFICATION_FORCE_QUIT = "notification_force_quit";
    private static final String REMAINING_TOKEN = "remaining_token";
    private static final String IS_TUT_INVITE_CONTACTS_VIEWED = "is_tut_invite_contacts_viewed";
    private static final String IS_TUT_MENTEE_MENTOR_VIEWED = "is_tut_mentee_mentor_viewed";
    private static final String IS_TUT_MENTEE_SETTINGS_VIEWED="is_tut_mentee_settings_viewed";

    public void setIsTutInviteContactsViewed(boolean isTutInviteContactsViewed){
        mEditor.putBoolean(IS_TUT_INVITE_CONTACTS_VIEWED, isTutInviteContactsViewed);
        mEditor.commit();
    }

    public boolean getIsTutInviteContactsViewed(){
        return mPreference.getBoolean(IS_TUT_INVITE_CONTACTS_VIEWED, false);
    }

    public void setIsTutMenteeMentorViewed(boolean isTutMenteeMentorViewed){
        mEditor.putBoolean(IS_TUT_MENTEE_MENTOR_VIEWED, isTutMenteeMentorViewed);
        mEditor.commit();
    }

    public boolean getIsTutMenteeMentorViewed(){
        return mPreference.getBoolean(IS_TUT_MENTEE_MENTOR_VIEWED, false);
    }

    public void setIsTutMenteeSettingsViewed(boolean isTutMenteeSettingsViewed){
        mEditor.putBoolean(IS_TUT_MENTEE_SETTINGS_VIEWED, isTutMenteeSettingsViewed);
        mEditor.commit();
    }

    public boolean getIsTutMenteeSettingsViewed(){
        return mPreference.getBoolean(IS_TUT_MENTEE_SETTINGS_VIEWED, false);
    }

    public DravaPreference(Context context) {
        mPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mEditor = mPreference.edit();
    }

    public void clearPreference() {
        mEditor.clear();
        mEditor.commit();
    }

    public void setAccessToken(String deviceToken) {
        mEditor.putString(ACCESS_TOKEN, deviceToken);
        mEditor.commit();
    }

    public String getAccessToken() {
        return mPreference.getString(ACCESS_TOKEN, "");
    }

    public void setUserLoggedIn(boolean isLoggedIn) {
        mEditor.putBoolean(USER_LOGGED, isLoggedIn);
        mEditor.commit();
    }

    public boolean isUserLoggedIn() {
        return mPreference.getBoolean(USER_LOGGED, false);
    }

    public void setAuthenticatedVia(String socialSite) {
        mEditor.putString(AUTHENTICATED_VIA, socialSite);
        mEditor.commit();
    }

    public String getAuthenticatedVia() {
        return mPreference.getString(AUTHENTICATED_VIA, "");
    }

    public void setMentorOrMentee(String text) {
        mEditor.putString(MENTOR_OR_MENTEE, text);
        mEditor.commit();
    }

    public String getMentorOrMentee() {
        return mPreference.getString(MENTOR_OR_MENTEE, "");
    }

    public void setFirstName(String firstName) {
        mEditor.putString(FIRST_NAME, firstName);
        mEditor.commit();
    }

    public String getFirstName() {
        return mPreference.getString(FIRST_NAME, "");
    }

    public void setLastName(String lastName) {
        mEditor.putString(LAST_NAME, lastName);
        mEditor.commit();
    }

    public String getLastName() {
        return mPreference.getString(LAST_NAME, "");
    }

    public void setEmail(String email) {
        mEditor.putString(EMAIL, email);
        mEditor.commit();
    }

    public String getEmail() {
        return mPreference.getString(EMAIL, "");
    }

    public void setFbId(String fbId) {
        mEditor.putString(FB_ID, fbId);
        mEditor.commit();
    }

    public String getFbId() {
        return mPreference.getString(FB_ID, "");
    }

    public void setGplusId(String gplusId) {
        mEditor.putString(GPLUS_ID, gplusId);
        mEditor.commit();
    }

    public String getGplusId() {
        return mPreference.getString(GPLUS_ID, "");
    }

    public void setLinkedinId(String linkedinId) {
        mEditor.putString(LINKEDIN_ID, linkedinId);
        mEditor.commit();
    }

    public String getLinkedinId() {
        return mPreference.getString(LINKEDIN_ID, "");
    }

    public void setInstagramId(String instagramId) {
        mEditor.putString(INSTAGRAM_ID, instagramId);
        mEditor.commit();
    }

    public String getInstagramId() {
        return mPreference.getString(INSTAGRAM_ID, "");
    }

    public void setDeviceId(String deviceId) {
        mEditor.putString(DEVICE_ID, deviceId);
        mEditor.commit();
    }

    public String getDeviceId() {
        return mPreference.getString(DEVICE_ID, "");
    }

    public void setPhoneNumber(String phoneNumber) {
        mEditor.putString(PHONE_NUMBER, phoneNumber);
        mEditor.commit();
    }

    public String getPhoneNumber() {
        return mPreference.getString(PHONE_NUMBER, "");
    }

    public void setIsHomePageShown(boolean isHomePageShown) {
        mEditor.putBoolean(IS_HOME_PAGE_SHOWN, isHomePageShown);
        mEditor.commit();
    }

    public boolean getIsHomePageShown() {
        return mPreference.getBoolean(IS_HOME_PAGE_SHOWN, false);
    }

    public void setIsInvitePageShown(boolean isInvitePageShown) {
        mEditor.putBoolean(IS_INVITE_PAGE_SHOWN, isInvitePageShown);
        mEditor.commit();
    }

    public boolean getIsInvitePageShown() {
        return mPreference.getBoolean(IS_INVITE_PAGE_SHOWN, false);
    }

    public void setIsContactPageShown(boolean isContactPageShown) {
        mEditor.putBoolean(IS_CONTACT_PAGE_SHOWN, isContactPageShown);
        mEditor.commit();
    }

    public boolean getIsContactPageShown() {
        return mPreference.getBoolean(IS_CONTACT_PAGE_SHOWN, false);
    }

    public String getUserId() {
        return mPreference.getString(USER_ID, "");
    }

    public void setUserId(String userId) {
        mEditor.putString(USER_ID, userId);
        mEditor.commit();
    }

    public String getReferralCode() {
        return mPreference.getString(REFERRAL_CODE, "");
    }

    public void setReferralCode(String referralCode) {
        mEditor.putString(REFERRAL_CODE, referralCode);
        mEditor.commit();
    }

    public String getUserStatus() {
        return mPreference.getString(USER_STATUS, "");
    }

    public void setUserStatus(String userStatus) {
        mEditor.putString(USER_STATUS, userStatus);
        mEditor.commit();
    }

    public String getPhoto() {
        return mPreference.getString(PHOTO, "");
    }

    public void setPhoto(String photo) {
        mEditor.putString(PHOTO, photo);
        mEditor.commit();
    }

    public void setGcmTokenId(String gcmTokenId) {
        mEditor.putString(GCM_TOKEN_ID, gcmTokenId);
        mEditor.commit();
    }

    public String getGcmTokenId() {
        return mPreference.getString(GCM_TOKEN_ID, "");
    }

    public void setTripId(String tripId) {
        mEditor.putString(TRIP_ID, tripId);
        mEditor.commit();
    }

    public String getTripId() {
        return mPreference.getString(TRIP_ID, "");
    }

    public void setStartTripWebserviceCalled(boolean isCalled) {
        mEditor.putBoolean(START_TRIP, isCalled);
        mEditor.commit();
    }

    public boolean isStartTripWebserviceCalled() {
        return mPreference.getBoolean(START_TRIP, false);
    }

    public void setEndTripInfo(EndTrip endTrip) {
        Gson gson = new Gson();
        String json = gson.toJson(endTrip);
        mEditor.putString(END_TRIP_INFO, json);
        mEditor.commit();
    }

    public EndTrip getEndTripInfo() {
        Gson gson = new Gson();
        String json = mPreference.getString(END_TRIP_INFO, "");
        return gson.fromJson(json, EndTrip.class);
    }

    public void setReferralPoints(String ReferralPoints) {
        mEditor.putString(REFERRAL_POINTS, ReferralPoints);
        mEditor.commit();
    }

    public String getReferralPoints() {
        return mPreference.getString(REFERRAL_POINTS, "");
    }

    public void setCurrentLat(String currentLat) {
        mEditor.putString(CURRENT_LAT, currentLat);
        mEditor.commit();
    }

    public String getCurrentLat() {
        return mPreference.getString(CURRENT_LAT, "");
    }

    public void setCurrentLong(String currentLong) {
        mEditor.putString(CURRENT_LONG, currentLong);
        mEditor.commit();
    }

    public String getCurrentLong() {
        return mPreference.getString(CURRENT_LONG, "");
    }

    public void setOverallScores(String overallScores) {
        mEditor.putString(OVERALL_SCORES, overallScores);
        mEditor.commit();
    }

    public String getOverallScores() {
        return mPreference.getString(OVERALL_SCORES, "No");
    }

    public void setThumbnailPhoto(String thumbnailPhoto) {
        mEditor.putString(THUMBNAIL_PHOTO, thumbnailPhoto);
        mEditor.commit();
    }

    public String getThumbnailPhoto() {
        return mPreference.getString(THUMBNAIL_PHOTO, "");
    }

    public void setCurrentLocation(String currentLocation) {
        mEditor.putString(CURRENT_LOCATION, currentLocation);
        mEditor.commit();
    }

    public  String getCurrentLocation() {
        return mPreference.getString(CURRENT_LOCATION, "");
    }

    public void setAboutUs(String aboutUs) {
        mEditor.putString(ABOUT_US, aboutUs);
        mEditor.commit();
    }

    public  String getAboutUs() {
        return mPreference.getString(ABOUT_US, "");
    }

    public void setPrivacyPolicy(String privacyPolicy) {
        mEditor.putString(PRIVACY_POLICY, privacyPolicy);
        mEditor.commit();
    }

    public  String getPrivacyPolicy() {
        return mPreference.getString(PRIVACY_POLICY, "");
    }

    public void setTermsConditions(String termsConditions) {
        mEditor.putString(TERMS_CONDITIONS, termsConditions);
        mEditor.commit();
    }

    public  String getTermsConditions() {
        return mPreference.getString(TERMS_CONDITIONS, "");
    }

    public void setContactUs(String contactUs) {
        mEditor.putString(CONTACT_US, contactUs);
        mEditor.commit();
    }

    public  String getContactUs() {
        return mPreference.getString(CONTACT_US, "");
    }

    public void setNotificationSpeedExceed(boolean speedExceed){
        mEditor.putBoolean(NOTIFICATION_SPEED_EXCEED, speedExceed);
        mEditor.commit();
    }

    public boolean getNotificationSpeedExceed(){
        return mPreference.getBoolean(NOTIFICATION_SPEED_EXCEED, false);
    }

    public void setNotificationMenteeDeviceSwithOff(boolean swithOff){
        mEditor.putBoolean(NOTIFICATION_SWITCH_OFF, swithOff);
        mEditor.commit();
    }

    public boolean getNotificationMenteeDeviceSwithcOff(){
        return mPreference.getBoolean(NOTIFICATION_SWITCH_OFF, false);
    }

    public void setNotificationMenteeNotReachable(boolean notReachable){
        mEditor.putBoolean(NOTIFICATION_NOT_REACHABLE, notReachable);
        mEditor.commit();
    }

    public boolean getNotificationMenteeNotReachable(){
        return mPreference.getBoolean(NOTIFICATION_NOT_REACHABLE, false);
    }

    public void setNotificationForceQuit(boolean forceQuit){
        mEditor.putBoolean(NOTIFICATION_FORCE_QUIT, forceQuit);
        mEditor.commit();
    }
}
