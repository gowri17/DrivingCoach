package com.drava.android.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TripViolationDetailParser extends Meta implements Serializable {

    @SerializedName("meta")
    @Expose
    public Meta meta;
    @SerializedName("TripViolationDetail")
    @Expose
    public List<TripViolationDetail> tripViolationDetail = new ArrayList<TripViolationDetail>();
    @SerializedName("notifications")
    @Expose
    public List<String> notifications = new ArrayList<String>();

    public class TripViolationDetail extends Meta implements Serializable {

        @SerializedName("ViolationId")
        @Expose
        private String violationId;
        @SerializedName("MenteeId")
        @Expose
        private String menteeId;
        @SerializedName("TripId")
        @Expose
        private String tripId;
        @SerializedName("RoadSpeed")
        @Expose
        private String roadSpeed;
        @SerializedName("VechileSpeed")
        @Expose
        private String vechileSpeed;
        @SerializedName("Latitude")
        @Expose
        private String latitude;
        @SerializedName("Longitude")
        @Expose
        private String longitude;

        @SerializedName("Location")
        @Expose
        private String location;

        @SerializedName("DateCreated")
        @Expose
        private String dateCreated;

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getDateCreated() {
            return dateCreated;
        }

        public void setDateCreated(String dateCreated) {
            this.dateCreated = dateCreated;
        }

        /**
         * @return The violationId
         */
        public String getViolationId() {
            return violationId;
        }

        /**
         * @param violationId The ViolationId
         */
        public void setViolationId(String violationId) {
            this.violationId = violationId;
        }

        /**
         * @return The menteeId
         */
        public String getMenteeId() {
            return menteeId;
        }

        /**
         * @param menteeId The MenteeId
         */
        public void setMenteeId(String menteeId) {
            this.menteeId = menteeId;
        }

        /**
         * @return The tripId
         */
        public String getTripId() {
            return tripId;
        }

        /**
         * @param tripId The TripId
         */
        public void setTripId(String tripId) {
            this.tripId = tripId;
        }

        /**
         * @return The roadSpeed
         */
        public String getRoadSpeed() {
            return roadSpeed;
        }

        /**
         * @param roadSpeed The RoadSpeed
         */
        public void setRoadSpeed(String roadSpeed) {
            this.roadSpeed = roadSpeed;
        }

        /**
         * @return The vechileSpeed
         */
        public String getVechileSpeed() {
            return vechileSpeed;
        }

        /**
         * @param vechileSpeed The VechileSpeed
         */
        public void setVechileSpeed(String vechileSpeed) {
            this.vechileSpeed = vechileSpeed;
        }

        /**
         * @return The latitude
         */
        public String getLatitude() {
            return latitude;
        }

        /**
         * @param latitude The Latitude
         */
        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        /**
         * @return The longitude
         */
        public String getLongitude() {
            return longitude;
        }

        /**
         * @param longitude The Longitude
         */
        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

    }

}