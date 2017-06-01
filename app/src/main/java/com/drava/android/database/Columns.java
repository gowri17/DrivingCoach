package com.drava.android.database;

public class Columns {
    /**
     * This class contain all column names on DB. Contract Class for the database of the project
     */
    public interface Trips {
        String TABLE = "trip";
        String TRIPID = "trip_id";
        String STARTTIME = "start_time";
        String ENDTIME = "end_time";
        String STARTLATITUDE = "start_latitude";
        String STARTLONGITUDE = "start_longitude";
        String ENDLATITUDE = "end_latitude";
        String ENDLONGITUDE = "end_longitude";
        String DISTANCE = "distance";
        String MINSPEED = "min_speed";
        String MAXSPEED = "max_speed";
        String TRIPTYPE = "trip_type";
        String TRIPDATE = "trip_date";
        String VOILATIONCOUNT = "voilation_count";
        String SERVERTRIPID = "server_trip_id";
        String ISPASSENGER = "is_passenger";
    }

    public interface TripVoilation{
        String TABLE = "trip_voilation";
        String TRIPID = "trip_id";
        String ROADSPEED = "road_speed";
        String VEHICLESPEED = "vehicle_speed";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
    }

    public interface TripPath{
        String TABLE = "trip_path";
        String TRIPID = "trip_id";
        String LATITUDE = "latitude";
        String LONGITUDE = "longitude";
        String ISVOILATED = "isvoilated";
    }
}
