package com.drava.android.activity.leftmenu;

import android.content.Context;
import android.content.res.Resources;

import com.drava.android.R;

import java.util.ArrayList;

public class MenuCollections {

    //Menu List Collections for Mentee
    static ArrayList<LeftMenuDrawerItem> getMenteeMenuItems(Context context) {
        ArrayList<LeftMenuDrawerItem> items = new ArrayList<>();
        Resources res = context.getResources();
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.HOME, R.drawable.ic_home, res.getString(R.string.str_home)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.TRIPS, R.drawable.ic_trips, res.getString(R.string.str_trips)));
//        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.VIOLATION, R.drawable.ic_violation, res.getString(R.string.str_violation)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.MY_MENTOR, R.drawable.ic_mymentee_or_mentor, res.getString(R.string.str_mentor)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.MY_PROFILE, R.drawable.ic_myprofile, res.getString(R.string.str_profile)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.NOTIFICATION, R.drawable.notification, res.getString(R.string.str_notification)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.INVITE, R.drawable.ic_invite, res.getString(R.string.str_invite)));
//        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.SETTINGS, R.drawable.ic_settings, res.getString(R.string.str_settings)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.HELP, R.drawable.help, res.getString(R.string.help)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.DIVIDER));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.TERMS_OF_SERVICES, R.drawable.ic_termsofservices, res.getString(R.string.str_terms)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.PRIVACY, R.drawable.ic_privcay, res.getString(R.string.str_privacy)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.ABOUT, R.drawable.about, res.getString(R.string.str_about)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.DIVIDER));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.SIGNOUT, R.drawable.sign_out, res.getString(R.string.str_signout)));
        return items;
    }

    //Menu List Collections for Mentor
    static ArrayList<LeftMenuDrawerItem> getMentorMenuItems(Context context) {
        ArrayList<LeftMenuDrawerItem> items = new ArrayList<>();
        Resources res = context.getResources();
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.HOME, R.drawable.ic_home, res.getString(R.string.str_home)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.MY_MENTEES, R.drawable.ic_mymentee_or_mentor, res.getString(R.string.str_my_mentees)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.MY_PROFILE, R.drawable.ic_myprofile, res.getString(R.string.str_profile)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.NOTIFICATION, R.drawable.notification, res.getString(R.string.str_notification)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.INVITE, R.drawable.ic_invite, res.getString(R.string.str_invite)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.SETTINGS, R.drawable.ic_settings, res.getString(R.string.str_settings)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.HELP, R.drawable.help, res.getString(R.string.help)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.DIVIDER));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.TERMS_OF_SERVICES, R.drawable.ic_termsofservices, res.getString(R.string.str_terms)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.PRIVACY, R.drawable.ic_privcay, res.getString(R.string.str_privacy)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.ABOUT, R.drawable.about, res.getString(R.string.str_about)));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.DIVIDER));
        items.add(new LeftMenuDrawerItem(LeftMenuDrawerItem.Type.SIGNOUT, R.drawable.sign_out, res.getString(R.string.str_signout)));
        return items;
    }
}
