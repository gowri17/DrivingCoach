package com.drava.android.activity.map;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.mentor_mentee.MentorListParser;
import com.drava.android.activity.mentor_mentee.ViewTripsActivity;
import com.drava.android.base.AppConstants;
import com.drava.android.model.MenteeClusterItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MarkerAdapterForMentee implements GoogleMap.InfoWindowAdapter,AppConstants{
    private Context context;
    View menteeContentView;
    private TextView txtUserName, txtUserAddress, txtViewTrips;
    private LinearLayout infoBaseLayout;

    public MarkerAdapterForMentee(Context context){
        this.context = context;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        menteeContentView = inflater.inflate(R.layout.mentee_marker_info_layout, null);
        txtUserAddress = (TextView)menteeContentView.findViewById(R.id.txt_user_address);
        txtUserName = (TextView)menteeContentView.findViewById(R.id.txt_user_name);
        txtViewTrips = (TextView)menteeContentView.findViewById(R.id.txt_view_trips);
        infoBaseLayout = (LinearLayout)menteeContentView.findViewById(R.id.info_base_layout);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return menteeContentView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public void updateMenteeInfo(String userName, String userAddress, final String menteeId, boolean isShowViewTrips) {
        txtUserName.setText(userName);
        txtUserAddress.setText(userAddress);
        if (isShowViewTrips) {
            txtViewTrips.setVisibility(View.VISIBLE);
        }else{
            txtViewTrips.setVisibility(View.GONE);
        }
    }
}
