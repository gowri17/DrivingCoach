package com.drava.android.activity.trips;

import com.drava.android.parser.Meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 11/21/2016.
 */

public class TripListParser implements Serializable {
    public Meta meta;
    public List<MenteeTripList> MenteeTripList = new ArrayList<>();
//    public String[] MenteeTripList;
    public class MenteeTripList implements Serializable {
        public String StartTime,EndTime,StartLatitude,StartLongitude,EndLatitude,EndLongitude,Distance
                ,HoursTravelled,MinSpeed,MaxSpeed,TripType,TripDate,Scores,ViolationCount,Status,DateCreated
                ,DateModified,MenteesId,TripId,StartLocation,EndLocation,Hours,Minutes,Seconds,IsOffline,IsPassenger;
    }
}
