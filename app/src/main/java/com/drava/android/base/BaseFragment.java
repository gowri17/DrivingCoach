package com.drava.android.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.drava.android.DravaApplication;
import com.drava.android.R;

public class BaseFragment extends Fragment implements AppConstants {

    public Toolbar toolbar;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public BaseFragment getActiveFragment() {
        return (BaseFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.content_scrolling);//TODO CHANGE ID according to the fragment container.
    }

    public DravaApplication getApp() {
        return (DravaApplication) getActivity().getApplication();
    }


}
