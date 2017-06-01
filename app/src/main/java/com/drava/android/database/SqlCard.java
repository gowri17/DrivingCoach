package com.drava.android.database;

public class SqlCard extends Columns{

    /**
     * This class contain table structure and give DB instance
     */

    static final String TRIP_TABLE = "CREATE TABLE IF NOT EXISTS " + Trips.TABLE + " ("
            + Trips.TRIPID + " TEXT , "
            + Trips.STARTTIME + " TEXT, "
            + Trips.ENDTIME + " TEXT, "
            + Trips.STARTLATITUDE + " TEXT, "
            + Trips.STARTLONGITUDE + " TEXT, "
            + Trips.ENDLATITUDE + " TEXT, "
            + Trips.ENDLONGITUDE + " TEXT, "
            + Trips.DISTANCE + " TEXT, "
            + Trips.MINSPEED + " TEXT, "
            + Trips.MAXSPEED + " TEXT, "
            + Trips.TRIPTYPE + " TEXT, "
            + Trips.TRIPDATE + " TEXT, "
            + Trips.ISPASSENGER + " TEXT, "
            + Trips.VOILATIONCOUNT + " INTEGER DEFAULT 0, "
            + Trips.SERVERTRIPID +" INTEGER DEFAULT 0"
            +")";

    static final String TRIP_VOILATION_TABLE = "CREATE TABLE IF NOT EXISTS "+ TripVoilation.TABLE+" ("
            + TripVoilation.TRIPID +" TEXT, "
            + TripVoilation.ROADSPEED + " TEXT, "
            + TripVoilation.VEHICLESPEED + " TEXT, "
            + TripVoilation.LATITUDE + " TEXT, "
            + TripVoilation.LONGITUDE + " TEXT "
            +")";

    static final String TRIP_TRACKING_TABLE = "CREATE TABLE IF NOT EXISTS "+ TripPath.TABLE+" ("
            + TripPath.TRIPID +" TEXT, "
            + TripPath.ISVOILATED + " TEXT, "
            + TripPath.LATITUDE + " TEXT, "
            + TripPath.LONGITUDE + " TEXT "
            +")";


}
