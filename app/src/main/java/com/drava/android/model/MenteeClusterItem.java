package com.drava.android.model;

import com.drava.android.utils.TextUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by evuser on 29-11-2016.
 */

public class MenteeClusterItem implements ClusterItem {

    private String latitude;
    private String longitude;
    private String imageurl;
    private String firstName;
    private String lastName;
    private String location;
    private LatLng latLng;
    private String menteeId;

    public MenteeClusterItem(String latitude, String longitude, String imageurl, String firstName,
                             String lastName, String location,String menteeId){
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageurl = imageurl;
        this.firstName = firstName;
        this.lastName = lastName;
        this.location = location;
        this.menteeId = menteeId;
        if(TextUtils.isNullOrEmpty(this.latitude)){     //R.L v1.1
            this.latitude = "4.2105";
        }
        if(TextUtils.isNullOrEmpty(this.longitude)) {
            this.longitude = "101.9758";
        }
        latLng = new LatLng(Double.parseDouble(this.latitude), Double.parseDouble(this.longitude));
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLocation() {
        return location;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getImageurl() {
        return imageurl;
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }

    public String getMenteeId() {
        return menteeId;
    }

    public void setMenteeId(String menteeId) {
        this.menteeId = menteeId;
    }
}
