package com.drava.android.activity.mentor_mentee.view_profile;

import com.drava.android.parser.Meta;

import java.io.Serializable;
import java.util.ArrayList;

class ProfileBeanClass implements Serializable {

    public Meta meta;
    public ArrayList<MenteeProfileBean> MenteeTripReport = new ArrayList<>();

    class MenteeProfileBean implements Serializable {

        public String Month;
        public String Year;
        public String MaxSpeed;
        public String TotalDistance;
        public String ViolationCount;
        public String Scores;
        public String TotalTravelHours;
        public String TotalTravelMinutes;
        public String MonthOfYear;

        public String getMonthOfYear() {
            return MonthOfYear;
        }

        public void setMonthOfYear(String monthOfYear) {
            MonthOfYear = monthOfYear;
        }


        public String getMonth() {
            return Month;
        }

        public void setMonth(String month) {
            Month = month;
        }

        public String getYear() {
            return Year;
        }

        public void setYear(String year) {
            Year = year;
        }

        public String getMaxSpeed() {
            return MaxSpeed;
        }

        public void setMaxSpeed(String maxSpeed) {
            MaxSpeed = maxSpeed;
        }

        public String getTotalDistance() {
            return TotalDistance;
        }

        public void setTotalDistance(String totalDistance) {
            TotalDistance = totalDistance;
        }

        public String getViolationCount() {
            return ViolationCount;
        }

        public void setViolationCount(String violationCount) {
            ViolationCount = violationCount;
        }

        public String getScores() {
            return Scores;
        }

        public void setScores(String scores) {
            Scores = scores;
        }

        public String getTotalTravelHours() {
            return TotalTravelHours;
        }

        public void setTotalTravelHours(String totalTravelHours) {
            TotalTravelHours = totalTravelHours;
        }

        public String getTotalTravelMinutes() {
            return TotalTravelMinutes;
        }

        public void setTotalTravelMinutes(String totalTravelMinutes) {
            TotalTravelMinutes = totalTravelMinutes;
        }

    }


}




