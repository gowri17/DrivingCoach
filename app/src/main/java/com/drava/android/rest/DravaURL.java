package com.drava.android.rest;

import static com.drava.android.DravaApplication.getApp;

public class DravaURL {

    public static final String LINKED_IN_PEOPLE_PROFILE = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,email-address,picture-url)?format=json"; // specific basic details
//    public static final String LINKED_IN_PEOPLE_PROFILE = "https://api.linkedin.com/v1/people/~:(id,first-name,last-name,email-address,public-profile-url)?format=json"; // specific basic details

//    public static final String BASE_URL = "http://172.21.4.104/carfit/";
//    public static final String BASE_URL = "http://drava.us-west-2.elasticbeanstalk.com/";
    public static final String BASE_URL = getApp().getDBURL();

    public static final String SIGN_UP = "v1/users/signin";
    public static final String INSTAGRAM_SIGNIN = "https://api.instagram.com/v1/users/self";
    public static final String INVITES = "v1/users/invites/check";
    public static final String MENTEE_LIST = "v1/mentee/";
    public static final String MENTOR_LIST = "v1/mentor/";
    public static final String INVITE_MENTOR_MENTEE = "v1/users/invite";
    public static final String USER_DETAILS="v1/users/";
    public static final String DELETE_MENTOR = "v1/mentor/";
    public static final String DELETE_MENTEE = "v1/mentee/";
    public static final String ACCEPT_DECLINE = "v1/users/invitation/{id}";
    public static final String INVITE_LIST = "v1/users/invites";
    public static final String TRIP_CREATE ="v1/trips/";
    public static final String TRIP_END = "v1/trips/{Tripid}";
    public static final String TRIP_VOILATE = "v1/trips/violation/{tripid}";

    public static final String TRIP_LIST = "v1/trips/{menteeId}/list";
    public static final String TRIP_DETAILS = "v1/trips/{Tripid}";
    public static final String TRIP_VIOLATION_DETAILS = "v1/trips/{tripid}/violation";
    public static final String TRIP_TRACK_PATH = "v1/users/location/sync";
    public static final String LOCATION_TRACKING = "v1/users/locationtracking";
    public static final String HOME = "v1/mentor/home";
    public static final String REPORT_TRIP = "v1/trips/{menteeId}/report";
    public static final String CONTENTS = "v1/contents/";
    public static final String NOTIFICATION = "v1/mentee/notification";
    public static final String NOTIFICATION_LIST ="v1/users/notificationtracking";
    public static final String SETTINGS ="v1/mentor/settings";
    public static final String LOCATEMENTEE ="v1/users/locatementee";
    public static final String PURCHASE_POINTS ="v1/users/purchase";
    public static final String DELETE_NOTIFICATION = "v1/users/{notificationId}";
}

