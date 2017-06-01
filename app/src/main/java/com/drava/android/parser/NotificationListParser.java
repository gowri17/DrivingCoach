package com.drava.android.parser;

import java.io.Serializable;
import java.util.List;

public class NotificationListParser implements Serializable {

    public Meta meta;
    public List<NotificationTrackingList> notificationTrackingList;
    public List<String> notifications;

    public class NotificationTrackingList
    {
        public String FirstName;
        public String LastName;
        public String Photo;
        public String FromUser;
        public String NotificationId;
        public String Message;
        public String Type;
        public String DateCreated;
        public String TripId;
        public String CurrentLocation;
        public String ThumbnailPhoto;
    }
}
