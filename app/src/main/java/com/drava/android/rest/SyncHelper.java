package com.drava.android.rest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.drava.android.model.EndTrip;
import com.drava.android.model.StartTrip;
import com.drava.android.model.TrackTripPath;
import com.drava.android.model.TripViolationInfo;
import com.drava.android.parser.TripEndParser;
import com.drava.android.parser.TripStartParser;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.TextUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

import static com.drava.android.DravaApplication.getApp;
import static com.drava.android.DravaApplication.mContext;
import static com.drava.android.base.AppConstants.MENTEE;

public class SyncHelper {

    Context context;
    public boolean syncRunning = false;
    public String TAG = SyncHelper.class.getSimpleName();

    public SyncHelper(Context context) {
        this.context = context;
    }

    public void startOfflineDataSync() {
        final long tripId = getApp().getDBSQLite().getFirstTripId();

        AppLog.print(context, "Trip ID in Synchelper : " + tripId);
        if (tripId != 0) {
            syncRunning = true;
            StartTrip startTripObject = getApp().getDBSQLite().getStartTripInfo(tripId);

            AppLog.print(context, "startTripObject.serverId : " + startTripObject.serverId);
            if (startTripObject != null && startTripObject.serverId == 0) {
                //offline start and end trip
                AppLog.print(context, "Start Object STime--> " + startTripObject.startTime + "  SLat--> " + startTripObject.startLatitude +
                        " SLong--> " + startTripObject.startLongitude + " Type--> " + startTripObject.tripType + "  Date--> " + startTripObject.tripDate
                        + "  isPassenger--> " + startTripObject.isPassenger);

                callTripStartWebService(tripId, startTripObject.startTime,
                        startTripObject.startLatitude,
                        startTripObject.startLongitude,
                        startTripObject.tripType,
                        startTripObject.tripDate, startTripObject.isPassenger, new SyncListener() {
                            @Override
                            public void onSuccess(boolean status, int serverTripId, String message) {

                                showToast("Trip start success :" + message);
                                syncRunning = false;
                                if (status) {
                                    EndTrip endTripObject = getApp().getDBSQLite().getEndTripInfo(tripId);
                                    if (endTripObject != null && endTripObject.EndLatitude != null && endTripObject.EndLongitude != null) {
                                        AppLog.print(context, "/n/n/n ETime--> " + endTripObject.EndTime + "  ELat--> " + endTripObject.EndLatitude +
                                                " ELong--> " + endTripObject.EndLongitude + " Distance--> " + endTripObject.Distance + "  MaxSpeed--> " + endTripObject.MaxSpeed + " /n/n/n");
                                        syncRunning = true;
                                        callTripEndWebService(endTripObject, "" + serverTripId, tripId, new SyncListener() {

                                            @Override
                                            public void onSuccess(boolean status, int serverTripId, String message) {
                                                syncRunning = false;

                                                showToast("Trip end success :" + message);
                                                if (getApp().getDBSQLite().deleteTripEntry(tripId)) {
                                                    AppLog.print(context, "---------------------End from 1-----------------------");
                                                    DravaLog.print("---------------------End from 1-----------------------");
                                                    callOfflineDataSyncwithDelay();     //startOfflineDataSync();
                                                }
                                            }

                                            @Override
                                            public void onFailure(boolean status, String message) {

//                                                showToast("Trip end fails :" + message);
                                                syncRunning = false;
                                            }
                                        });
//                                        callTrackTripPath(tripId, serverTripId);
                                    } else {
                                        AppLog.print(context, "SyncHelper EndTrip Trip object is null during trip Starting");
                                        syncRunning = false;
                                    }
                                } else {
                                    syncRunning = false;
                                    if(getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {   //R.L v1.1
                                        callOfflineDataSyncwithDelay();     //startOfflineDataSync();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(boolean status, String message) {
                                syncRunning = false;

//                                showToast("Trip starts fails : " + message);
                            }
                        });


            } else if (startTripObject != null && startTripObject.serverId != 0) {
                //online start offline stop
                EndTrip endTripObject = getApp().getDBSQLite().getEndTripInfo(tripId);
                if (endTripObject != null && endTripObject.EndLatitude != null && endTripObject.EndLongitude != null) {
                    AppLog.print(context, "SyncHelper online start offline stop EndTrip Trip object not null");
//                    final int tempTripId = getApp().getDBSQLite().getServerIdforEndTrip(Long.parseLong(getApp().getUserPreference().getTripId()));
                    callTripEndWebService(endTripObject, "" + startTripObject.serverId, tripId, new SyncListener() {
                        @Override
                        public void onSuccess(boolean status, int serverTripId, String message) {

                            showToast("Trip End off Success : " + message);
                            if (getApp().getDBSQLite().deleteTripEntry(tripId)) {
                                syncRunning = false;
                                AppLog.print(context, "---------------------End from 2-----------------------");
                                DravaLog.print("---------------------End from 2-----------------------");
                                callOfflineDataSyncwithDelay();     //startOfflineDataSync();
                            }
                        }

                        @Override
                        public void onFailure(boolean status, String message) {

//                            showToast("Trip Start off failure : " + message);
                            syncRunning = false;
                        }
                    });

//                    callTrackTripPath(tripId, startTripObject.serverId);
                } else {
                    AppLog.print(context, "SyncHelper EndTrip Trip object *****null*****");
                    syncRunning = false;

//                    showToast("Trip not ended correctly!!");
                    /*if(LocationTrackerService.isEndTripTriggred) {
                    EndTrip endTripObj = getApp().getUserPreference().getEndTripInfo();
                    if (endTripObj != null) {
                        DravaLog.print("---------------------End from end object preference part-----------------------");
                        AppLog.print(context, "SyncHelper Preferences EndTrip Trip object contains value not null");
                        if (getApp().getDBSQLite().updateEndTripFromPreference(startTripObject.serverId,
                                endTripObj.EndTime,
                                endTripObj.EndLatitude,
                                endTripObj.EndLongitude,
                                endTripObj.Distance,
                                endTripObj.MinSpeed,
                                endTripObj.MaxSpeed)) {
                        }
                    } else {
                        AppLog.print(context, "SyncHelper Preferences EndTrip Trip object is null");
                    }
                    AppLog.print(context, "SyncHelper online start offline stop EndTrip Trip object is null");
                    syncRunning = false;
                    DravaLog.print("---------------------End from 3-----------------------");
                    callOfflineDataSyncwithDelay();
                    LocationTrackerService.isEndTripTriggred = false;
                    }*/
                }
            }
        } else {
            syncRunning = false;
//            showToast("Sync Completed");
        }
        startOfflineViolateDataSync();
    }


    public void callTrackTripPath(final long offlineTripId, final int tripId) {

        List<TrackTripPath> trackTripPathList = getApp().getDBSQLite().getTrackTripPathInfo(offlineTripId);

        Log.e(TAG, "TrackPathObject size :" + trackTripPathList.size());
        JSONArray trackTripJsonArray = new JSONArray();

        if (trackTripPathList.size() > 0) {
            for (TrackTripPath object : trackTripPathList) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Latitude", object.Latitude);
                    jsonObject.put("Longitude", object.Longitude);
                    jsonObject.put("TripId", tripId);
                    jsonObject.put("IsViolation", object.IsViolation);
                    trackTripJsonArray.put(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Log.e(TAG, trackTripJsonArray.toString());
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.accumulate("Location", trackTripJsonArray);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.e(TAG, jsonObject.toString());

            callTripPathWebService(jsonObject.toString(), new SyncListener() {
                @Override
                public void onSuccess(boolean status, int tripId, String message) {
                    if (getApp().getDBSQLite().deleteTrackPath(offlineTripId)) {
                        showToast("Trip Tracking data move success");
                    } else {
//                        showToast("Trip Tracking data move fails");
                    }
                }

                @Override
                public void onFailure(boolean status, String message) {
                    Log.e(TAG, "Trip Tracking Ws fails " + message);
                }
            });
        }
    }

    public void callTripPathWebService(String trackPathJson, final SyncListener syncListener) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), (trackPathJson));
        getApp().getRetrofitInterface().tripTrackingDetails(requestBody).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                AppLog.print(context, "Tracking Trip Path Success----------------");
                Log.e(TAG, "Trip Tracking Path : " + content);
                syncListener.onSuccess(true, 0, "Track path data moved success");
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                Log.e(TAG, "Trip Tracking Path Error: " + message);
                String cause = "";
                if (t.getCause() != null) {
                    cause = t.getCause().toString();
                }
                AppLog.print(context, "Tracking Trip Path Failure message---------->" + message + " ---------cause==>" + cause);
                syncListener.onFailure(false, "Track path data move Failure");
            }
        });
    }


    public void callTripEndWebService(EndTrip endTrip, final String serverTripId, final long offlineTripId, final SyncListener syncListener) {
        List<TrackTripPath> trackTripPathList = getApp().getDBSQLite().getTrackTripPathInfo(offlineTripId);

        Log.e(TAG, "TrackPathObject size :" + trackTripPathList.size());
        AppLog.print(mContext, "SynHelper===>TrackPathObject size :" + trackTripPathList.size());
        JSONArray trackTripJsonArray = new JSONArray();

        if (trackTripPathList.size() > 0) {
            for (TrackTripPath object : trackTripPathList) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Latitude", object.Latitude);
                    jsonObject.put("Longitude", object.Longitude);
                    jsonObject.put("TripId", serverTripId);
                    jsonObject.put("IsViolation", object.IsViolation);
                    trackTripJsonArray.put(jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.e(TAG, trackTripJsonArray.toString());
            endTrip.TripLocation = trackTripJsonArray.toString();
        } else {
            endTrip.TripLocation = "";
        }
        String json = new Gson().toJson(endTrip);
        Log.d(TAG, "callTripEndWebService: json" + json);
        AppLog.print(mContext, "callTripEndWebService: json" + json);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), (json));
        getApp().getRetrofitInterface().endTrip(serverTripId, /*endTrip*/ requestBody).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);

                showToast("Trip End id " + serverTripId);
                TripEndParser tripEndParser = new Gson().fromJson(content, TripEndParser.class);

                if (tripEndParser != null) {
                    if (tripEndParser.meta.code == 201) {
                        getApp().getUserPreference().setStartTripWebserviceCalled(false);
                        Log.e("SyncHelper", "onSuccessCallback=>" + content);
                        AppLog.print(context, "Trip End WS ---  onSuccessCallback=>" + content);
                        if (getApp().getDBSQLite().deleteTrackPath(offlineTripId)) {
                            showToast("Trip Tracking data move success");
                        } else {
//                            showToast("Trip Tracking data move fails");
                        }
                        syncListener.onSuccess(true, Integer.parseInt(serverTripId), "Trip Successfully Ended - " + serverTripId);
                    } else if (tripEndParser.meta.code == 1015) {
                        syncListener.onFailure(true, "Trip end time is invaild");
                    } else {
                        if (!TextUtils.isNullOrEmpty(tripEndParser.meta.dataPropertyName)) {
                            Log.e(TAG, tripEndParser.meta.dataPropertyName);
                            AppLog.print(context, tripEndParser.meta.dataPropertyName);
                        }
                        if (!TextUtils.isNullOrEmpty(tripEndParser.meta.errorMessage)) {
                            AppLog.print(context, "Trip End WS==>ErrorMessage==>" + tripEndParser.meta.errorMessage);
                            showToast("Trip Error Message " + tripEndParser.meta.errorMessage);
                        }
                        syncListener.onFailure(false, "Trip end response code in valid check it");
                    }
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                Log.e("Location Service", "Failure Call back");
//                showToast("Trip End fails");
                DravaLog.print("onFailureCallback=>message=>" + message + "=>code=>" + code);
                AppLog.print(context, "Trip End WS --- onFailureCallback=>message=>" + message + "=>code=>" + code);
                if (t != null) {
                    if (t.getCause() != null) {
                        AppLog.print(context, "Trip End WS --- onFailureCallback=>message=>" + message + "=>code=>" + code + "==>Cause==>" + t.getCause());
                    }
                }
                syncListener.onFailure(false, message);
            }
        });
    }

    public void callTripStartWebService(final long offlineTripId, final String startTime,
                                        final String startLatitude, final String startLongitude,
                                        final String tripType, final String tripDate, final String isPassenger,
                                        final SyncListener syncListener) {

        getApp().getRetrofitInterface().tripCreate(startTime, startLatitude, startLongitude, tripType, tripDate, isPassenger).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                DravaLog.print("onSuccessCallback=>" + content);
                AppLog.print(context, "Trip Start WS --- onSuccessCallback=>" + content);
                getApp().getUserPreference().setStartTripWebserviceCalled(true);
                TripStartParser tripStartParser = new Gson().fromJson(content, TripStartParser.class);
                if (tripStartParser.meta.code == 201) {
                    if (tripStartParser.meta.tripStatus == 0) {

//                        getApp().getDBSQLite().deleteTripEntry(offlineTripId);
                        getApp().getDBSQLite().updateServerStartTripId(tripStartParser.TripDetail.TripId, offlineTripId);
                        if (!getApp().getUserPreference().getTripId().equals(String.valueOf(offlineTripId))) {  //pr
                            EndTrip endTrip = getApp().getUserPreference().getEndTripInfo();

                            getApp().getDBSQLite().updateEndTrip(offlineTripId,
                                    endTrip.EndTime,
                                    endTrip.EndLatitude,
                                    endTrip.EndLongitude,
                                    endTrip.Distance,
                                    endTrip.MinSpeed,
                                    endTrip.MaxSpeed);

                            startOfflineDataSync();
                        }else{
                            AppLog.print(mContext, "------------Previous Trip not Ended, Trip Started with continue of previous trip-------------");
                            syncListener.onSuccess(false, 0, "Previous Trip not Ended, Trip Started with continue of previous trip");     //  Trip started with status = 0
                        }
//                        long newTripofflineTripId = DateConversion.getTimeinMillis();
//                        getApp().getUserPreference().setTripId("" + newTripofflineTripId);
//                        if (getApp().getDBSQLite().startNewTrip(newTripofflineTripId,
//                                startTime,
//                                startLatitude,
//                                startLongitude,
//                                tripType,
//                                tripDate)) {
//                showToast("Start Offline trip :" + offlineTripId);
//                        }
//                        syncListener.onSuccess(true, tripStartParser.TripDetail.TripId, "Trip Started : " + tripStartParser.TripDetail.TripId);

                    } else {
                        getApp().getDBSQLite().updateServerStartTripId(tripStartParser.TripDetail.TripId, offlineTripId);

                        syncListener.onSuccess(true, tripStartParser.TripDetail.TripId, "Trip Started : " + tripStartParser.TripDetail.TripId);
                    }
                } else {
                    syncListener.onSuccess(false, 0, "cannot start trip, previous trip incomplete");
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
//                showToast("Trip starts fails");
                DravaLog.print("onFailureCallback=>message=>" + message + "=>code=>" + code);
                AppLog.print(context, "Trip Start WS ---  onFailureCallback=>message=>" + message + "=>code=>" + code);
                if (t != null) {
                    if (t.getCause() != null) {
                        AppLog.print(context, "Trip Start WS ---  onFailureCallback=>message=>" + message + "=>code=>" + code + "==>Cause==>" + t.getCause());
                    }
                }
                getApp().getUserPreference().setStartTripWebserviceCalled(false);
                syncListener.onFailure(false, message);
            }
        });
    }

    // Inserting violation offline stored data
    private void startOfflineViolateDataSync() {
        List<TripViolationInfo> violationInfoList = getApp().getDBSQLite().getTripViolationInfo();
        if (violationInfoList.size() > 0) {
            for (TripViolationInfo violationInfoObject : violationInfoList) {
                if (TextUtils.isNullOrEmpty(violationInfoObject.rowId))
                    return;
                String serverTripId = violationInfoObject.tripId;
                String roadSpeed = violationInfoObject.roadSpeed;
                String vehicleSpeed = violationInfoObject.vehicleSpeed;
                String latitude = violationInfoObject.latitude;
                String longitude = violationInfoObject.longitude;
                final String rowId = violationInfoObject.rowId;
                AppLog.print(context, "Offline Trip Violation entry Trip id " + serverTripId);

                getApp().getRetrofitInterface().tripVoilate(serverTripId, roadSpeed, vehicleSpeed, latitude, longitude).enqueue(new RetrofitCallback<ResponseBody>() {
                    @Override
                    public void onSuccessCallback(Call<ResponseBody> call, String content) {
                        super.onSuccessCallback(call, content);
                        AppLog.print(mContext, "tripVoilate====>onSuccessCallback=>" + content);
                        if (getApp().getDBSQLite().deleteViolationInsertedRow(rowId)) {
//                            startOfflineViolateDataSync();
                            AppLog.print(mContext, "SynHelper==============>Trip Voilate Stored in Db<==============RowId==>" + rowId + "====> Success Deleted");
                        }
                    }

                    @Override
                    public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                        super.onFailureCallback(call, t, message, code);
//                        startOfflineViolateDataSync();
                        if (getApp().getDBSQLite().deleteViolationInsertedRow(rowId)) {
//                            startOfflineViolateDataSync();
                            AppLog.print(mContext, "SynHelper==============>Trip Voilate Stored in Db<==============RowId==>" + rowId + "====>Failure Deleted");
                        }
                        AppLog.print(mContext, "tripVoilate====>onFailureCallback=>" + message+" Code=====> "+code);
                        if (t != null && t.getCause() != null) {
                            AppLog.print(mContext, "tripVoilate====>onFailureCallback=>" + message + "==>Cause==>" + t.getCause());
                        }
                    }
                });
            }
        } else {
            AppLog.print(mContext, "====>TripVoilationList==>Empty");
        }
    }

    private void showToast(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 1000);
    }


    private void callOfflineDataSyncwithDelay() {

        new java.util.Timer().schedule(

                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        startOfflineDataSync();
                    }
                }, 300);
    }

}
