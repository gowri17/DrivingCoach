package com.drava.android.activity.map.services;

/**
 * Created by admin on 09/12/2016.
 */
//Interface used to communicate between HomeActivity to HomeFragment
public interface UpdateTripStateFragment {

    void setButtonState(boolean state);
    void updateView();
    void updateViolatedPoint();
    void clearMap();
}
