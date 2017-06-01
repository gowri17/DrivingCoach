package com.drava.android.base;

import com.drava.android.BuildConfig;

public class Log {
    private static final int VERBOSE = android.util.Log.VERBOSE;
    private static final int DEBUG = android.util.Log.DEBUG;
    private static final int INFO = android.util.Log.INFO;
    private static final int WARN = android.util.Log.WARN;
    private static final int ERROR = android.util.Log.ERROR;
    private static final int ASSERT = android.util.Log.ASSERT;

    public static void e(String message){
        println(ERROR, "CarFit", message);
    }

    public static void d(String message){
        println(DEBUG, "CarFit", message);
    }

    public static void e(String TAG, String message) {
        println(ERROR, TAG, message);
    }

    public static void d(String TAG, String message) {
        println(DEBUG, TAG, message);
    }

    public static void i(String TAG, String message) {
        println(INFO, TAG, message);
    }

    public static void v(String TAG, String message) {
        println(VERBOSE, TAG, message);
    }

    public static void w(String TAG, String message) {
        println(WARN, TAG, message);
    }

    private static void println(int priority, String tag, String msg) {
        if (BuildConfig.DEBUG) {
            if (msg.length() > 4000) {
                android.util.Log.println(priority, tag, msg.substring(0, 4000));
                println(priority, tag, msg.substring(4000));
            }else {
                android.util.Log.println(priority, tag, msg);
            }
        }
    }

}
