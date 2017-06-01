package com.drava.android.activity.leftmenu;

/**
 * Created by evuser on 25-10-2016.
 */

public class LeftMenuDrawerItem {
    int icon;
    String title, linkUrl, content, menuIcon;
    int unreadCount;
    boolean selected;
    Type type;

    public LeftMenuDrawerItem(Type type) {
        this.type = type;
    }

    public LeftMenuDrawerItem(String title) {
        this.title = title;
    }

    public LeftMenuDrawerItem(Type type, String title) {
        this.title = title;
        this.type = type;
    }

    public LeftMenuDrawerItem(Type type, int icon, String title) {
        this.icon = icon;
        this.title = title;
        this.type = type;
    }

    public LeftMenuDrawerItem(Type type, int icon, String title, int unreadCount) {
        this.type = type;
        this.title = title;
        this.unreadCount = unreadCount;
        this.icon = icon;
    }

    public enum Type{
        HOME,
        TRIPS,
        VIOLATION,
        MY_MENTEES,
        MY_MENTOR,
        MY_PROFILE,
        NOTIFICATION,
        INVITE,
        SETTINGS,
        HELP,
        TERMS_OF_SERVICES,
        PRIVACY,
        ABOUT,
        DIVIDER,
        SIGNOUT,
        VERSION
    }
}
