package com.drava.android.activity.mentor_mentee.view_profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class FragmentTabAdapter extends FragmentStatePagerAdapter {

    protected int tabcount;
    protected String userId;
    protected String profilePhoto;

    public FragmentTabAdapter(FragmentManager fmanager, int counttab, String userid, String profilePhoto) {
        super(fmanager);
        tabcount = counttab;
        this.userId = userid;
        this.profilePhoto = profilePhoto;
    }


    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("menteeID", userId);
        bundle.putString("profilePhoto", profilePhoto);
        switch (position) {
            case 0:
                MonthFragment monthFragment = new MonthFragment();
                monthFragment.setArguments(bundle);
                return monthFragment;

            case 1:
                OverallFragment overallFragment = new OverallFragment();
                overallFragment.setArguments(bundle);
                return overallFragment;

            default:
                return null;
        }
    }


    @Override
    public int getCount() {
        return tabcount;
    }
}
