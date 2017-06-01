package com.drava.android.parser;

import java.util.List;

public class MenteeListParser {

    public Meta meta;
    public List<MentorsMenteeList> MentorsMenteeList;
    public List<String> notifications;


    public class MentorsMenteeList
    {
        public String MenteeId;
        public String FirstName;
        public String LastName;
        public String Photo;
        public String CurrentLatitude;
        public String CurrentLongitude;
        public String CurrentLocation;
        public String Distance;
    }
}


