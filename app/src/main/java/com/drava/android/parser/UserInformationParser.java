package com.drava.android.parser;

import java.util.List;

/**
 * Created by evuser on 03-11-2016.
 */

public class UserInformationParser {
    public Meta meta;
    public UserDetails userDetails;
    public List<String> notifications;

    public Meta getMeta() {
        return meta;
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public class Meta
    {
        public int code;
        public String dataPropertyName;
    }

    public class UserDetails
    {
        public String UserId;
        public String FirstName;
        public String LastName;
        public String Email;
        public String FBId;
        public String LinkedInId;
        public String GooglePlusId;
        public String InstagramId;
        public String UserType;
        public String Platform;
        public String PhoneNumber;
        public String ReferralCode;
        public String Status;
        public String LastLoginDate;
        public String DateCreated;
        public String DateModified;
        public String Photo;
        public String ReferralPoints;
        public String CurrentLatitude;
        public String CurrentLongitude;
        public String CurrentLocation;
        public String OverallScores;
        public String ThumbnailPhoto;
        public String IsSwitchOff;
        public String IsViolation;
        public String IsGpsOff;
        public String IsForceQuit;
    }
}
