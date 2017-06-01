package com.drava.android.activity.map.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.drava.android.DravaApplication;
import com.drava.android.base.AppConstants;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AppLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;

import static com.drava.android.DravaApplication.getApp;
import static com.drava.android.DravaApplication.mContext;

public class CurrentLocationUpdateService extends Service {

    public LocationManager locationManager;
    private final long LOCATION_INTERVAL = 1000 * 10L;
    private final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    String TAG = CurrentLocationUpdateService.class.getSimpleName();
    ScheduledExecutorService updateUserCurrentLocService = Executors.newScheduledThreadPool(1);
    public Location lastKnownLocation;

    public CurrentLocationUpdateService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class CustomLocationListener implements LocationListener, AppConstants {

        Location currentLocation;

        public CustomLocationListener(String provider){
            updateUserCurrentLocService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if(currentLocation != null){
                        updateUserCurrentLocation();
                    }
                }
            },0, 3, TimeUnit.MINUTES); //change to Minutes 5
        }

        public CustomLocationListener(){

        }

        public void updateUserCurrentLocation(){
            if(null == currentLocation ) {
                currentLocation = lastKnownLocation;
            }
            if(currentLocation != null && currentLocation.getLatitude()>0 && currentLocation.getLongitude()>0) {
                Log.e(TAG, "Current Location onChanged: ");
                Log.e(TAG, "" + currentLocation.getLatitude() + " " + currentLocation.getLongitude());
                getApp().getRetrofitInterface().setCurrentLocation(""+currentLocation.getLatitude(), ""+currentLocation.getLongitude()).enqueue(new RetrofitCallback<ResponseBody>() {
                    @Override
                    public void onSuccessCallback(Call<ResponseBody> call, String content) {
                        super.onSuccessCallback(call, content);
                        Log.e("","Current Location updated");
//                        Toast.makeText(getApplicationContext(),"Current Location updated",Toast.LENGTH_SHORT).show();
                        AppLog.print(CurrentLocationUpdateService.this.getApplicationContext(), "Current Location update :"+ currentLocation.getLatitude()+"  :  "+currentLocation.getLongitude());
                    }

                    @Override
                    public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                        super.onFailureCallback(call, t, message, code);
                        AppLog.print(getApplicationContext(), "Current Location Failed :"+message);
//                        Toast.makeText(getApplicationContext(),"Current Location Failed",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            if(location != null) {
                currentLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String provider, int i, Bundle bundle) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            if(provider.equalsIgnoreCase(LocationManager.GPS_PROVIDER)) {
                if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {

                    Intent intent = new Intent(GPS_DISABLED);
                    LocalBroadcastManager.getInstance(CurrentLocationUpdateService.this.getApplicationContext()).sendBroadcast(intent);

                    Log.e(TAG, "onProviderDisabled: " + provider);
                    getApp().getRetrofitInterface().updateGPSStatus("1", "0","0","1").enqueue(new RetrofitCallback<ResponseBody>() {
                        @Override
                        public void onSuccessCallback(Call<ResponseBody> call, String content) {
                            super.onSuccessCallback(call, content);
                            Log.e(TAG, "GPS Notification updated Success");
                        }

                        @Override
                        public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                            super.onFailureCallback(call, t, message, code);
                            Log.e(TAG, "GPS Notification updated Failed");
                        }
                    });
                }


            }
        }
    }

    CustomLocationListener[] customLocationListeners = new CustomLocationListener[]{
            new CustomLocationListener(LocationManager.GPS_PROVIDER)
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "Service created");
        initializeLocationManager();

        try{
            if(ActivityCompat.checkSelfPermission(CurrentLocationUpdateService.this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(CurrentLocationUpdateService.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, MIN_DISTANCE_CHANGE_FOR_UPDATES, customLocationListeners[0]);
            lastKnownLocation = getInitialLocation();
            customLocationListeners[0].updateUserCurrentLocation();

        }catch (java.lang.SecurityException e){
            Log.e(TAG, e.toString());
        }catch (java.lang.IllegalArgumentException e){
            Log.e(TAG, e.toString());
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onDestroy() {
        removeListener();
        super.onDestroy();
    }

    private void initializeLocationManager(){
        if(locationManager == null){
            locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public DravaApplication getApp() {
        return (DravaApplication) getApplication();
    }

    public void removeListener() {
        if (ActivityCompat.checkSelfPermission(CurrentLocationUpdateService.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(CurrentLocationUpdateService.this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Log.e(TAG, "CurrentLocationUpdateService====>unregisterReceiver location listener Called");
        if (locationManager != null && customLocationListeners[0] != null ) {
            locationManager.removeUpdates(customLocationListeners[0]);
            AppLog.print(mContext, "==>Current Location Update Service GPS removeListener==>");
        }
    }

    public Location getInitialLocation() {
        Location location = null;
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                AppLog.print(mContext, "==>Initial Location provider are disabled-----");

            } else {
                if (ActivityCompat.checkSelfPermission(CurrentLocationUpdateService.this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(CurrentLocationUpdateService.this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(	LocationManager.NETWORK_PROVIDER,
                            LOCATION_INTERVAL,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, new CustomLocationListener());
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    if(location != null) {
                        AppLog.print(mContext, "Initial Location From Network provider are :" + location.getLatitude() + "  " + location.getLongitude());
                        Log.e("LocationTracker", "Initial Location From Network provider are :" + location.getLatitude() + "  " + location.getLongitude());
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(	LocationManager.GPS_PROVIDER,
                                LOCATION_INTERVAL,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, new CustomLocationListener());
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                    if(location != null) {
                        AppLog.print(mContext, "Initial Location From GPS provider are :" + location.getLatitude() + "  " + location.getLongitude());
                        Log.e("LocationTracker", "Initial Location From Network provider are :" + location.getLatitude() + "  " + location.getLongitude());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }
}
