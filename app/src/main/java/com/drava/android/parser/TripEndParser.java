package com.drava.android.parser;

import java.util.List;

/**
 * Created by evuser on 28-11-2016.
 */

public class TripEndParser {
    public Meta meta;
    public TripDetail TripDetail;
    public List<String> notifications;

    public class TripDetail {
        public int TripId;
    }
}
