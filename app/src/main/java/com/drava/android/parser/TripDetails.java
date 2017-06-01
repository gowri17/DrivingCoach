package com.drava.android.parser;

import java.util.List;

public class TripDetails {

    public Meta meta;
    public List<TripDetail> TripDetail;
    public List<String> notifications;

    public class TripDetail
    {
        public String StartTime;
        public String EndTime;
        public String StartLatitude;
        public String StartLongitude;
        public String EndLatitude;
        public String EndLongitude;
        public String StartLocation;
        public String EndLocation;
        public String Distance;
        public String HoursTravelled;
        public String MinSpeed;
        public String MaxSpeed;
        public String TripType;
        public String TripDate;
        public String Scores;
        public String ViolationCount;
        public String Hours;
        public String Minutes;
        public String Seconds;
        public String TripLocation;
    }
}
