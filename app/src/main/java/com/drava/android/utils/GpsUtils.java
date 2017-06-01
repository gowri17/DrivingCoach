package com.drava.android.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.drava.android.R;

import static com.drava.android.base.AppConstants.GPS_LOCATION;

public class GpsUtils {

    public static boolean isGpsEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return !(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

    }

    public static void showGpsAlert(final Context context,DialogInterface.OnClickListener onClickListener, DialogInterface.OnClickListener onClickCancelListener) {
        String message = context.getString(R.string.gps_alert_message);
        new AlertDialog.Builder(context).setMessage(message)
                .setTitle(context.getString(R.string.app_name))
                .setCancelable(false)
                .setNegativeButton("Cancel",onClickCancelListener)
                .setPositiveButton(R.string.settings,onClickListener).create()
                .show();
    }

    public static String formatNumber(double distance) {
        String unit = "m";
        if (distance < 1) {
//            distance *= 1000;
            distance = 0;
            unit = "mm";
        } else if (distance > 1000) {
            distance /= 1000;
            unit = "km";
        }
        Log.d("GpsUtils", "formatNumber: "+distance);
        return String.format("%.02f", distance);
    }
}
