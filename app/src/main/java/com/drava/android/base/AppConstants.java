package com.drava.android.base;

import com.drava.android.BuildConfig;

public interface AppConstants {
    String DEFAULT_ERROR = "Unable to connect the server. Please try again later";
    String FACEBOOK = "facebook";
    String TWITTER = "twitter";
    String GOOGLE_PLUS = "google_plus";
    String LINKEDIN = "linkedin";
    String INSTAGRAM = "instagram";
    String FIRST_NAME = "firstName";
    String LAST_NAME = "lastName";
    String EMAIL = "email";
    String FACEBOOK_ID = "facebookId";
    String GOOGLE_PLUS_ID = "googleplusId";
    String LINKEDIN_ID = "linkedInId";
    String TWITTER_ID = "twitterId";
    String INSTAGRAM_ID = "instagramId";
    String DEVICE_ID = "deviceId";
    String ANDROID = "android";

    String INSTAGRAM_CLIENT_ID = "6117c5314d484284861c352bc01913a7";
    String INSTAGRAM_REDIRECE_URI = "http://drava.us-west-2.elasticbeanstalk.com/";

    String INSTAGRAM_SDC_CLIENT_ID = "485f6f33d2e74030b6ce404da3b40a00";
    String INSTAGRAM_SDC_REDIRECT_URI = "http://www.sdc.com.my/Welcome.html";
    String SECRET_ID = " f561f0bfc92c472499188e1f2b43ee08";

    String MENTOR = "mentor";
    String MENTEE = "mentee";

    int PERMISSION_REQ_CODE = 1000;
    int DEFAULT_USER_STATUS = 10;
    int VIEW_MENTEE_REQ_CODE = 10001;

    String AUTHORIZATION_ACTIVITY = "authorization_activity";
    String CUSTOM_CONTACT_ACTIVITY = "custom_contact_activity";
    String REFERRAL_ACTIVITY = "referral_activity";


    int TRACKING_START_SPEED = 10;
    int TRACKING_STOP_SPEED = 5;
    int INITIAL_COUNT_TO_VIEW_MENTEE = 5;
    String LOCATION_TRACKING_SERVICE="com.drava.android.activity.map.services.LocationTrackerService.SERVICE";
    String TRIP_START_END = "tripStartEnd";
    String START = "1";
    String END = "0";
    String VOILATE = "2";
    String CURRENT_LOCATION = "3";
    String STATUS = "status";
    String START_TIME = "start_time";
    String START_LATITUDE = "start_latitude";
    String START_LONGITUDE = "start_longitude";
    String CURRENT_LATITUDE = "current_latitude";
    String CURRENT_LONGITUDE = "current_longitude";
    String TRIP_TYPE = "trip_type";
    String TRIP_DATE = "trip_date";
    String ROAD_SPEED = "road_speed";
    String VEHICLE_SPEED ="vehicle_speed";
    String END_TRIP = "end_trip";
    String TRIP_ID = "trip_id";
    int GPS_LOCATION = 20;
    String MENTOR_LIST = "MENTOR_LIST";
    String PROFILE_PHOTO = "profile_photo";
    String MENTEE_ID ="mentee_id";
    String PASSENGER = "passenger";
    String PUSH_MESSAGE = "push_message";
    String INAPP_BILLING_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuBogXJapisB1TMUIz/a90DhyvLbD8mnXhfNafk0YRsHahzbboE2i2jjIx1mpw1N0f5wgqHXH1/bANP+WnW9dU3LgG6t2TAagxCvDHFCKE8vvnQ93JmV/oBvLUMxwjU1FerCqgbftIfYalWp1iFR7Ke2rR5wdzN73R6CpWTcg4uu2PQ+KBpGeDkHV8Erz8DLsfRr5YmY1lnX/8f+WJtQEnHG3OKFX6XyqbkH6jPg201hz+SBP9Yop8bbVp1FTLFbqIB1egAiuSGi3usoOd3B4Y5nGqkR3XXLWIlV00IWG/zHtjubODF1dRs5ta5mMe3a+rNfdm2q0cseEq7TVTuNo3QIDAQAB";

    public static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String MENTEE_LOCATION = "mentee_location";
    public static final String MENTEE_LATTITUDE = "mentee_lattitude";
    public static final String MENTEE_LONGITUDE = "mentee_longitude";

    String GPS_DISABLED = "gps_disabled";
}
