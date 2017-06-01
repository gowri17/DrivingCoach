package com.drava.android.activity.map.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.drava.android.base.AppConstants;
import com.drava.android.model.EndTrip;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DateConversion;
import com.drava.android.utils.DravaLog;

import okhttp3.ResponseBody;
import retrofit2.Call;

import static com.drava.android.DravaApplication.getApp;

public class PollReceiver extends BroadcastReceiver implements AppConstants {
    private static final int PERIOD = 1000;
    private String TAG = PollReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context ctxt, Intent i) {
        Log.e("Receiver","Called");
        scheduleAlarms(ctxt);
    }

    void scheduleAlarms(Context ctxt) {
        Log.e("Receiver","Service Call Method");
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE) /*&& (getApp().getUserPreference().isStartTripWebserviceCalled())*/) {
            Log.e("Receiver","Service Call Success 1");
            EndTrip endTrip = getApp().getUserPreference().getEndTripInfo();
            if(endTrip!=null && endTrip.Distance!=null) {
                AppLog.print(ctxt, "Receiver==>distance==>" + endTrip.Distance);
                AppLog.print(ctxt, "Receiver==>tripMaxSpeed==>" + endTrip.MaxSpeed);
                AppLog.print(ctxt, "Receiver==>tripMinSpeed==>" + endTrip.MinSpeed);
                AppLog.print(ctxt, "Receiver==>End_time==>" + DateConversion.getCurrentDateAndTime());
                AppLog.print(ctxt, "Receiver==>Current Latitude==>" + endTrip.EndLatitude);
                AppLog.print(ctxt, "Receiver==>Current Longitude==>" + endTrip.EndLongitude);

                Intent intent = new Intent(ctxt, LocationTrackerService.class);
                intent.putExtra(END_TRIP, endTrip);
                intent.putExtra(TRIP_START_END, END);
                ctxt.startService(intent);
                Log.e("Receiver", "Service Call Success 2");
            } else {
                AppLog.print(ctxt, "Receiver==>End Trip is null");
            }


            Log.e(TAG, "App got Force quited by the user");         //R.L v1.1
            AppLog.print(ctxt, "-----------------App got Force quited by the user--------------------");
            getApp().getRetrofitInterface().updateGPSStatus("0","0", "1","3").enqueue(new RetrofitCallback<ResponseBody>() {
                @Override
                public void onSuccessCallback(Call<ResponseBody> call, String content) {
                    super.onSuccessCallback(call, content);
                    Log.e(TAG, "Force Quit Notification updated Success");
                }

                @Override
                public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                    super.onFailureCallback(call, t, message, code);
                    Log.e(TAG, "Force Quit Notification updated Failed");
                }
            });
        }
   /* Intent i=new Intent(ctxt, ScheduledService.class);
    ctxt.startService(i);
*/
  /*  PendingIntent pi=PendingIntent.getService(ctxt, 0, i, 0);

    mgr.setRepeating(AlarmManager.ELAPSED_REALTIME,
                     SystemClock.elapsedRealtime() + PERIOD, PERIOD, pi);*/


    }
}