package com.drava.android.activity.map;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.activity.HomeActivity;
import com.drava.android.activity.map.services.UpdateTripStateFragment;
import com.drava.android.activity.mentor_mentee.MenteeListAdapter;
import com.drava.android.activity.mentor_mentee.ViewTripsActivity;
import com.drava.android.model.EndTrip;
import com.drava.android.activity.map.maputils.MapStateListener;
import com.drava.android.activity.map.maputils.TouchableMapFragment;
import com.drava.android.base.AppConstants;
import com.drava.android.base.BaseFragment;
import com.drava.android.model.MenteeClusterItem;
import com.drava.android.parser.MenteeListParser;
import com.drava.android.parser.UserInformationParser;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.ui.SpeedometerView;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.BitmapUtils;
import com.drava.android.utils.CurrentLocationClient;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.GpsUtils;
import com.drava.android.utils.TextUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlPlacemark;
import com.google.maps.android.kml.KmlPolygon;
import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class HomeFragment extends BaseFragment implements OnMapReadyCallback, AppConstants,
        ClusterManager.OnClusterClickListener<MenteeClusterItem>,
        ClusterManager.OnClusterInfoWindowClickListener<MenteeClusterItem>,
        ClusterManager.OnClusterItemClickListener<MenteeClusterItem>,
        ClusterManager.OnClusterItemInfoWindowClickListener<MenteeClusterItem>, UpdateTripStateFragment {

    public static final String KEY_POINTS = "points";
    private GoogleMap mMap;
    private LatLng mCurrentLatLng, tripStartLatLng;
    private static final float ZOOM_LEVEL = 15.0f;
    private boolean isMapTouched = false;
    private KmlLayer layer;

    private ArrayList<LatLng> violatePoints;
    private PolylineOptions whitePolyLine;
    private TouchableMapFragment mapFragment;

    private TextView txtMaxSpeed, txtCurrentSpeed, txtSpeed;
    private RelativeLayout speedometerLayout;
    private SpeedometerView speedometerView;
    private ImageButton btnManualStart;
    public static boolean isWebServiceRunnig = false;
    public double maxSpeed = 0.0;
    private double currentLatitude, currentLongitude;
    private boolean isTripEndCountDownTimerStarted = false;
    private boolean showAlertDialog = true;
    private int tripType = 2;
    private double tripMinSpeed = 0, tripMaxSpeed = 0;
    private EndTrip endTrip;
    private CurrentLocationClient currentLocationClient;
    private Date startDate;
    private Location mLastLocation;

    private ClusterManager<MenteeClusterItem> mClusterManager;
    private LinearLayout layoutBottomSheetArrow;
    private MenteeListAdapter menteeListAdapter;
    private ArrayList<MenteeListParser.MentorsMenteeList> menteeList;
    private RecyclerView recyclerView;
    private TextView txtEmptyView;
    private ProgressDialog progressDialog;
    private ProgressBar progressBar;
    private boolean isLoading = false;
    private int totalCount = 0, start = 0;
    private int visibleThreshold = 1;
    private MarkerAdapterForMentee markerAdapterForMentee;
    private RelativeLayout menteeListLayout;
    private ImageView imgArrow;
    private boolean isBottomSheetUp = false;
    private LatLng centerScreen;

    ScheduledExecutorService trackLatlanPathExecutor = Executors.newScheduledThreadPool(1);
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    private BottomSheetBehavior mBottomSheetBehavior;
    private View bottomSheet;
    private RelativeLayout mRootView;

    private HashMap<MyMapPoint, Marker> mPoints = new HashMap<MyMapPoint, Marker>();


    @Override
    public void setButtonState(boolean state) {              // This method is called from TripStates.java ----> HomeActivity.java ------> HomeFragment.java
        if (state) {
            btnManualStart.setImageResource(R.drawable.stop);
        } else {
            btnManualStart.setImageResource(R.drawable.start);
        }
    }

    @Override
    public void updateView() {                              // This method is called from TripStates.java ----> HomeActivity.java ------> HomeFragment.java
        if (getActivity() != null && isAdded()) {
            if (getApp() != null) {
                if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
                    drawLineOnMap();
                }
            }
        }
    }

    @Override
    public void updateViolatedPoint() {                     // This method is called from TripStates.java ----> HomeActivity.java ------> HomeFragment.java
//        getHome().mPoints.add(new MyMapPoint((getHome().mPoints.size() + 1), getHome().tripStates.mCurrentLatLng, true));
    }

    @Override
    public void clearMap() {                                // This method is called from TripStates.java ----> HomeActivity.java ------> HomeFragment.java
        mPoints.clear();
        if(mMap != null) {
            mMap.clear();
        }
    }

    public static HomeFragment newInstance(ArrayList<MyMapPoint> points) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(KEY_POINTS, points);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DravaLog.print("HomeFragment=>onCreateView");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        init(view);
        setupDefaults();
        return view;
    }


    private void init(View view) {
        getHome().setUpdateTripStateFragment(this);
        mapFragment = (TouchableMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);

        txtMaxSpeed = (TextView) view.findViewById(R.id.txt_max_speed);
        txtCurrentSpeed = (TextView) view.findViewById(R.id.txt_current_speed);
        speedometerView = (SpeedometerView) view.findViewById(R.id.speedometer);
        txtSpeed = (TextView) view.findViewById(R.id.txt_speed);
        btnManualStart = (ImageButton) view.findViewById(R.id.btn_manual_start);
        speedometerLayout = (RelativeLayout) view.findViewById(R.id.rlv_speedometer_layout);

        bottomSheet = view.findViewById(R.id.bottom_sheet);
        mRootView = (RelativeLayout) view.findViewById(R.id.root_view);
        layoutBottomSheetArrow = (LinearLayout) view.findViewById(R.id.bottom_sheet_arrow);

        recyclerView = (RecyclerView) view.findViewById(R.id.mentee_list);
        txtEmptyView = (TextView) view.findViewById(R.id.empty_view);
        menteeListLayout = (RelativeLayout) view.findViewById(R.id.mentee_list_layout);
        progressBar = (ProgressBar) view.findViewById(R.id.progresswheel);
        imgArrow = (ImageView) view.findViewById(R.id.img_arrow);

        menteeList = new ArrayList<>();
        menteeListAdapter = new MenteeListAdapter(getActivity(), menteeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(menteeListAdapter);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.loading));

        violatePoints = new ArrayList<>();
        endTrip = new EndTrip();

        whitePolyLine = new PolylineOptions();
        whitePolyLine.width(7);
        whitePolyLine.color(Color.BLUE);


        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {

            btnManualStart.setVisibility(View.VISIBLE);
            speedometerLayout.setVisibility(View.VISIBLE);
            bottomSheet.setVisibility(View.GONE);
            if (getHome().tripStates.getButtonState()) {
                btnManualStart.setImageResource(R.drawable.stop);
            } else {
                btnManualStart.setImageResource(R.drawable.start);
            }
        } else {
            btnManualStart.setVisibility(View.GONE);
            speedometerLayout.setVisibility(View.GONE);
            bottomSheet.setVisibility(View.VISIBLE);
        }

//        btnManualStart.setImageResource(R.drawable.start);
        btnManualStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!GpsUtils.isGpsEnabled(getActivity())) {
                    showGPSAlert();         // manual button start
                } else {

                    if (!getHome().tripStates.isTripAutoStarted && !getHome().tripStates.isTripManualStarted) {
                        getHome().tripStates.tripType = 1;
                        getHome().tripStates.isTripManualStarted = true;
                        getHome().tripStates.setButtonState(true);      //call TripStates.java setButton()
                        btnManualStart.setImageResource(R.drawable.stop);

                        if (!getHome().tripStates.isTripAutoStarted && getHome().tripStates.currentSpeed < TRACKING_START_SPEED && !getHome().tripStates.isTripEndCountDownTimerStarted) {
                            getHome().tripStates.isTripEndCountDownTimerStarted = true;
                            getHome().tripStates.createTimer();  //10000 - 10 seconds, 120000 - 120 seconds
                        }

                    } else {
                        getHome().tripStates.tripType = 2;
                        getHome().tripStates.setButtonState(false);      //call TripStates.java setButton()
                        btnManualStart.setImageResource(R.drawable.start);
                        if (getHome().tripStates.stopTrackTimer != null) {
                            getHome().tripStates.stopTrackTimer.cancel();
                        }
                        DravaLog.print("---------------------End from Manual button-----------------------");
                        if (getHome().tripStates.isTripAutoStarted) {
                            getHome().tripStates.callEndTripService();
                        } else {
                            setButtonState(false);
                            getHome().tripStates.isTripManualStarted = false;
                            getHome().tripStates.isTripEndCountDownTimerStarted = false;
                        }
                        getHome().mPoints.clear();
                    }
                }
            }
        });

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        layoutBottomSheetArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    imgArrow.setImageResource(R.drawable.locaiton_down_arrow);
                } else {
                    BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    imgArrow.setImageResource(R.drawable.location_up_arrow);
                }
            }
        });

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            float previousOffset = 0;

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior.setPeekHeight(50);
                    isBottomSheetUp = false;
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    isBottomSheetUp = true;
                } else if (newState == BottomSheetBehavior.STATE_SETTLING) {
                    if (isBottomSheetUp) {
                        mapZoomIn();
                    } else {
                        mapZoomOut();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (previousOffset < slideOffset) {
                    imgArrow.setImageResource(R.drawable.locaiton_down_arrow);
                    isBottomSheetUp = false;
                } else {
                    imgArrow.setImageResource(R.drawable.location_up_arrow);
                    isBottomSheetUp = true;
                }
                previousOffset = slideOffset;
            }
        });

        mBottomSheetBehavior.setPeekHeight(50);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


    }

    private void setupDefaults() {

      /*  Bundle extras = getArguments();
        if ((extras != null) && extras.containsKey(KEY_POINTS)) {
            for (Parcelable pointP : extras.getParcelableArrayList(KEY_POINTS)) {
                mPoints.put((MyMapPoint) pointP, null);
            }
        }*/

//        getHome().setUpdateTripStateFragment(this);
    }


    private void addMapPoints() {

        if (mMap != null) {
            mMap.clear();
            whitePolyLine = new PolylineOptions();
            whitePolyLine.width(7);
            whitePolyLine.color(Color.BLUE);
            whitePolyLine.geodesic(true);
            mPoints.clear();
            for (MyMapPoint mapPoint : getHome().mPoints) {
//                mPoints.put(mapPoint, null);
                whitePolyLine.add(mapPoint.latLng);
                if (mapPoint.isViolated) {
                    mMap.addMarker(mapPoint.getMarkerOptions());
                }
            }
            /*for (Map.Entry<MyMapPoint, Marker> entry : mPoints.entrySet()) {

                MyMapPoint point = entry.getKey();
                if (point.isViolated) {
                    mMap.addMarker(point.getMarkerOptions());
                }
                whitePolyLine.add(point.latLng);
            }
            whitePolyLine.add(getHome().mPoints);*/
            mMap.addPolyline(whitePolyLine);
        }
    }

    private void drawLineOnMap() {

        txtSpeed.setText((int) getHome().tripStates.currentSpeed + "");
        txtMaxSpeed.setText(String.format(getString(R.string.max_speed), String.valueOf((int) getHome().tripStates.maxSpeed + "")));
        speedometerView.setSpeed(getHome().tripStates.currentSpeed);
        animateToCurrentLocation();
        //Code Moved to trip state
//=================
        /*if ((!getHome().tripStates.isTripAutoStarted && getHome().tripStates.currentSpeed >= TRACKING_START_SPEED && !getHome().tripStates.isTripManualStarted) ||
                (!getHome().tripStates.isTripAutoStarted && getHome().tripStates.currentSpeed >= TRACKING_START_SPEED && getHome().tripStates.isTripManualStarted)) {  // && currentSpeed >= TRACKING_START_SPEED
            // call Trip create webservice
            getHome().tripStates.tripMinSpeed = 0;
            getHome().tripStates.tripMaxSpeed = 0;
            getHome().mPoints.clear();
            mPoints.clear();
            mMap.clear();

            getHome().tripStates.startDate = Calendar.getInstance().getTime();
            getHome().tripStates.tripStartLatLng = new LatLng(getHome().tripStates.currentLatitude, getHome().tripStates.currentLongitude);
            getHome().tripStates.isTripAutoStarted = true;
            getHome().tripStates.isTripManualStarted = true;
            btnManualStart.setImageResource(R.drawable.stop);
            getHome().tripStates.setButtonState(true);

            final Toast trip_toast = Toast.makeText(getActivity(), "Trip going to start", Toast.LENGTH_SHORT);
            trip_toast.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    trip_toast.cancel();
                }
            }, 1000);


            getHome().tripStates.startTrip(String.valueOf(getHome().tripStates.mCurrentLatLng.latitude), String.valueOf(getHome().tripStates.mCurrentLatLng.longitude),
                    DateConversion.getCurrentDateAndTime(), "" + tripType, DateConversion.getCurrentDate());


        } else if (getHome().tripStates.isTripAutoStarted && getHome().tripStates.currentSpeed < TRACKING_START_SPEED && !getHome().tripStates.isTripEndCountDownTimerStarted) {
            getHome().tripStates.isTripEndCountDownTimerStarted = true;
            getHome().tripStates.createTimer();  //10000 - 10 seconds, 120000 - 120 seconds
        }*/
//=================
        addMapPoints();

       /* updateLatLng.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                getHome().mPoints.add(new MyMapPoint((getHome().mPoints.size() + 1), getHome().tripStates.mCurrentLatLng, false));
            }
        }, 0, 5, TimeUnit.SECONDS);*/
//        getHome().mPoints.add(new MyMapPoint((getHome().mPoints.size() + 1), getHome().tripStates.mCurrentLatLng, false));
//        whitePolyLine.add(getHome().tripStates.mCurrentLatLng);

       /* if (mMap != null) {
            mMap.addPolyline(whitePolyLine);
        }*/
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        DravaLog.print("onActivityResult");
//        if (requestCode == GPS_LOCATION) {
        if (isAdded() && getActivity() != null && getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
            if (!GpsUtils.isGpsEnabled(getActivity())) {
                showGPSAlert();         //on Activity result
            } else {
                btnManualStart.setVisibility(View.VISIBLE);
            }

        } else {
            btnManualStart.setVisibility(View.GONE);
        }
//        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initMapService();
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
            addMapPoints();
            callKlmCoordinates();       // highlighting the malasiya
        }
        new MapStateListener(mMap, mapFragment, getActivity()) {

            @Override
            public void onMapTouched() {
                Log.d("MapsActivity", "onMapTouched");
                isMapTouched = true;
            }

            @Override
            public void onMapReleased() {
                Log.d("MapsActivity", "onMapReleased");
                isMapTouched = false;
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
        if (getApp().getUserPreference().getMentorOrMentee().equalsIgnoreCase(MENTOR)) {
            initMapforMentor();
            loadMenteesCurrentPositionListforMentor();
            if(!TextUtils.isNullOrEmpty(getApp().getUserPreference().getCurrentLat())){         //R.L v1.1
                currentLatitude = Double.parseDouble(getApp().getUserPreference().getCurrentLat());
            }
            if(!TextUtils.isNullOrEmpty(getApp().getUserPreference().getCurrentLong())){
                currentLongitude = Double.parseDouble(getApp().getUserPreference().getCurrentLong());
            }
            if(currentLatitude>0 && currentLongitude>0){
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLatitude, currentLongitude), ZOOM_LEVEL));
            }else{
                callKlmCoordinates();       // highlighting the malasiya
            }
        }
    }

    private void initMapService() {
        if (mMap != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
            } else {
                if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 5);
                } else {
                    mMap.setMyLocationEnabled(true);
                }
            }

            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);

            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if (!GpsUtils.isGpsEnabled(getActivity())) {
                        showGPSAlert();         //btn myLocation
                    } else {
                        isMapTouched = false;
                        animateToCurrentLocation();
                    }
                    return false;
                }
            });

        }
//        getHome().tripStates.updateTripPath();        R.L
    }

    private void animateToCurrentLocation() {
        if (!isMapTouched) {
            if (getHome().tripStates !=null && getHome().tripStates.mCurrentLatLng != null && mMap != null) {
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(getHome().tripStates.mCurrentLatLng, ZOOM_LEVEL);
                mMap.animateCamera(update);
            }
        } else {
            isMapTouched = false;
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
//            KmlLayer kmlLayer = new KmlLayer(mMap, new ByteArrayInputStream(byteWorldCoords), getActivity().getApplicationContext());
//            kmlLayer.addLayerToMap();
            byte[] byteMalasiyaCoords = readFromFile(R.raw.kml_malasiya_coords);
            KmlLayer kmlMalaysia = new KmlLayer(mMap, new ByteArrayInputStream(byteMalasiyaCoords), getActivity().getApplicationContext());
            moveCameraToKml(kmlMalaysia);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private byte[] readFromFile(int resourceFile) {
        try {
            Resources r = getActivity().getResources();
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 5:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Location Permission Granted", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                }
        }
    }

    @Override
//    @TargetApi(Build.VERSION_CODES.M)
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onStart() {
        super.onStart();
        DravaLog.print("HomeFragment=>onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        DravaLog.print("HomeFragment=>onStop");
    }

    private void mapZoomOut() {
        recyclerView.setEnabled(true);
        centerScreen = mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();    // Sets the center of the map
        if(mMap != null) {  //R.L V1.1
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(centerScreen)
                    .zoom(mMap.getCameraPosition().zoom - 1.5f)                   // Sets the zoom out
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void mapZoomIn() {
        if(mMap != null) {  //R.L v1.1
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(centerScreen)      // Sets the center of the map
                    .zoom(mMap.getCameraPosition().zoom + 1.5f)                   // Sets the zoom in
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void initMapforMentor() {
        mClusterManager = new ClusterManager<>(getActivity(), mMap);
//        mMap.setOnCameraIdleListener(mClusterManager);                //R.L v1.1
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mClusterManager.setRenderer(new MenteeClusterRenderer());

        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);

        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        markerAdapterForMentee = new MarkerAdapterForMentee(getActivity());
//        mClusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(new MarkerAdapterForMentee(getActivity()));
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(markerAdapterForMentee);

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (!GpsUtils.isGpsEnabled(getActivity())) {
                    showGPSAlert();         //btn myLocation
                } else {
                    isMapTouched = false;
                    animateToCurrentLocation();
                }
                return false;
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                //Your logic to resize map
            }
        });
        mMap.setMinZoomPreference(5.0f);
    }

    private void loadMenteesCurrentPositionListforMentor() {
        if (DeviceUtils.isInternetConnected(getActivity())) {
            getMenteeList(true, false);
        } else {
            AlertUtils.showAlert(getActivity(), getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }, false);
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem, totalItemCount;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                totalItemCount = layoutManager.getItemCount();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                DravaLog.print("totalItemCount==>" + totalItemCount + "==>lastVisibleItem==>" + lastVisibleItem + "==>isLoading==>" + isLoading);
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    onLoadMore();
                    isLoading = true;
                }
            }
        });
    }


    public static Bitmap convertViewToBitmap(View view) {
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    private void onLoadMore() {
        if (DeviceUtils.isInternetConnected(getActivity())) {
            if (start < totalCount) {
                getMenteeList(false, true);         //onLoadMore
            }
        } else {
            AlertUtils.showAlert(getActivity(), getString(R.string.check_your_internet_connection, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }));
        }
    }


    private void menteeListAutoLoad() {

        if (DeviceUtils.isInternetConnected(getActivity())) {
            start = 0;
            getMenteeList(false, false);
        }
    }

    private void getMenteeList(boolean showProgress, final boolean showLoadMore) {
        if (showProgress) {
            progressDialog.show();
        }
        if (showLoadMore) {
            progressBar.setVisibility(View.VISIBLE);
        }

        getApp().getRetrofitInterface().getMenteesCurrentLocation(String.valueOf(start)).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);

                if ((progressDialog != null) && (progressDialog.isShowing()) && isAdded()) {
                    progressDialog.dismiss();
                }
                if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }

                Log.e("Response :", content);
                AppLog.print(getActivity(), "=====>Current Mentee location======> response============>Success");
                MenteeListParser menteeListParser = new Gson().fromJson(content, MenteeListParser.class);
                if (menteeListParser != null && menteeListParser.meta.code == 200) {
                    if (menteeListParser.MentorsMenteeList.size() > 0) {
                        isLoading = false;
                        menteeListLayout.setVisibility(View.VISIBLE);
                        txtEmptyView.setVisibility(View.GONE);
                        if (!showLoadMore) {
                            menteeList.clear();
                            mClusterManager.clearItems();
                        }
                        menteeList.addAll(menteeListParser.MentorsMenteeList);

                        totalCount = menteeListParser.meta.TotalCount;
                        start += menteeListParser.meta.ListedCount;
                        for (MenteeListParser.MentorsMenteeList object : menteeListParser.MentorsMenteeList) {
                            if (object != null) {
                                if (!TextUtils.isEmpty(object.CurrentLatitude) && !TextUtils.isEmpty(object.CurrentLongitude)) {
//                                    mClusterManager.addItem(new MenteeClusterItem(object.CurrentLatitude, object.CurrentLongitude, object.Photo, object.FirstName, object.LastName, object.CurrentLocation, object.MenteeId));        //R.L v1.1
                                }
                            }
                        }
                        menteeListAdapter.updateMenteeList(menteeList);
                    } else {
                        menteeListLayout.setVisibility(View.GONE);
                        txtEmptyView.setVisibility(View.VISIBLE);
                        txtEmptyView.setText(getString(R.string.no_mentees_found));
                    }
                }
                callUserInformationWebService();

                // have to fetch the user information again to get the mentor current position
                mClusterManager.addItem(new MenteeClusterItem(getApp().getUserPreference().getCurrentLat(),     //R.L v1.1
                        getApp().getUserPreference().getCurrentLong(),
                        getApp().getUserPreference().getPhoto(),
                        getApp().getUserPreference().getFirstName(),
                        getApp().getUserPreference().getLastName(),
                        getApp().getUserPreference().getCurrentLocation(),
                        getApp().getUserPreference().getUserId()));
                mClusterManager.cluster();
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);

                if ((progressDialog != null) && (progressDialog.isShowing()) && isAdded()) {
                    progressDialog.dismiss();
                }
                if (progressBar != null && progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                }

                Log.e("Failure :", "Get Mentee List");      //message
                AppLog.print(getActivity(), "=====>Current Mentee location======> response============>Failed");

            }
        });
    }

    public void callUserInformationWebService(){
        AppLog.print(getActivity(), "---------------Getting Mentor current location information---------------");
        DravaApplication.getApp().getRetrofitInterface().getUserInformation().enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                UserInformationParser userInformationParser = new Gson().fromJson(content, UserInformationParser.class);
                if(userInformationParser.getMeta().code == 200){
                    if (getActivity() != null && isAdded()) {
                        AppLog.print(getActivity(), "---------------success Getting Mentor current location information---------------");
                        getApp().getUserPreference().setCurrentLat(userInformationParser.getUserDetails().CurrentLatitude);
                        getApp().getUserPreference().setCurrentLong(userInformationParser.getUserDetails().CurrentLongitude);
                        getApp().getUserPreference().setCurrentLocation(userInformationParser.getUserDetails().CurrentLocation);
                    }
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                AppLog.print(getActivity(), "---------------failure Getting Mentor current location information---------------");
            }
        });
    }

    private class MenteeClusterRenderer extends DefaultClusterRenderer<MenteeClusterItem> {

        public MenteeClusterRenderer() {
            super(getActivity().getApplicationContext(), mMap, mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(MenteeClusterItem item, MarkerOptions markerOptions) {
            if (getActivity() != null) {
                View marker_view = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.location_marker_layout, null);
                ImageView userImage = (ImageView) marker_view.findViewById(R.id.marker_image_layout);
                if (!TextUtils.isEmpty(item.getImageurl())) {
                    Picasso.with(getActivity()).load(item.getImageurl()).placeholder(R.drawable.user).into(userImage);
                }
                Bitmap bitmap = convertViewToBitmap(marker_view);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            }
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MenteeClusterItem> cluster, MarkerOptions markerOptions) {
            String clusterSize;
            if (cluster.getSize() >= 100)
                clusterSize = "99+";
            else
                clusterSize = String.valueOf(cluster.getSize());

            View marker_view = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.location_marker_layout, null);
            Bitmap bitmap = convertViewToBitmap(marker_view);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            Bitmap icon = BitmapUtils.drawTextToBitmap(getActivity(), bitmap, clusterSize).getBitmap();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<MenteeClusterItem> cluster) {
            return cluster.getSize() > 1;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<MenteeClusterItem> cluster) {
        if (mMap.getCameraPosition().zoom == mMap.getMaxZoomLevel()) {

        } else {
            CollapseBottomSheetDown();
            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (MenteeClusterItem clusterItem : cluster.getItems()) {
                builder.include(clusterItem.getPosition());
            }

            try {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<MenteeClusterItem> cluster) {
    }

    @Override
    public boolean onClusterItemClick(MenteeClusterItem menteeClusterItem) {
        try {
            String fname = (TextUtils.isEmpty(menteeClusterItem.getFirstName()) ? "" : menteeClusterItem.getFirstName());
            String lname = (TextUtils.isEmpty(menteeClusterItem.getLastName()) ? "" : menteeClusterItem.getLastName());
            String location = (TextUtils.isEmpty(menteeClusterItem.getLocation()) ? "" : menteeClusterItem.getLocation());
            String menteeId = (TextUtils.isEmpty(menteeClusterItem.getMenteeId()) ? "" : menteeClusterItem.getMenteeId());
            if(getApp().getUserPreference().getMentorOrMentee().equals(MENTEE))         //R.L v1.2
                markerAdapterForMentee.updateMenteeInfo(fname + " " + lname, location, menteeId, true);
            else
                markerAdapterForMentee.updateMenteeInfo(fname + " " + lname, location, menteeId, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CollapseBottomSheetDown();
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(MenteeClusterItem menteeClusterItem) {
        if(getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {       //R.L v1.2
            Intent intent = new Intent(getActivity(), ViewTripsActivity.class);
            if (menteeClusterItem != null) {
                intent.putExtra(MENTEE_ID, menteeClusterItem.getMenteeId());
                intent.putExtra(PROFILE_PHOTO, menteeClusterItem.getImageurl());
            }
            getActivity().startActivity(intent);
        }
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

    private void showGPSAlert() {
        GpsUtils.showGpsAlert(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_LOCATION);
            }
        }, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if (!GpsUtils.isGpsEnabled(getActivity())) {
                    showGPSAlert();         //recursive showGPSAlert
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void CollapseBottomSheetDown() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_COLLAPSED);
            mapZoomIn();
        }
    }


    private HomeActivity getHome() {
        return (HomeActivity) getActivity();
    }


    public static class MyMapPoint implements Parcelable {
        private static final int CONTENTS_DESCR = 1;

        public int objectId;
        public LatLng latLng;
        public boolean isViolated;

        public MyMapPoint(int oId, LatLng point, boolean isViolated) {
            objectId = oId;
            latLng = point;
            this.isViolated = isViolated;
        }

        public MyMapPoint(Parcel in) {
            objectId = in.readInt();
            latLng = in.readParcelable(LatLng.class.getClassLoader());
        }

        public MarkerOptions getMarkerOptions() {
            return new MarkerOptions().position(latLng);
        }

        @Override
        public int describeContents() {
            return CONTENTS_DESCR;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(objectId);
            dest.writeParcelable(latLng, 0);
            dest.writeString("" + isViolated);
        }

        public static final Parcelable.Creator<MyMapPoint> CREATOR = new Parcelable.Creator<MyMapPoint>() {
            public MyMapPoint createFromParcel(Parcel in) {
                return new MyMapPoint(in);
            }

            public MyMapPoint[] newArray(int size) {
                return new MyMapPoint[size];
            }
        };
    }
}
