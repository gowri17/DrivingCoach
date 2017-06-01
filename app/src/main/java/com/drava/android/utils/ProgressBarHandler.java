package com.drava.android.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;

import com.drava.android.R;


public class ProgressBarHandler {
    private ProgressBar mProgressBar;
    private ProgressDialog mProgressDialog;
    private Context mContext;

    public ProgressBarHandler(Context context) {
        mContext = context;
        mProgressDialog = new ProgressDialog(mContext, R.style.Theme_CarFit_Alert);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Large);

        hide();
    }

    public void show() {
        if (mProgressDialog != null) {
            mProgressDialog.show();
        }
    }

    public void hide() {
        if (mProgressDialog != null) {
            mProgressDialog.hide();
        }
    }
}