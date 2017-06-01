package com.drava.android.rest;

/**
 * Created by evuser on 22-11-2016.
 */

public interface SyncListener {
    void onSuccess(boolean status, int tripId,String message);
    void onFailure(boolean status, String message);
}
