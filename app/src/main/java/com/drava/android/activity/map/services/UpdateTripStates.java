package com.drava.android.activity.map.services;

/**
 * Created by admin on 09/12/2016.
 */
//Interface communicate between TripStates to HomeActivity
public interface UpdateTripStates {

    void setButtonState(boolean state);
    void updateView();
    void updateViolatedPoint();
    void clearMap();
}
