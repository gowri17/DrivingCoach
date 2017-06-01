package com.drava.android.welcome;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by admin on 10/18/2016.
 */

public class WelcomePageAdapter extends FragmentStatePagerAdapter {

    public WelcomePageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        int index = position % getCount();
        return  WelcomeImageFragment.newInstance(index);
    }

    @Override
    public int getCount() {
        return 16;
    }
}
