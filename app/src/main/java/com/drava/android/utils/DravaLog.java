package com.drava.android.utils;

import android.util.Log;

import com.drava.android.BuildConfig;
import com.drava.android.DravaApplication;

/**
 * Created by evuser on 12-11-2016.
 */

public class DravaLog {
    private static String TAG = DravaApplication.class.getSimpleName();

    public static void print(String message){
        if (!BuildConfig.DEBUG) {
            return;
        }
        Log.d(TAG,message);
    }
}
