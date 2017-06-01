package com.drava.android.activity.mentor_mentee;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.drava.android.R;
import com.drava.android.activity.map.MarkerAdapterForMentee;
import com.drava.android.activity.map.maputils.MapStateListener;
import com.drava.android.activity.map.maputils.TouchableMapFragment;
import com.drava.android.base.BaseActivity;
import com.drava.android.base.Log;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.TextUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import okhttp3.ResponseBody;
import retrofit2.Call;

import static com.drava.android.activity.map.HomeFragment.convertViewToBitmap;

public class ViewOnMapActivity extends BaseActivity implements OnMapReadyCallback{

    protected Toolbar toolbar;
    protected MentorListParser.MentorList menteeDetails;
    protected String menteeLatitude, menteeLongitude, menteeLocaiton="";
    protected TouchableMapFragment mapFragment;
    protected GoogleMap mMap;
    protected LatLng currentLatLng;
    private static final float ZOOM_LEVEL = 12.0f;
    private MarkerAdapterForMentee markerAdapterForMentee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_on_map);
        init();
        setDefaults();
    }

    private void init(){
        setToolbar("Mentee Location");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        mapFragment = (TouchableMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);       //from MenteeMentorFragment
        menteeDetails = (MentorListParser.MentorList) getIntent().getExtras().getSerializable(MENTOR_LIST);
        assert menteeDetails != null;
        Log.e("Selected Mentee ", "Mentee Name :" + menteeDetails.FirstName +" "+menteeDetails.LastName);
        mapFragment.getMapAsync(this);
    }

    private void setDefaults(){
        if(getIntent().hasExtra(MENTEE_LOCATION)) {             //getting Mentee location information
            menteeLocaiton = getIntent().getStringExtra(MENTEE_LOCATION);
            menteeLatitude = getIntent().getStringExtra(MENTEE_LATTITUDE);
            menteeLongitude = getIntent().getStringExtra(MENTEE_LONGITUDE);
            Log.e("Selected Mentee ", "Mentee Location :" + menteeLocaiton);
            Log.e("Selected Mentee ", "Mentee Lattitude :" + menteeLatitude);
            Log.e("Selected Mentee ", "Mentee Longitude :" + menteeLongitude);
        }

        if(menteeDetails != null) {                             //getting Mentee personal information
            if(!TextUtils.isEmpty(menteeLatitude) && !TextUtils.isEmpty(menteeLongitude))
                currentLatLng = new LatLng(Double.parseDouble(menteeLatitude),Double.parseDouble(menteeLongitude));
            markerAdapterForMentee = new MarkerAdapterForMentee(ViewOnMapActivity.this);
        }

        if(TextUtils.isNullOrEmpty(menteeLocaiton) && TextUtils.isNullOrEmpty(menteeLatitude) && TextUtils.isNullOrEmpty(menteeLongitude)){
            Toast.makeText(ViewOnMapActivity.this, getString(R.string.str_no_location), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(currentLatLng != null) {
            View marker_view = ((LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.location_marker_layout, null);
            ImageView userImage = (ImageView) marker_view.findViewById(R.id.marker_image_layout);
            if (!TextUtils.isEmpty(menteeDetails.Photo)) {
                Picasso.with(this).load(menteeDetails.Photo).placeholder(R.drawable.user).into(userImage);
            }
            Bitmap bitmap = convertViewToBitmap(marker_view);
            mMap.addMarker(new MarkerOptions()
                    .position(currentLatLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            mMap.setInfoWindowAdapter(markerAdapterForMentee);
            markerAdapterForMentee.updateMenteeInfo(menteeDetails.FirstName+" "+menteeDetails.LastName, menteeLocaiton, menteeDetails.UserId, false);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, ZOOM_LEVEL));

        }

        new MapStateListener(mMap, mapFragment, ViewOnMapActivity.this){

            @Override
            public void onMapTouched() {

            }

            @Override
            public void onMapReleased() {

            }

            @Override
            public void onMapUnsettled() {

            }

            @Override
            public void onMapSettled() {

            }
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
