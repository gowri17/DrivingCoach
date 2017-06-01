package com.drava.android.activity.map;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.drava.android.DravaApplication;
import com.drava.android.activity.map.maputils.TaskFetchRoadSpeedLimit;
import com.drava.android.activity.map.maputils.UpdateMaxSpeed;
import com.drava.android.activity.map.services.LocationTrackerService;
import com.drava.android.activity.map.services.UpdateTripStates;
import com.drava.android.base.AppConstants;
import com.drava.android.model.EndTrip;
import com.drava.android.model.TrackTripPath;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.CurrentLocationClient;
import com.drava.android.utils.DateConversion;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.TextUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.drava.android.DravaApplication.getApp;
import static com.drava.android.DravaApplication.mContext;


public class TripStates implements AppConstants, UpdateMaxSpeed {
    public LatLng mCurrentLatLng, tripStartLatLng;
    public MyLocationListener myLocationListener;
    public LocationManager locationManager;
    public TrackCountDownTimer stopTrackTimer;

    public ArrayList<LatLng> violatePoints;
    public PolylineOptions whitePolyLine;

    public static boolean isWebServiceRunnig = false;
    public double maxSpeed = 0.0;
    public double currentLatitude, currentLongitude;
    public boolean isTripAutoStarted = false;
    public boolean isTripManualStarted = false;
    public boolean isTripEndCountDownTimerStarted = false;
    public int tripType = 2;
    public double tripMinSpeed = 0, tripMaxSpeed = 0;
    public EndTrip endTrip;
    public CurrentLocationClient currentLocationClient;
    public Date startDate;
    public Location mLastLocation;
    UpdateTripStates updateTripState;
    public ScheduledExecutorService trackLatlanPathExecutor = Executors.newScheduledThreadPool(1);
    public ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    public double currentSpeed;
    public boolean btnStatue;
    Context context;
    LocationTrackerService locationTrackerService;
    public String isPassenger = "0";

    public TripStates(Context context) {
        this.context = context;
        locationTrackerService = new LocationTrackerService();
        violatePoints = new ArrayList<>();
        endTrip = new EndTrip();
        IntentFilter intentFilter = new IntentFilter(LOCATION_TRACKING_SERVICE);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
    }

    public void setUpdateTripState(UpdateTripStates updateTripState) {
        this.updateTripState = updateTripState;
    }

    class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
//            AppLog.print(context, "Location Accuracy : "+location.getAccuracy());
//            if(location.getAccuracy()<24)
//            {
                onLocationReceived(location);
//            }
//            else
//            {
//                location.setSpeed(1.0f);
//                onLocationReceived(location);
//            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }

    public void createTimer() {
        stopTrackTimer = new TrackCountDownTimer(121000, 10000);  //10000 - 10 seconds, 120000 - 120 seconds
        stopTrackTimer.start();
    }

    public class TrackCountDownTimer extends CountDownTimer {
        public Context context;


        public TrackCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
            context = DravaApplication.getContext();
        }

        @Override
        public void onTick(long millisecToFinish) {
            long temp = millisecToFinish;
            String msg;
            if (temp > 61000) {
                temp = temp - 60000;
                msg = "1 min " + temp / 1000;
            } else {
                msg = "" + temp / 1000;
            }
            final Toast toast = Toast.makeText(context, "Speed < 10, Trip end in " + msg + " sec", Toast.LENGTH_LONG);
            toast.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 1000);
        }

        @Override
        public void onFinish() {
            //&& !getApp().getUserPreference().isEndTripWebserviceCalled()
            DravaLog.print("End WebService in Timer");
            DravaLog.print("---------------------End from Auto finish-----------------------");
            if (isTripAutoStarted) {
                callEndTripService();
            } else {
                setButtonState(false);
                isTripManualStarted = false;
                isTripEndCountDownTimerStarted = false;
            }
        }
    }

    public int getVoilationTimer() {
        int Mins = 0;
        try {
            SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss aa");
            String strCurrentTime = format.format(Calendar.getInstance().getTime());
            String strStartTime = format.format(startDate);
            Date currentTime = format.parse(strCurrentTime);
            Date startTime = format.parse(strStartTime);

            long mills = currentTime.getTime() - startTime.getTime();
            DravaLog.print("StartData1=>" + currentTime.getTime());
            DravaLog.print("EndData2=>" + startTime.getTime());
            int Hours = (int) (mills / (1000 * 60 * 60));
            Mins = (int) (mills / (1000 * 60)) % 60;
            String diff = Hours + ":" + Mins; // updated value every1 second
            DravaLog.print(diff);

            AppLog.print(context, "voilation Hours:Mins==>" + diff);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Mins;
    }

    @Override
    public void updateMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
        isWebServiceRunnig = false;
    }

    public void initLocationClient() {
        removeListener();
        myLocationListener = new MyLocationListener();
        Criteria criteria = new Criteria();
        criteria.setSpeedRequired(true);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);

        initializeLocationManager();
        String bestProvider = locationManager.getBestProvider(criteria, true);

        AppLog.print(context, "---------------------------bestProvider-------------------------" + bestProvider);
        DravaLog.print("---------------------------bestProvider-------------------------" + bestProvider);
//        currentLocationClient = new CurrentLocationClient(context,context);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(bestProvider, 2000, 0, myLocationListener);//15000 => 15 seconds
        } else {
            locationManager.requestLocationUpdates(bestProvider, 2000, 0, myLocationListener);//(bestProvider, 2000, 2000.0f, myLocationListener)
        }
        updateTripPath();
        callOSMtoCheckMaxSpeed();
    }

    private void initializeLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void updateTripPath() {
//        try {
        trackLatlanPathExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                AppLog.print(mContext, "trackLatlanPathExecutor.............Called");
                DravaLog.print("trackLatlanPathExecutor.............Called");
                if (isTripAutoStarted || isTripManualStarted) {
                    if (!TextUtils.isNullOrEmpty(getApp().getUserPreference().getTripId())) {
                        getApp().getDBSQLite().updateLatLngTripPath(Long.parseLong(getApp().getUserPreference().getTripId()), "" + currentLatitude, "" + currentLongitude, false);
                    }
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
//        } finally {
//            AppLog.print(mContext, "trackLatlanPathExecutor got Error.............Called");
//            if (trackLatlanPathExecutor != null) {
//                trackLatlanPathExecutor.shutdownNow();
//                AppLog.print(mContext, "trackLatlanPathExecutor new instance1 has created.............Called");
//                trackLatlanPathExecutor = Executors.newScheduledThreadPool(1);
//            } else {
//                AppLog.print(mContext, "trackLatlanPathExecutor new instance2 has created.............Called");
//                trackLatlanPathExecutor = Executors.newScheduledThreadPool(1);
//            }
//            updateTripPath();
//        }
    }

    public void callOSMtoCheckMaxSpeed() {
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                AppLog.print(mContext, "max Speed update scheduler.............Called");
                DravaLog.print("max Speed update scheduler.............Called");
                if (isWebServiceRunnig && (isTripAutoStarted || isTripManualStarted)) {
                    isWebServiceRunnig = false;
                }
            }
        }, 0, 20, TimeUnit.SECONDS);
    }


    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public double getCurrentSpeed() {
        return this.currentSpeed;
    }

    public void tripStarted() {
        showToast("Trip Started");
    }

    public void startTrip(String latitude, String longitude, String sTime, String type, String tDate, String isPassenger) {
       /* final String startTime = sTime;
        final String startLatitude = latitude;
        final String startLongitude = longitude;
        final String tripType = type;
        final String tripDate = tDate;*/

        Intent intent = new Intent(context, LocationTrackerService.class);
        intent.putExtra(TRIP_START_END, START);
        intent.putExtra(START_TIME, sTime);
        intent.putExtra(START_LATITUDE, latitude);
        intent.putExtra(START_LONGITUDE, longitude);
        intent.putExtra(TRIP_TYPE, "" + type);//Change Trip type :1-Manual ,2- Dyanamic
        intent.putExtra(TRIP_DATE, tDate);
        intent.putExtra(PASSENGER, isPassenger);
        DravaLog.print("HomeFragment==>Start_Time=>" + DateConversion.getCurrentDateAndTime());
        DravaLog.print("HomeFragment==>Trip_date=>" + DateConversion.getCurrentDate());
        context.startService(intent);
    }

    public void onLocationReceived(Location location) {
        if (location == null) {
            return;
        }
        if (mCurrentLatLng != null && distanceBetween(mCurrentLatLng, new LatLng(location.getLatitude(), location.getLongitude())) < /*0.1f*/5.0f) {
            AppLog.print(mContext,"Came to below 5m=>>" + distanceBetween(mCurrentLatLng, new LatLng(location.getLatitude(), location.getLongitude())));
            currentSpeed = 0;
        } else {
            if (location.hasSpeed()) {
                currentSpeed = Math.round(location.getSpeed()) * 3.6f;
            }
        }
//        Vibrator viberate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//        viberate.vibrate(100);
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        mCurrentLatLng = new LatLng(currentLatitude, currentLongitude);
//        animateToCurrentLocation();
//        double currentSpeed = Math.round(location.getSpeed()) * 3.6f;

        if (currentSpeed > tripMaxSpeed) {
            tripMaxSpeed = currentSpeed;
        } else if (currentSpeed < tripMinSpeed) {
            tripMinSpeed = currentSpeed;
        }

        if (isTripEndCountDownTimerStarted) {
            if (currentSpeed > TRACKING_START_SPEED && stopTrackTimer != null) {
                isTripEndCountDownTimerStarted = false;
                stopTrackTimer.cancel();
            }
        }

//        txtCurrentSpeed.setText(String.format(getString(R.string.your_speed), String.valueOf(currentSpeed)));
//        this.currentSpeed = currentSpeed;
//        setCurrentSpeed(currentSpeed);
//        txtSpeed.setText((int) currentSpeed + "");
//        speedometerView.setSpeed(currentSpeed);

        AppLog.print(context, "TripStates isTripAutoStarted 1 ===================>" + isTripAutoStarted + "    currentSpeed===>  " + currentSpeed + "    isTripManualStarted===>  " + isTripManualStarted);

        //To maintain trip start in all fragment
        if ((!isTripAutoStarted && currentSpeed >= TRACKING_START_SPEED && !isTripManualStarted) ||
                (!isTripAutoStarted && currentSpeed >= TRACKING_START_SPEED && isTripManualStarted)) {  // && currentSpeed >= TRACKING_START_SPEED
            // call Trip create webservice
            tripMinSpeed = 0;
            tripMaxSpeed = 0;

            startDate = Calendar.getInstance().getTime();
            tripStartLatLng = new LatLng(currentLatitude, currentLongitude);
            isTripAutoStarted = true;
            isTripManualStarted = true;
            AppLog.print(context, "TripStates isTripAutoStarted 2 ===================>" + isTripAutoStarted);
            setButtonState(true);

//            DEV
//            final Toast trip_toast = Toast.makeText(context, "Trip going to start", Toast.LENGTH_SHORT);
//            trip_toast.show();
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    trip_toast.cancel();
//                }
//            }, 1000);

            startTrip(String.valueOf(mCurrentLatLng.latitude), String.valueOf(mCurrentLatLng.longitude),
                    DateConversion.getCurrentDateAndTime(), "" + tripType, DateConversion.getCurrentDate(), isPassenger);

//            if (updateTripState != null) {
//                updateTripState.clearMap();
//            }

        } else if (isTripAutoStarted && currentSpeed < TRACKING_START_SPEED && !isTripEndCountDownTimerStarted) {
            isTripEndCountDownTimerStarted = true;
            createTimer();  //10000 - 10 seconds, 120000 - 120 seconds
        }

        if (updateTripState != null) {
            updateTripState.updateView();   //This will call updateView() in HomeActivity for every 2 sec once the trip started
        }

        double radiusDegrees = 10.0;
        LatLng center = new LatLng(currentLatitude, currentLongitude);

        LatLng southwest = SphericalUtil.computeOffset(center, radiusDegrees * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radiusDegrees * Math.sqrt(2.0), 45);


        /*if (mMap != null) {
            mMap.addPolyline(whitePolyLine);
        }*/
        if (DeviceUtils.isInternetConnected(mContext)) {
            if (maxSpeed <= 5) {
                checkSpeedLimit(southwest, northeast);
            } else if (isTripAutoStarted) {
                if (currentSpeed > maxSpeed && isPassenger.equals("0")) {
                    if (getVoilationTimer() >= 1) {  //5
                        startDate = Calendar.getInstance().getTime();
                        showToast("Trip has voilated");
                        if (updateTripState != null) {
                            updateTripState.updateViolatedPoint();      //This will call updateVoilatedPoint() in HomeActivity once trip violated
                        }
                        if (!TextUtils.isNullOrEmpty(getApp().getUserPreference().getTripId())) {
                            getApp().getDBSQLite().updateLatLngTripPath(Long.parseLong(getApp().getUserPreference().getTripId()), "" + currentLatitude, "" + currentLongitude, true);
                        }

                        if (!violatePoints.contains(center)) {
                            violatePoints.add(center);
                            int tempTripId = getApp().getDBSQLite().getServerIdforEndTrip(Long.parseLong(getApp().getUserPreference().getTripId()));
                            if (tempTripId == 0) {
                                // ToDo check for all offline data has inserted and then insert voilate
                            }
//                        mMap.addMarker(new MarkerOptions().position(center).icon(BitmapDescriptorFactory.defaultMarker()));
                            String tripId = "" + getApp().getDBSQLite().getServerIdforEndTrip(Long.parseLong(getApp().getUserPreference().getTripId()));
//                        locationTrackerService.tripVoilate(tripId,String.valueOf(maxSpeed),String.valueOf(currentSpeed),String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()));

                            Intent intent = new Intent(context, LocationTrackerService.class);
                            intent.putExtra(TRIP_START_END, VOILATE);
                            intent.putExtra(ROAD_SPEED, String.valueOf(maxSpeed));
                            intent.putExtra(VEHICLE_SPEED, String.valueOf(currentSpeed));
                            intent.putExtra(START_LATITUDE, String.valueOf(location.getLatitude()));
                            intent.putExtra(START_LONGITUDE, String.valueOf(location.getLongitude()));
                            context.startService(intent);
                        }
                    }
                }
            }
            checkSpeedLimit(southwest, northeast);
        }else {
            maxSpeed = 0;
        }
        AppLog.print(context, "HomeFragment==>tripMaxSpeed-->" + tripMaxSpeed + " Road speed -->" + maxSpeed + " CurrentSpeed -->" + currentSpeed
                + "location.getSpeed() =>" + location.getSpeed() + " Current Latitude==>" + currentLatitude + " Current Longitude==>" + currentLongitude);
        assingEndTripValues();
    }

    public void checkSpeedLimit(LatLng southwest, LatLng northeast) {
        if (!isWebServiceRunnig) {
//            Log.e("calling", "calling OSM webService");
            isWebServiceRunnig = true;
            if (DeviceUtils.isInternetConnected(context)) {
                new TaskFetchRoadSpeedLimit(this, southwest, northeast, context).execute();
            }
        }
    }

    public float distanceBetween(LatLng latLng1, LatLng latLng2) {
        Location loc1 = new Location(LocationManager.GPS_PROVIDER);
        Location loc2 = new Location(LocationManager.GPS_PROVIDER);
        loc1.setLatitude(latLng1.latitude);
        loc1.setLongitude(latLng1.longitude);
        loc2.setLatitude(latLng2.latitude);
        loc2.setLongitude(latLng2.longitude);
        return loc1.distanceTo(loc2);
    }

    public void assingEndTripValues() {
        if (tripStartLatLng != null && !TextUtils.isNullOrEmpty(getApp().getUserPreference().getTripId())) {
            Location startLocation = new Location("Start Location");
            startLocation.setLatitude(tripStartLatLng.latitude);
            startLocation.setLongitude(tripStartLatLng.longitude);
            Location endLocation = new Location("End Location");
            endLocation.setLatitude(mCurrentLatLng.latitude);
            endLocation.setLongitude(mCurrentLatLng.longitude);

            List<TrackTripPath> trackTripPathList = getApp().getDBSQLite().getTrackTripPathInfo(Long.valueOf(getApp().getUserPreference().getTripId())/*.getDBSQLite().getFirstTripId()*/);
            float tripDistance;
            if (trackTripPathList.size() > 0) {
                AppLog.print(context, "CameTo=>TrackTripPathList=>" + trackTripPathList.size());
                //To draw start log tracking to end LatLng
                tripDistance = distanceBetween(tripStartLatLng, new LatLng(Double.valueOf(trackTripPathList.get(0).Latitude)
                        , Double.valueOf(trackTripPathList.get(0).Longitude)));


                for (int i = 0; i < trackTripPathList.size(); i++) {
                    int j = i + 1;
                    if (j <= trackTripPathList.size() - 1) {

                        tripDistance += distanceBetween(new LatLng(Double.valueOf(trackTripPathList.get(i).Latitude)
                                        , Double.valueOf(trackTripPathList.get(i).Longitude)),
                                new LatLng(Double.valueOf(trackTripPathList.get(j).Latitude)
                                        , Double.valueOf(trackTripPathList.get(j).Longitude)));
                    }
                }
                //To draw end log tracking to end LatLng
                tripDistance += distanceBetween(new LatLng(Double.valueOf(trackTripPathList.get(trackTripPathList.size() - 1).Latitude)
                                , Double.valueOf(trackTripPathList.get(trackTripPathList.size() - 1).Longitude))
                        , mCurrentLatLng);
                tripDistance = tripDistance / 1000;

            } else {
                tripDistance = startLocation.distanceTo(endLocation) / 1000;
            }

            AppLog.print(context, "HomeFragment==>distance==>" + tripDistance);
//            DravaLog.print("HomeFragment==>distance==>" + tripDistance);
//            DravaLog.print("HomeFragment==>tripMaxSpeed==>" + (int) tripMaxSpeed);
//            DravaLog.print("HomeFragment==>tripMinSpeed==>" + (int) tripMinSpeed);
//            DravaLog.print("HomeFragment==>End_time==>" + DateConversion.getCurrentDateAndTime());
//            DravaLog.print("HomeFragment-->RoadMaxSpeed==>" + context.maxSpeed);
//            DravaLog.print("HomeFragment==>Current Latitude==>" + (int) currentLatitude);
//            DravaLog.print("HomeFragment==>Current Longitude==>" + (int) currentLongitude);

            endTrip.EndTime = DateConversion.getCurrentDateAndTime();
            endTrip.EndLatitude = String.valueOf(currentLatitude);
            endTrip.EndLongitude = String.valueOf(currentLongitude);
            endTrip.MaxSpeed = String.valueOf((int) tripMaxSpeed);
            endTrip.MinSpeed = String.valueOf((int) tripMinSpeed);
            endTrip.Distance = String.valueOf(tripDistance);
            if (endTrip != null) {
                getApp().getUserPreference().setEndTripInfo(endTrip);
            }
        }
    }

    public void setButtonState(boolean btnPlay) {
        this.btnStatue = btnPlay;
        if (updateTripState != null) {
            updateTripState.setButtonState(btnStatue);      //This will call setButtonState() in HomeActivity once the button state has changed
        }
    }

    public boolean getButtonState() {
        return this.btnStatue;
    }

    public void callEndTripService() {
        AppLog.print(context, "TripStates isTripAutoStarted 3 ===================>" + isTripAutoStarted);
        isTripAutoStarted = false;
        isTripManualStarted = false;
        isTripEndCountDownTimerStarted = false;
        setButtonState(false);
        assingEndTripValues();
        if (!TextUtils.isEmpty(getApp().getUserPreference().getTripId())) {
            final String tripId = getApp().getUserPreference().getTripId();
            Intent intent = new Intent(context, LocationTrackerService.class);
            intent.putExtra(END_TRIP, getApp().getUserPreference().getEndTripInfo());
            intent.putExtra(TRIP_START_END, END);
            context.startService(intent);
            showToast("Trip End Called");
        } else {
            showToast("Trip id not available");
        }
        if (updateTripState != null) {      //This will call clearMap() in HomeActivity once the trip got ended
            updateTripState.clearMap();
        }
    }

    public boolean getisTripStarted(){
        return isTripAutoStarted;
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Result after location service ends
            boolean status = intent.getBooleanExtra(STATUS, false);
            if (status) {
                showToast("Service Success");
            } else {
                showToast("Service Fails");
            }
        }
    };

    public void showToast(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void removeListener() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (locationManager != null && myLocationListener != null) {
            locationManager.removeUpdates(myLocationListener);
            AppLog.print(mContext, "==>removeListener==>");
        }
    }
}
