package com.drava.android.utils;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.drava.android.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class CurrentLocationClient implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    static final String TAG = "CurrentLocationClient";
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String mCurrentAddress;
    //private Context mContext;
    private SCOPE mScope;
    private OnLocationReceivedListener mOnLocationReceivedCallback;
    private OnAddressReceivedListener mOnAddressReceivedListener;
    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private Context context;
    public CurrentLocationClient(Context context, SCOPE scope, OnLocationReceivedListener listener) {
        this(context, scope, listener, null);
    }

    public CurrentLocationClient(Context context, SCOPE scope, OnLocationReceivedListener listener,
                                 OnAddressReceivedListener addressListener) {
        mOnLocationReceivedCallback = listener;
        mOnAddressReceivedListener = addressListener;
        init(context, scope);
    }

    public void setAddressReceivedListener(OnAddressReceivedListener addressListener) {
        mOnAddressReceivedListener = addressListener;
    }

    public CurrentLocationClient(Context context, SCOPE scope) {
        init(context, scope);
    }

    private void init(Context context, SCOPE scope) {
        mScope = scope;
        mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(LocationServices.API)
                .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
        this.context=context;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Location client connected");
        startLocationUpdates(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            promptSettings();
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//        Location location = LocationServices.FusedLocationApi.getLocationAvailability(mGoogleApiClient);
        if (location != null) {
            onLocationReceived(location);
        } else {
            LocationRequest request = new LocationRequest();
            request.setNumUpdates(1);
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
        }
        startLocationUpdates(context);
    }

    private void goToSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myAppSettings);

    }


    @Override
    public void onConnectionSuspended(int arg0) {
        Log.d(TAG, "Location client disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "Location client failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            onLocationReceived(location);
        }
    }

    private void onLocationReceived(Location location) {
        mCurrentLocation = location;
        reset();
        if (mOnLocationReceivedCallback != null) {
            mOnLocationReceivedCallback.onLocationReceived(location);
        }
    }

    /**
     * Call this method when destroy activity
     */
    public void reset() {
        Log.d(TAG, "Location client reset");
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }
            mGoogleApiClient.disconnect();
        }
    }

    public Location getCurrentLocation() {
        return mCurrentLocation;
    }

    public String getCurrentAddress() {
        return mCurrentAddress;
    }

    public enum SCOPE {
        LOCATION, LOCATION_AND_ADDRESS;
    }

    public interface OnLocationReceivedListener {
        void onLocationReceived(Location location);
    }

    public interface OnAddressReceivedListener {
        void onAddressReceive(String address);
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates(Context context) {
        // Create the location request
        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(UPDATE_INTERVAL)
                        .setFastestInterval(FASTEST_INTERVAL);
                // Request location updates

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
//                    promptSettings();
                    return;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

}
