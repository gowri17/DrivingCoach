package com.drava.android.activity.map.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.drava.android.R;
import com.drava.android.activity.HomeActivity;
import com.drava.android.model.EndTrip;
import com.drava.android.base.AppConstants;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DateConversion;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.TextUtils;

import okhttp3.ResponseBody;
import retrofit2.Call;

import static com.drava.android.DravaApplication.getApp;
import static com.drava.android.DravaApplication.mContext;

public class LocationTrackerService extends IntentService implements AppConstants {

    //    public String tripId;
    public long offlineTripId;
    //    public SyncHelper syncHelper;     R.L
    public static boolean isEndTripTriggred = false;

    public LocationTrackerService() {
        super("LocationService");
//        syncHelper = new SyncHelper(this);    R.L
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        Log.d("service", "onHandleIntent");
        final String trip = intent.getStringExtra(TRIP_START_END);

        if(getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {       //R.L v1.1
            if (trip.equals(START)) {
                //Call Start Webservice
                final String startTime = intent.getStringExtra(START_TIME);
                final String startLatitude = intent.getStringExtra(START_LATITUDE);
                final String startLongitude = intent.getStringExtra(START_LONGITUDE);
                final String tripType = intent.getStringExtra(TRIP_TYPE);
                final String tripDate = intent.getStringExtra(TRIP_DATE);
                final String passenger = intent.getStringExtra(PASSENGER);
                startTrip(startLatitude, startLongitude, startTime, tripType, tripDate, passenger);
            } else if (trip.equals(END)) {
                //Call End Webservice
                final String tripId = getApp().getUserPreference().getTripId();
                EndTrip endTrip = (EndTrip) intent.getSerializableExtra(END_TRIP);
                endTrip(tripId, endTrip);
            } else if (trip.equals(VOILATE)) {
                // Call Voilate WebService
                String roadSpeed = intent.getStringExtra(ROAD_SPEED);
                String vehicleSpeed = intent.getStringExtra(VEHICLE_SPEED);
                String latitude = intent.getStringExtra(START_LATITUDE);
                String longitude = intent.getStringExtra(START_LONGITUDE);
                String tripId = "" + getApp().getDBSQLite().getServerIdforEndTrip(Long.parseLong(getApp().getUserPreference().getTripId()));
                tripVoilate(tripId, roadSpeed, vehicleSpeed, latitude, longitude);
            }
        }
    }


    public void tripVoilate(final String tripId, final String roadSpeed, final String vehicleSpeed, final String latitude, final String longitude) {

        if (!TextUtils.isNullOrEmpty(tripId) && !tripId.equalsIgnoreCase("0")) {
            //insert voilation in db
            final long rowId = getApp().getDBSQLite().insertViolationInfo(tripId, roadSpeed, vehicleSpeed, latitude, longitude);
            if (rowId > 0) {
                AppLog.print(mContext, "==============>Trip Voilate Stored in Db<==============RowId==>" + rowId);
                if (getApp().getUserPreference().isStartTripWebserviceCalled()) {
                    if (DeviceUtils.isInternetConnected(getApplicationContext())) {

                        getApp().getRetrofitInterface().tripVoilate(tripId, roadSpeed, vehicleSpeed, latitude, longitude).enqueue(new RetrofitCallback<ResponseBody>() {
                            @Override
                            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                                super.onSuccessCallback(call, content);
                                if (getApp().getDBSQLite().deleteViolationInsertedRow(String.valueOf(rowId))) {
                                    AppLog.print(mContext, "==============>Trip Voilate Stored in Db<==============RowId==>" + rowId + "====>Deleted");
                                } else {
                                    AppLog.print(mContext, "==============>Trip Voilate Stored in Db<==============RowId==>" + rowId + "====>Not Deleted");
                                }
//                        DravaLog.print("onSuccessCallback=>" + content);
                                AppLog.print(getApplicationContext(), "tripVoilate====>onSuccessCallback=>" + content);
//                        addNotification("Voilate Speed Service", 5);
                            }

                            @Override
                            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                                super.onFailureCallback(call, t, message, code);
                                DravaLog.print("onFailureCallback=>" + message + "   Failure Code : " + code);
//                            if(getApp().getDBSQLite().deleteViolationInsertedRow(String.valueOf(rowId))){
//                                AppLog.print(mContext,"==============>Trip Voilate Stored in Db<==============RowId==>"+rowId+"====>Deleted");
//                            }else {
//                                AppLog.print(mContext,"==============>Trip Voilate Stored in Db<==============RowId==>"+rowId+"====>Not Deleted");
//                            }
                                AppLog.print(getApplicationContext(), "tripVoilate====>onFailureCallback=>" + message+" Code=====> "+code);
                                if (t != null && t.getCause() != null) {
                                    AppLog.print(getApplicationContext(), "tripVoilate====>onFailureCallback=>" + message + "==>Cause==>" + t.getCause());
                                }
//                        addNotification("Failed voilate Service", 6);
                            }
                        });
                    }
                }
            } else {
                AppLog.print(mContext, "==============>Trip Voilate Not Stored in Db<==============");
            }
        }
    }

    public void endTrip(String tripId, EndTrip endTrip) {
        DravaLog.print("tripId==>" + tripId);
        AppLog.print(getApplicationContext(), "tripId==>" + tripId);
        Log.e("Location Service", "tripId==>" + tripId);
        final int tempTripId = getApp().getDBSQLite().getServerIdforEndTrip(Long.parseLong(tripId));
        AppLog.print(getApplicationContext(), "Call Store End Trip in DB");
        if (endTrip != null) {
            getApp().getDBSQLite().updateEndTrip(Long.parseLong(getApp().getUserPreference().getTripId()),
                    endTrip.EndTime,
                    endTrip.EndLatitude,
                    endTrip.EndLongitude,
                    endTrip.Distance,
                    endTrip.MinSpeed,
                    endTrip.MaxSpeed);

            AppLog.print(getApplicationContext(), "Call Store End Trip in DB" + getApp().getUserPreference().getTripId());
        } else {
            EndTrip endTripObj = getApp().getUserPreference().getEndTripInfo();
            if (endTripObj != null) {
                AppLog.print(LocationTrackerService.this.getApplicationContext(), "Location tracker service end object is null, construct object from preference");
                getApp().getDBSQLite().updateEndTripFromPreference(Long.parseLong(getApp().getUserPreference().getTripId()),
                        endTripObj.EndTime,
                        endTripObj.EndLatitude,
                        endTripObj.EndLongitude,
                        endTripObj.Distance,
                        endTripObj.MinSpeed,
                        endTripObj.MaxSpeed);
                AppLog.print(getApplicationContext(), "Call Store End Trip in DB" + getApp().getUserPreference().getTripId());
            } else {
                AppLog.print(LocationTrackerService.this.getApplicationContext(), "LocationTracker EndTrip Trip object is null");
            }
        }
        isEndTripTriggred = true;

        if (DeviceUtils.isInternetConnected(getApplicationContext())) {
//            if (syncHelper.syncRunning) {
            if (getApp().getSyncHelper().syncRunning) {
                return;
            } else {
//                    new java.util.Timer().schedule(
//                            new java.util.TimerTask(){
//
//                                @Override
//                                public void run() {
                getApp().getSyncHelper().startOfflineDataSync();
//                                }
//                            }, 300);

            }
        }
    }

    public void startTrip(String latitude, String longitude, String sTime, String type, String tDate, String strPassenger) {
        final String startTime = sTime;
        final String startLatitude = latitude;
        final String startLongitude = longitude;
        final String tripType = type;
        final String tripDate = tDate;
        final String Passenger = strPassenger;

        offlineTripId = DateConversion.getTimeinMillis();
        AppLog.print(LocationTrackerService.this.getApplicationContext(), "LocationTrackerService======>startTrip======================>offlineTripId==>" + offlineTripId);
        getApp().getUserPreference().setTripId("" + offlineTripId);

        long unEndedTripIdduringSwitchOff = getApp().getDBSQLite().getOfflineTripIdofUnEndedTripduringSwitchOff();      //R.L v1.1
        AppLog.print(LocationTrackerService.this.getApplicationContext(), "LocationTrackerService======>unEndedTripIdduringSwitchOff============>" + unEndedTripIdduringSwitchOff);
        if(unEndedTripIdduringSwitchOff > 0){
            AppLog.print(LocationTrackerService.this.getApplicationContext(), "LocationTrackerService======>Updating of unEndedTripId============>" + unEndedTripIdduringSwitchOff);
            EndTrip endTripObject = getApp().getUserPreference().getEndTripInfo();
            if (endTripObject != null && endTripObject.EndLatitude != null && endTripObject.EndLongitude != null) {
                AppLog.print(LocationTrackerService.this.getApplicationContext(), "LocationTrackerService======>Updating of unEndedTripId============>Location tracker service end object is null, construct object from preference");
                getApp().getDBSQLite().updateEndTrip(unEndedTripIdduringSwitchOff,
                        endTripObject.EndTime,
                        endTripObject.EndLatitude,
                        endTripObject.EndLongitude,
                        endTripObject.Distance,
                        endTripObject.MinSpeed,
                        endTripObject.MaxSpeed);
            }else {
                AppLog.print(LocationTrackerService.this.getApplicationContext(), "LocationTrackerService======>Updating of unEndedTripId with Static Values ============>");
                getApp().getDBSQLite().updateEndTrip(unEndedTripIdduringSwitchOff, startTime, startLatitude, startLongitude, "0.9", "0", "50");
            }
        }

        if (getApp().getDBSQLite().startNewTrip(offlineTripId,
                startTime,
                startLatitude,
                startLongitude,
                tripType,
                tripDate, Passenger)) {
//                showToast("Start Offline trip :" + offlineTripId);
        }

        if (DeviceUtils.isInternetConnected(getApplicationContext())) {
//            if (syncHelper.syncRunning) {
            if (getApp().getSyncHelper().syncRunning) {
                return;
            } else {
                AppLog.print(LocationTrackerService.this.getApplicationContext(), "---------------------End from 5-----------------------");
                DravaLog.print("---------------------End from 5 -----------------------");
                getApp().getSyncHelper().startOfflineDataSync();
            }
        } else {
            getApp().getUserPreference().setStartTripWebserviceCalled(false);
        }
    }

    public void addNotification(String title, int id) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.spot_mask)
                        .setContentTitle(title)
                        .setContentText("Sample test for trips");

        Intent notificationIntent = new Intent(this, HomeActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(id, builder.build());
    }

    /*public DravaApplication getApp() {
        return (DravaApplication) getApplication();
    }*/

    public void showToast(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LocationTrackerService.this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }


}
