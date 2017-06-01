package com.drava.android.activity.mentor_mentee;



import com.drava.android.parser.Meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 11/3/2016.
 */

public class MentorListParser implements Serializable {

    public Meta meta;
    public List<MentorList> MentorList;
    public List<MentorList> MenteeList;
    public String[] notifications = new String[]{};


    public class MentorList implements Serializable{

        public String UserId,FirstName,LastName,Email,FBId,TwitterId,GooglePlusId,InstagramId,UserType,PhoneNumber,ReferralCode,Photo
                ,CurrentLocation,CurrentLatitude,CurrentLongitude,ThumbnailPhoto;
    }
}
