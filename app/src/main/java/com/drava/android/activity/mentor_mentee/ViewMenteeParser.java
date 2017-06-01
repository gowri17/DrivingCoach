package com.drava.android.activity.mentor_mentee;

import java.util.List;

/**
 * Created by evuser on 14-02-2017.
 */

public class ViewMenteeParser{
    public Meta meta;
    public ReferralPoints ReferralPoints;
    public List<String> notifications;

    public class Meta
    {
        public int code;
        public String dataPropertyName;
    }

    public class ReferralPoints
    {
        public int RemainingPoints;
        public String MenteeLatitude;
        public String MenteeLongitude;
        public String MenteeLocation;
    }
}

