package com.drava.android.parser;

import android.location.Location;

import com.google.maps.model.SnappedPoint;

import java.io.Serializable;
import java.util.List;

/**
 * Created by admin on 1/4/2017.
 */

public class SnapRoadParser implements Serializable {
    public Error error;
    public List<ParserSnappedPoint> snappedPoints;

    public class Error {
        public int code;
        public String message, status;
    }

    public class ParserSnappedPoint {
        public ParserLocation location;
        public int originalIndex = -1;
        public String placeId;

        public class ParserLocation {
            public double latitude, longitude;
        }
    }
}
