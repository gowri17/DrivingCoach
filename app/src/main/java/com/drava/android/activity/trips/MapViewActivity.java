package com.drava.android.activity.trips;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drava.android.R;
import com.drava.android.activity.map.maputils.MapStateListener;
import com.drava.android.activity.map.maputils.TouchableMapFragment;
import com.drava.android.activity.mentor_mentee.MentorListParser;
import com.drava.android.base.BaseActivity;
import com.drava.android.parser.SnapRoadParser;
import com.drava.android.parser.TripDetails;
import com.drava.android.parser.TripViolationDetailParser;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DateConversion;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.TextUtils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.GeoApiContext;
import com.google.maps.RoadsApi;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;
import com.google.maps.model.SnappedPoint;
import com.google.maps.model.SpeedLimit;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;

import static com.google.maps.internal.StringJoin.join;

/**
 * Created by admin on 11/23/2016.
 */


public class MapViewActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private static final float ZOOM_LEVEL = 15.0f;
    private TouchableMapFragment mapFragment;
    private GoogleMap mMap;
    private LatLng startLatLng, endLatLng;
    private ArrayList<Polyline> polylineList = new ArrayList<>();
    private TripListParser.MenteeTripList menteeTripList;
    private TextView txtTripCreatedDate, txtTripStartLocation, txtTripEndLocation, txtTripStartTime, txtTripEndTime, txtVoilationCount, txtTripDuration, txtTripDistance, txtShareOnFacebook;
    private List<LocationTrackingParser.LocationTracking> mLocationTrackingList = new ArrayList<>();
    private int start = 0;
    private List<TripViolationDetailParser.TripViolationDetail> tripViolationDetailList = new ArrayList<>();
    private HashMap<Marker, TripViolationDetailParser.TripViolationDetail> voilatedMarkerHashMap = new HashMap<Marker, TripViolationDetailParser.TripViolationDetail>();
    private ProgressBar mProgressBar;
    private ImageView imgClose;
    private RelativeLayout rlVoilateLabel;
    private String profilePhoto;
    private MentorListParser.MentorList mentorMenteeList;
    private TripDetails tripDetailParser;
    private LinearLayout rlRootView;
    //Snap to road
    List<com.google.maps.model.LatLng> mCapturedLocations = new ArrayList<>();
    List<SnappedPoint> mSnappedPoints = new ArrayList<>();
    List<SnapRoadParser.ParserSnappedPoint> mSnapPoints = new ArrayList<>();
    Map<String, SpeedLimit> mPlaceSpeeds;
    private GeoApiContext mContext;
    /* The number of points allowed per API request. This is a fixed value.*/
    private static final int PAGE_SIZE_LIMIT = 100;
    /* Define the number of data points to re-send at the start of subsequent requests. This helps
     to influence the API with prior data, so that paths can be inferred across multiple requests.
     You should experiment with this value for your use-case.*/
    private static final int PAGINATION_OVERLAP = 5;
    private CallbackManager callbackManager;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_map_view);
        init();
    }

    private void init() {

        setToolbar("Map View");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        setStatusBarColor();
        txtTripCreatedDate = (TextView) findViewById(R.id.txt_trip_date);
        txtTripStartTime = (TextView) findViewById(R.id.txt_trip_start_time);
        txtTripStartLocation = (TextView) findViewById(R.id.txt_trip_start_location);
        txtTripEndTime = (TextView) findViewById(R.id.txt_trip_end_time);
        txtTripEndLocation = (TextView) findViewById(R.id.txt_trip_end_location);
        txtVoilationCount = (TextView) findViewById(R.id.txt_trip_violate_count);
        txtTripDistance = (TextView) findViewById(R.id.txt_trip_distance);
        txtTripDuration = (TextView) findViewById(R.id.txt_trip_duration);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        rlVoilateLabel = (RelativeLayout) findViewById(R.id.rl_violation_details);
        imgClose = (ImageView) findViewById(R.id.img_close);
        menteeTripList = (TripListParser.MenteeTripList) getIntent().getSerializableExtra("menteeTripList");
        mapFragment = (TouchableMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);
        rlRootView = (LinearLayout) findViewById(R.id.rl_root_view);
        txtShareOnFacebook = (TextView) findViewById(R.id.txt_share_on_facebook);
        mapFragment.getMapAsync(this);
        mContext = new GeoApiContext().setApiKey(getString(R.string.road_api_key));
        if (menteeTripList != null) {
            txtTripCreatedDate.setText(DateConversion.formatDate(menteeTripList.TripDate, "yyyy-MM-dd", "dd MMMM yyyy"));
            txtTripStartTime.setText(DateConversion.getTimeFromDate(DateConversion.stringToDate(menteeTripList.StartTime, "yyyy-MM-dd HH:mm:ss"), "HH:mm"));
            txtTripEndTime.setText(DateConversion.getTimeFromDate(DateConversion.stringToDate(menteeTripList.EndTime, "yyyy-MM-dd HH:mm:ss"), "HH:mm"));
            txtTripStartLocation.setText(menteeTripList.StartLocation);
            txtTripEndLocation.setText(menteeTripList.EndLocation);
            txtTripDistance.setText(menteeTripList.Distance + " km");
            txtVoilationCount.setText(menteeTripList.ViolationCount);
            txtTripDuration.setText(menteeTripList.Hours + ":" + menteeTripList.Minutes + ":" + menteeTripList.Seconds);
            if (!TextUtils.isNullOrEmpty(menteeTripList.StartLatitude) && !TextUtils.isNullOrEmpty(menteeTripList.StartLongitude)
                    && !TextUtils.isNullOrEmpty(menteeTripList.EndLatitude) && !TextUtils.isNullOrEmpty(menteeTripList.EndLongitude)) {
                startLatLng = new LatLng(Double.valueOf(menteeTripList.StartLatitude), Double.valueOf(menteeTripList.StartLongitude));
                endLatLng = new LatLng(Double.valueOf(menteeTripList.EndLatitude)
                        , Double.valueOf(menteeTripList.EndLongitude));
            } else {
                Toast.makeText(this, "Trip Not ended", Toast.LENGTH_SHORT).show();
            }
        }
        if (getIntent() != null) {
            if (getIntent().hasExtra(PROFILE_PHOTO)) {
                profilePhoto = getIntent().getStringExtra(PROFILE_PHOTO);
                if (!TextUtils.isEmpty(profilePhoto)) {
                    setUserImage(profilePhoto);
                }
            }
        }
//        DeviceUtils.setSystemUiVisibility(rlRootView);


    }

    private void setUpEvents() {
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rlVoilateLabel.setVisibility(View.GONE);
            }
        });
        txtShareOnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                configureFacebook();
            }
        });
    }


 /*   private void callTrackingWebservice() {
//
//        List<TestModel> testList=new ArrayList<TestModel>();
//
////        testList.add(new TestModel("3.16394614","101.71619293"));
//        testList.add(new TestModel("3.16361624","101.71830771"));
//        testList.add(new TestModel("3.16388005","101.71809602"));
//        testList.add(new TestModel("3.16417597","101.71774252"));
//        testList.add(new TestModel("3.16378893","101.7175578"));
//        testList.add(new TestModel("3.16330609","101.7175578"));
//        testList.add(new TestModel("3.1635356","101.71625221"));
////        testList.add(new TestModel("3.16361624","101.71830771"));
////        testList.add(new TestModel("11.95624078","79.82059154"));
////        testList.add(new TestModel("11.95624082","79.82059163"));
//        Collections.reverse(testList);
//        testList.add(0,new TestModel("3.16394614","101.71619293"));
//        testList.add(testList.size(),new TestModel("3.16361624","101.71830771"));
//        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(testList.get(0).lat)
//                ,Double.valueOf(testList.get(0).lng)))
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.trip_start_map_icon)));
//        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(testList.get(testList.size()-1).lat)
//                ,Double.valueOf(testList.get(testList.size()-1).lng)))
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.trip_end_map_icon)));
//        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(testList.get(0).lat)
//                        ,Double.valueOf(testList.get(0).lng)), ZOOM_LEVEL);
//                    mMap.animateCamera(update);
//
//        for (int i = 0;i<testList.size();i++){
//            int j = i+1;
//            if(j <= testList.size()-1){
////                String url = getDirectionsUrl(new LatLng(Double.valueOf(testList.get(i).lat)
////                                ,Double.valueOf(testList.get(i).lng)),
////                        new LatLng(Double.valueOf(testList.get(j).lat)
////                                ,Double.valueOf(testList.get(j).lng)));
////                DownloadTask downloadTask = new DownloadTask(false);
////                // Start downloading json data from Google Directions API
////                downloadTask.execute(url);
//                ArrayList<LatLng> points = new ArrayList<>();
//                PolylineOptions lineOptions =new PolylineOptions();
//                points.add(new LatLng(Double.valueOf(testList.get(i).lat)
//                                ,Double.valueOf(testList.get(i).lng)));
//                points.add(new LatLng(Double.valueOf(testList.get(j).lat)
//                                ,Double.valueOf(testList.get(j).lng)));
//                // Adding all the points in the route to LineOptions
//                lineOptions.addAll(points);
//                lineOptions.width(10);
//                lineOptions.color(Color.BLUE);
//                mMap.addPolyline(lineOptions);
//            }
//        }
        if (DeviceUtils.isInternetConnected(this)) {
            mProgressBar.setVisibility(View.VISIBLE);
//            getApp().getUserPreference().setAccessToken("c769ermQ07ooXjH40qKIBEUV811joQS8ZdW4DaLl");
//            getApp().getRetrofitInterface().getLocationTracking(menteeTripList.TripId, String.valueOf(start),menteeTripList.MenteesId*//*"4"*//*).enqueue(new RetrofitCallback<ResponseBody>() {
            getApp().getRetrofitInterface().getTripDetails(menteeTripList.TripId).enqueue(new RetrofitCallback<ResponseBody>() {
                @Override
                public void onSuccessCallback(Call<ResponseBody> call, String content) {
                    AppLog.print(MapViewActivity.this, "onSuccessCallback=>content=>" + content);
                    LocationTrackingParser locationTrackingParser = new Gson().fromJson(content, LocationTrackingParser.class);
                    if (locationTrackingParser.locationTrackingList != null && locationTrackingParser.locationTrackingList.size() > 0) {
                        start += locationTrackingParser.meta.ListedCount;
                        if (locationTrackingParser.meta.TotalCount > start) {
                            mLocationTrackingList.addAll(locationTrackingParser.locationTrackingList);
                            callTrackingWebservice();
                        } else {
                            if (mLocationTrackingList.size() == 0) {
                                mLocationTrackingList = locationTrackingParser.locationTrackingList;
                            } else {
                                mLocationTrackingList.addAll(locationTrackingParser.locationTrackingList);
                            }
                            drawTrackPath();
                        }
                    } else if (locationTrackingParser.meta.code == 1016) {
                        //If no Tracking Location
                        Toast.makeText(MapViewActivity.this, "" + locationTrackingParser.meta.errorMessage, Toast.LENGTH_SHORT).show();
                        mMap.addMarker(new MarkerOptions().position(startLatLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.trip_start_map_icon)));

                        mMap.addMarker(new MarkerOptions().position(endLatLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.trip_end_map_icon)));
                        ArrayList<LatLng> points = new ArrayList<>();
                        PolylineOptions lineOptions = new PolylineOptions();
                        points.add(startLatLng);
                        points.add(endLatLng);
                        // Adding all the points in the route to LineOptions
                        lineOptions.addAll(points);
                        lineOptions.width(10);
                        lineOptions.color(Color.BLUE);
                        mMap.addPolyline(lineOptions);
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(startLatLng);
                        builder.include(endLatLng);
                        LatLngBounds bounds = builder.build();
                        int padding = ((mapFragment.getView().getWidth() * 10) / 100); // offset from edges of the map
                        // in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                                padding);
                        mMap.animateCamera(cu);
                        mProgressBar.setVisibility(View.GONE);
                    }
                    super.onSuccessCallback(call, content);
                }

                @Override
                public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                    super.onFailureCallback(call, t, message, code);
                    AppLog.print(MapViewActivity.this, "onFailureCallback=>message=>" + message);
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        } else {
            AlertUtils.showAlert(this, getResources().getString(R.string.check_your_internet_connection));
        }
    }*/

    public void getTripTrackPathDetails() {
        if (DeviceUtils.isInternetConnected(this)) {
            mProgressBar.setVisibility(View.VISIBLE);
            getApp().getRetrofitInterface().getTripDetails(menteeTripList.TripId).enqueue(new RetrofitCallback<ResponseBody>() {
                @Override
                public void onSuccessCallback(Call<ResponseBody> call, String content) {
                    AppLog.print(MapViewActivity.this, "getTripDetails===================>onSuccessCallback=========>");
                    tripDetailParser = new Gson().fromJson(content, TripDetails.class);
                    if (tripDetailParser != null && tripDetailParser.meta.code == 200 && tripDetailParser.TripDetail.size() > 0) {
                        if (!tripDetailParser.TripDetail.get(0).TripLocation.equals("0") && !TextUtils.isNullOrEmpty(tripDetailParser.TripDetail.get(0).TripLocation)) {
                            String temp = tripDetailParser.TripDetail.get(0).TripLocation;
//                            String temp = "[{\"Latitude\":\"3.159504317928301\",\"Longitude\":\"101.64712811210633\",\"TripId\":\"786\",\"IsViolation\":\"0\"},{\"Latitude\":\"3.1593418767691865\",\"Longitude\":\"101.64708215194285\",\"TripId\":\"786\",\"IsViolation\":\"0\"},{\"Latitude\":\"3.1591774614093207\",\"Longitude\":\"101.64689350408476\",\"TripId\":\"786\",\"IsViolation\":\"0\"},{\"Latitude\":\"3.1590977006795944\",\"Longitude\":\"101.64676266413771\",\"TripId\":\"786\",\"IsViolation\":\"0\"},{\"Latitude\":\"3.158943684281007\",\"Longitude\":\"101.64657837761136\",\"TripId\":\"786\",\"IsViolation\":\"0\"}]";
                            temp = temp.replaceAll("\\\\", "");
                            temp = temp.replace("\"[", "[");
                            temp = temp.replace("]\"", "]");

                            Log.e("Location Response", temp);

                            Type fooType = new TypeToken<List<LocationTrackingParser.LocationTracking>>() {
                            }.getType();
                            mLocationTrackingList = new Gson().fromJson(temp, fooType);
                            if (mLocationTrackingList != null && mLocationTrackingList.size() > 0) {
                                /*==========Snap to Road========*/
                                /*for (LocationTrackingParser.LocationTracking locationTracking : mLocationTrackingList) {
                                    if (!locationTracking.IsViolation.equals("1"))
                                        mCapturedLocations.add(new com.google.maps.model.LatLng(Double.valueOf(locationTracking.Latitude), Double.valueOf(locationTracking.Longitude)));
                                }*/
                                drawTrackPath();
//                                mTaskSnapToRoads.execute();

                            } else {
                                Toast.makeText(MapViewActivity.this, "No Location Details!!", Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.GONE);
                            }
                        } else {
                            Toast.makeText(MapViewActivity.this, "No Location Details!!", Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.GONE);
                        }
                    } else if (tripDetailParser.meta.code == 1016) {
                        //If no Tracking Location
                        Toast.makeText(MapViewActivity.this, "" + tripDetailParser.meta.errorMessage, Toast.LENGTH_SHORT).show();
                        mMap.addMarker(new MarkerOptions().position(startLatLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.trip_start_map_icon)));

                        mMap.addMarker(new MarkerOptions().position(endLatLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.trip_end_map_icon)));
                        ArrayList<LatLng> points = new ArrayList<>();
                        PolylineOptions lineOptions = new PolylineOptions();
                        points.add(startLatLng);
                        points.add(endLatLng);
                        // Adding all the points in the route to LineOptions
                        lineOptions.addAll(points);
                        lineOptions.width(10);
                        lineOptions.color(Color.BLUE);
//                        mMap.addPolyline(lineOptions);
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(startLatLng);
                        builder.include(endLatLng);
                        LatLngBounds bounds = builder.build();
                        int padding = ((mapFragment.getView().getWidth() * 10) / 100); // offset from edges of the map
                        // in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                                padding);
                        mMap.animateCamera(cu);
                        mProgressBar.setVisibility(View.GONE);
                    }
                    super.onSuccessCallback(call, content);
                }

                @Override
                public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                    super.onFailureCallback(call, t, message, code);
                    if (t != null && t.getCause() != null) {
                        AppLog.print(MapViewActivity.this, "getTripDetails=====>onFailureCallback======>Cause===>" + t.getCause());
                    }
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        } else {
            AlertUtils.showAlert(this, getResources().getString(R.string.check_your_internet_connection));
        }
    }

    private void drawTrackPath() {
        //If Tracking location are available
        Collections.reverse(mLocationTrackingList);
        //Draw from start latlng to first  log tracking

        /*To Draw Polyline*/
        /*ArrayList<LatLng> points = new ArrayList<>();
        PolylineOptions lineOptions = new PolylineOptions();
        lineOptions.width(10);
        lineOptions.color(Color.BLUE);
        points.add(startLatLng);
        points.add(new LatLng(Double.valueOf(mLocationTrackingList.get(0).Latitude)
                , Double.valueOf(mLocationTrackingList.get(0).Longitude)));*/

        mMap.addMarker(new MarkerOptions().position(startLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_marker)));
        // Adding all the points in the route to LineOptions
        mMap.addMarker(new MarkerOptions().position(startLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.trip_start_map_icon)));
        /*==================Snap to Road=========================*/
      /*  int offset = 0;
        while (offset < mCapturedLocations.size()) {
            int upperBound = Math.min(offset + PAGE_SIZE_LIMIT, mCapturedLocations.size());
            DravaLog.print("offset==>" + offset + "UpperBound==>" + upperBound);
            com.google.maps.model.LatLng[] page = mCapturedLocations
                    .subList(offset, upperBound)
                    .toArray(new com.google.maps.model.LatLng[upperBound - offset]);
            String path = join('|', page);
            DravaLog.print("page.length==>" + page.length + "path==>" + path);
            offset = upperBound;
            callDownLoadTask(path, false, null);
        }*/
        /*==================Local Tracking=========================*/
        for (int i = 0; i < mLocationTrackingList.size(); i++) {
             /*====================To Draw Polyline==================*/
//          /*  points.add(new LatLng(Double.valueOf(mLocationTrackingList.get(i).Latitude)
//                    , Double.valueOf(mLocationTrackingList.get(i).Longitude)));
//            int j = i + 1;
//            if (j <= mLocationTrackingList.size() - 1) {
////                callDownLoadTask(new LatLng(Double.valueOf(mLocationTrackingList.get(i).Latitude)
////                                , Double.valueOf(mLocationTrackingList.get(i).Longitude)),
////                        new LatLng(Double.valueOf(mLocationTrackingList.get(j).Latitude)
////                                , Double.valueOf(mLocationTrackingList.get(j).Longitude)));
//
//                points.add(new LatLng(Double.valueOf(mLocationTrackingList.get(i).Latitude)
//                        , Double.valueOf(mLocationTrackingList.get(i).Longitude)));
//                points.add(new LatLng(Double.valueOf(mLocationTrackingList.get(j).Latitude)
//                        , Double.valueOf(mLocationTrackingList.get(j).Longitude)));
//                            }*/
            if (mLocationTrackingList.get(i).IsViolation.equals("0")) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(mLocationTrackingList.get(i).Latitude)
                        , Double.valueOf(mLocationTrackingList.get(i).Longitude)))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_marker)));
            }
//                // Adding all the points in the route to LineOptions
//            }
            if (i == mLocationTrackingList.size() - 1) {
                mProgressBar.setVisibility(View.GONE);
            }

        }
 /*====================To Draw Polyline==================*/
 /*       //To draw end log tracking to end LatLng
//        callDownLoadTask(new LatLng(Double.valueOf(mLocationTrackingList.get(mLocationTrackingList.size() - 1).Latitude)
//                        , Double.valueOf(mLocationTrackingList.get(mLocationTrackingList.size() - 1).Longitude))
//                , endLatLng);
//        points.add(new LatLng(Double.valueOf(mLocationTrackingList.get(mLocationTrackingList.size() - 1).Latitude)
//                , Double.valueOf(mLocationTrackingList.get(mLocationTrackingList.size() - 1).Longitude)));
//        points.add(endLatLng);
//        mMap.addMarker(new MarkerOptions().position(endLatLng)
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_marker)));
        // Adding all the points in the route to LineOptions
//        lineOptions.addAll(points);
//        mMap.addPolyline(lineOptions);*/

        mMap.addMarker(new MarkerOptions().position(endLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.trip_end_map_icon)));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(startLatLng);
        builder.include(endLatLng);
        LatLngBounds bounds = builder.build();
        int padding = ((mapFragment.getView().getWidth() * 10) / 100); // offset from edges of the map
        // in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                padding);
        mMap.animateCamera(cu);
        getViolationDetails(menteeTripList.TripId);

    }

    AsyncTask<Void, Void, List<SnappedPoint>> mTaskSnapToRoads =
            new AsyncTask<Void, Void, List<SnappedPoint>>() {
                @Override
                protected void onPreExecute() {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setIndeterminate(true);
                }

                @Override
                protected List<SnappedPoint> doInBackground(Void... params) {
                    try {
                        return snapToRoads(mContext);
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onPostExecute(List<SnappedPoint> snappedPoints) {
                    mSnappedPoints = snappedPoints;
                    mProgressBar.setVisibility(View.INVISIBLE);
                    com.google.android.gms.maps.model.LatLng[] mapPoints =
                            new com.google.android.gms.maps.model.LatLng[mSnappedPoints.size()];
                    int i = 0;
                    LatLngBounds.Builder bounds = new LatLngBounds.Builder();
                    for (SnappedPoint point : mSnappedPoints) {
                        mapPoints[i] = new com.google.android.gms.maps.model.LatLng(point.location.lat,
                                point.location.lng);
                        bounds.include(mapPoints[i]);
                        i += 1;
                    }

                    mMap.addPolyline(new PolylineOptions().add(mapPoints).color(Color.BLUE));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 0));
                }
            };

    private List<SnappedPoint> snapToRoads(GeoApiContext context) throws Exception {
        List<SnappedPoint> snappedPoints = new ArrayList<>();

        int offset = 0;
        while (offset < mCapturedLocations.size()) {
            // Calculate which points to include in this request. We can't exceed the APIs
            // maximum and we want to ensure some overlap so the API can infer a good location for
            // the first few points in each request.
            if (offset > 0) {
                offset -= PAGINATION_OVERLAP;   // Rewind to include some previous points
            }
            int lowerBound = offset;
            int upperBound = Math.min(offset + PAGE_SIZE_LIMIT, /*mCapturedLocations.size()*/mLocationTrackingList.size());
            // Grab the data we need for this page.
            com.google.maps.model.LatLng[] page = mCapturedLocations
                    .subList(lowerBound, upperBound)
                    .toArray(new com.google.maps.model.LatLng[upperBound - lowerBound]);
            // Perform the request. Because we have interpolate=true, we will get extra data points
            // between our originally requested path. To ensure we can concatenate these points, we
            // only start adding once we've hit the first new point (i.e. skip the overlap).
            SnappedPoint[] points = RoadsApi.snapToRoads(context, true, page).await();
            boolean passedOverlap = false;
            for (SnappedPoint point : points) {
                if (offset == 0 || point.originalIndex >= PAGINATION_OVERLAP) {
                    passedOverlap = true;
                }
                if (passedOverlap) {
                    snappedPoints.add(point);
                }
            }
            offset = upperBound;
        }

        return snappedPoints;
    }


    private float distanceBetween(LatLng latLng1, LatLng latLng2) {

        Location loc1 = new Location(LocationManager.GPS_PROVIDER);
        Location loc2 = new Location(LocationManager.GPS_PROVIDER);

        loc1.setLatitude(latLng1.latitude);
        loc1.setLongitude(latLng1.longitude);

        loc2.setLatitude(latLng2.latitude);
        loc2.setLongitude(latLng2.longitude);


        return loc1.distanceTo(loc2);
    }

    private void callDownLoadTask(/*LatLng startLatLng, LatLng endLatLng*/String path, boolean forVoilation, TripViolationDetailParser.TripViolationDetail tripViolationDetail) {
//        String url = getDirectionsUrl(startLatLng, endLatLng);
        String url = getSnapRoadUrl(path);
        DravaLog.print("snapRoad==>Url=>" + url);
        if (DeviceUtils.isInternetConnected(this)) {
            DownloadTask downloadTask = new DownloadTask(forVoilation, tripViolationDetail);
            // Start downloading json data from Google Directions API
            downloadTask.execute(url);
        } else {
            AlertUtils.showAlert(this, getResources().getString(R.string.check_your_internet_connection));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        new MapStateListener(mMap, mapFragment, this) {

            @Override
            public void onMapTouched() {
                Log.d("MapsActivity", "onMapTouched");
//                isMapTouched = true;
            }

            @Override
            public void onMapReleased() {
                Log.d("MapsActivity", "onMapReleased");
//                isMapTouched = false;
            }

            @Override
            public void onMapUnsettled() {
//                Log.d("MapsActivity", "onMapUnsettled");
            }

            @Override
            public void onMapSettled() {
//                Log.d("MapsActivity", "onMapSettled");
            }
        };
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        /*CameraPosition cameraPosition = new CameraPosition.Builder()
                .zoom(15)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/
//        mMap.moveCamera(new CameraUpdateFactory.newLatLng(startLatLng), 3000, 3500, 5);
        if (startLatLng != null && endLatLng != null) {
            callKlmCoordinates();             //Highlight the Malaysia Co-ordinates
            getTripTrackPathDetails();
            setUpEvents();
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&travelMode=DRIVING&key=AIzaSyAIU7__6lQoFjBvmePf2tAom5hneyIXkVY";

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        return url;
    }

    private String getSnapRoadUrl(String path) {

        // Origin of route
        String strPath = "path=" + path;

        // Destination of route

        String interpolate = "interpolate=" + false;//if true Generate more value


        // Sensor enabled
        String key = "key=" + getString(R.string.road_api_key);

        // Building the parameters to the web service
        String parameters = strPath + "&" + interpolate + "&" + key;
        // Building the url to the web service
        String url = "https://roads.googleapis.com/v1/snapToRoads?" + parameters;
        return url;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        View infoView = LayoutInflater.from(this).inflate(R.layout.view_map_info_view, null);
        TextView txtSpeedLimit = (TextView) infoView.findViewById(R.id.txt_speed_limit);
        TripViolationDetailParser.TripViolationDetail tripViolationDetail = voilatedMarkerHashMap.get(marker);
        if (tripViolationDetail != null) {
            txtSpeedLimit.setText("Speeds at " + tripViolationDetail.getVechileSpeed() + " km(Speed Limit " + tripViolationDetail.getRoadSpeed() + " km/h)");
            return infoView;
        } else {
            return null;
        }
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {
        boolean forVoilation;
        TripViolationDetailParser.TripViolationDetail tripViolationDetail;

        // Downloading data in non-ui thread
        public DownloadTask(Boolean forTracking, TripViolationDetailParser.TripViolationDetail tripViolationDetail) {
            this.forVoilation = forTracking;
            this.tripViolationDetail = tripViolationDetail;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setIndeterminate(true);
        }

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                DravaLog.print("snapRoad=Response=>" + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
           /* //For direction Api
//            ParserTask parserTask = new ParserTask(forVoilation);
//            parserTask.execute(result);*/
            /*For Snap Road*/
            SnapRoadParser snapRoadParser = new Gson().fromJson(result, SnapRoadParser.class);
            if (snapRoadParser != null && snapRoadParser.snappedPoints != null) {
                mProgressBar.setVisibility(View.INVISIBLE);
                if (!forVoilation) {
                    com.google.android.gms.maps.model.LatLng[] mapPoints =
                            new com.google.android.gms.maps.model.LatLng[snapRoadParser.snappedPoints.size()];
                    int i = 0;
                    for (SnapRoadParser.ParserSnappedPoint point : snapRoadParser.snappedPoints) {
                        mapPoints[i] = new com.google.android.gms.maps.model.LatLng(point.location.latitude,
                                point.location.longitude);
                        i += 1;
//                    mMap.addCircle(new CircleOptions().center(new com.google.android.gms.maps.model.LatLng(point.location.latitude,
//                            point.location.longitude)).radius(5).fillColor(Color.BLUE).strokeColor(Color.WHITE).strokeWidth(2));
                        mMap.addMarker(new MarkerOptions().position(new com.google.android.gms.maps.model.LatLng(point.location.latitude,
                                point.location.longitude))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.dot_marker)));
                    }
//                mMap.addPolyline(new PolylineOptions().add(mapPoints).color(Color.BLUE));
                } else {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(snapRoadParser.snappedPoints.get(0).location.latitude
                            , snapRoadParser.snappedPoints.get(0).location.longitude))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.voilate_point)));
                    if (tripViolationDetail != null) {
                        voilatedMarkerHashMap.put(marker, tripViolationDetail);
                    }
                    mMap.setInfoWindowAdapter(MapViewActivity.this);
                }
            } else {
                DravaLog.print("SnappedPoints Failed");
                Toast.makeText(MapViewActivity.this, "SnappedPoints Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setReadTimeout(60000);
            urlConnection.setConnectTimeout(60000);
            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
//            Log.d("Exception", e.toString());
            e.printStackTrace();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        boolean forTracking;

        public ParserTask(boolean forTracking) {
            this.forTracking = forTracking;
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
//            CircleOptions circleOptions = new CircleOptions();
            MarkerOptions markerOptions = new MarkerOptions();
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);
            }
            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (!forTracking) {
                    DravaLog.print("For=>PolyLine");
                    DravaLog.print("polyLine==>Size==>" + polylineList.size());
//                    if (polylineList.size() >= 1) {
//                        polylineList.get(0).remove();
//                        polylineList.clear();
//                    }
                    Polyline polyline = mMap.addPolyline(lineOptions);
                    polylineList.add(polyline);
                } else {
                    DravaLog.print("For=>Movement");
//                    trackingPoints.clear();
//                    trackingPoints = points;
//                    DravaLog.print("TrackingPoints=>size=>"+trackingPoints.size());
                }
            } else {
                Toast.makeText(MapViewActivity.this, "Location Not Available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getViolationDetails(String tripId) {

        if (!TextUtils.isEmpty(tripId)) {
            getApp().getRetrofitInterface().getTripViolationDetails(tripId).enqueue(new RetrofitCallback<ResponseBody>() {
                @Override
                public void onSuccessCallback(Call<ResponseBody> call, String content) {
                    super.onSuccessCallback(call, content);
                    TripViolationDetailParser mViolationDetailParser = new Gson().fromJson(content, TripViolationDetailParser.class);
                    if (mViolationDetailParser != null && mViolationDetailParser.meta.getCode() == 200 && mViolationDetailParser.tripViolationDetail.size() > 0) {
                        rlVoilateLabel.setVisibility(View.VISIBLE);
                        tripViolationDetailList = mViolationDetailParser.tripViolationDetail;
                        for (TripViolationDetailParser.TripViolationDetail tripViolationDetail : mViolationDetailParser.tripViolationDetail) {
                            String path = tripViolationDetail.getLatitude() + "," + tripViolationDetail.getLongitude();
                            /*Snap To Road*/
//                            callDownLoadTask(path, true, tripViolationDetail);
                            /*Local Tracking*/
                            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(tripViolationDetail.getLatitude())
                                    , Double.valueOf(tripViolationDetail.getLongitude())))
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.voilate_point)));
                            voilatedMarkerHashMap.put(marker, tripViolationDetail);
                            mMap.setInfoWindowAdapter(MapViewActivity.this);
                        }
                    } else {
//                        txtViolationTitle.setVisibility(View.GONE);
                        Toast.makeText(MapViewActivity.this, "No Voilation found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                    super.onFailureCallback(call, t, message, code);
                    AppLog.print(MapViewActivity.this, "getViolationDetails==>message==>" + message);
//                    txtViolationTitle.setVisibility(View.GONE);
                }
            });
        }
    }

    private void callKlmCoordinates() {
        try {
            retrieveFileFromResource();
        } catch (Exception e) {
            Log.e("Exception caught", e.toString());
        }
    }

    private void retrieveFileFromResource() {
        try {
//            byte[] byteWorldCoords = readFromFile(R.raw.kml_complete_world_coords);
//            KmlLayer kmlLayer = new KmlLayer(mMap, new ByteArrayInputStream(byteWorldCoords), getApplicationContext());
//            kmlLayer.addLayerToMap();
            byte[] byteMalasiyaCoords = readFromFile(R.raw.kml_malasiya_coords);
            KmlLayer kmlMalaysia = new KmlLayer(mMap, new ByteArrayInputStream(byteMalasiyaCoords), getApplicationContext());
            moveCameraToKml(kmlMalaysia);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private byte[] readFromFile(int resourceFile) {
        try {
            Resources r = getResources();
            InputStream is = r.openRawResource(resourceFile);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void moveCameraToKml(KmlLayer kmlLayer) {
        KmlContainer container = kmlLayer.getContainers().iterator().next();
        container = container.getContainers().iterator().next();
        KmlPlacemark placemark = container.getPlacemarks().iterator().next();
        KmlPolygon polygon = (KmlPolygon) placemark.getGeometry();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : polygon.getOuterBoundaryCoordinates()) {
            builder.include(latLng);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 3000, 3500, 5));
    }


    private void configureFacebook() {
        if (DeviceUtils.isFacebookInstalled(this)) {
            FacebookSdk.sdkInitialize(getApplicationContext());
            callbackManager = CallbackManager.Factory.create();
            sharePhotoToFacebook();
        } else {
            AlertUtils.showAlert(this, getString(R.string.facebook_not_installed));
        }
    }


    private void sharePhotoToFacebook() {
        if (tripDetailParser != null) {
            final ShareDialog shareDialog = new ShareDialog(this);
            mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    Bitmap fullScreenBitmap = getScreenShot();
                    Bitmap combineBitmap = combineImages(bitmap, fullScreenBitmap);
                    SharePhoto photo = new SharePhoto.Builder()
                            .setBitmap(combineBitmap)
                            .build();

                    SharePhotoContent photoContent = new SharePhotoContent.Builder()
                            .addPhoto(photo)
                            .build();


                    shareDialog.show(photoContent, ShareDialog.Mode.AUTOMATIC);
                }
            });
        }
    }

    public Bitmap getScreenShot() {
        txtShareOnFacebook.setVisibility(View.GONE);
        View screenView = rlRootView.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        txtShareOnFacebook.setVisibility(View.VISIBLE);
        return bitmap;
    }

    private Bitmap combineImages(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("sharing...");
        progressDialog.show();
        Bitmap cs = null;
        int width, height = 0;
//        if (c.getWidth() > s.getWidth()) {
        if (c.getHeight() > s.getHeight()) {
            width = c.getWidth() /*+ s.getWidth()*/;
            height = c.getHeight();
        } else {
            width = s.getWidth() /*+ s.getWidth()*/;
//            height = c.getHeight();
            height = s.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

//        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s,/* c.getWidth()*/0f, 0f, null);


        int top = txtTripCreatedDate.getHeight() * 3 / 2 + toolbar.getHeight();

        int mergeHeight = (int) (txtTripCreatedDate.getHeight()/* * 2 / 1.5 */ + toolbar.getHeight() + mapFragment.getView().getHeight());
        DravaLog.print("Merge==>top===>" + top);
        DravaLog.print("Merge==>Height===>" + mergeHeight);
        comboImage.drawBitmap(c, null, new Rect(0, top, c.getWidth(), mergeHeight), null);
        // save all clip
        comboImage.save(Canvas.ALL_SAVE_FLAG);

        // store
        comboImage.restore();
        // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location
//        String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png";
//
//        OutputStream os = null;
//        try {
//            os = new FileOutputStream(this.getExternalCacheDir() + tmpImg);
//            cs.compress(Bitmap.CompressFormat.PNG, 100, os);
//        } catch (IOException e) {
//            Log.e("combineImages", "problem combining images", e);
//        }
        progressDialog.hide();
        return cs;
    }

}
