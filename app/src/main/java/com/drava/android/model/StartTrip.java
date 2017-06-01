package com.drava.android.model;

import java.io.Serializable;

/**
 * Created by evuser on 21-11-2016.
 */

public class StartTrip implements Serializable {
    public String startTime;
    public String startLatitude;
    public String startLongitude;
    public String tripType;
    public String tripDate;
    public String isPassenger;
    public int serverId;
}
