package com.drava.android.parser;

import java.util.List;

/**
 * Created by evuser on 14-11-2016.
 */

public class TripStartParser
{
    public Meta meta;
    public TripDetail TripDetail;
    public List<String> notifications;

    public class Meta
    {
        public int code,tripStatus;
        public String dataPropertyName;
    }

    public class TripDetail
    {
        public int TripId;
        public String StartLatitude,StartLongitude;
    }
}