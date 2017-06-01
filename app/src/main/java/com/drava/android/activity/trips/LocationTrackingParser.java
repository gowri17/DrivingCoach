package com.drava.android.activity.trips;

import com.drava.android.parser.Meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 11/23/2016.
 */

public class LocationTrackingParser implements Serializable {
    Meta meta;
    List<LocationTracking> locationTrackingList;
    public class LocationTracking{
        String TripId,UsersId,Latitude,Longitude,IsViolation;
    }
}
