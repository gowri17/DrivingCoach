package com.drava.android.base;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.utils.TextUtils;
import com.squareup.picasso.Picasso;

import static com.drava.android.DravaApplication.getApp;

public class BaseActivity extends AppCompatActivity implements AppConstants {
    public Toolbar toolbar;
    public ImageView imgUser,imgPassenger;
    public LinearLayout llPassenger;
    public SwitchCompat switchPassenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public DravaApplication getApp() {
        return (DravaApplication) getApplication();
    }

    public void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.color_status_bar));
        }
    }

    public void setToolbar(String title) {
        toolbar = (Toolbar) findViewById(R.id.default_toolbar);
        setSupportActionBar(toolbar);
        TextView txtTitle = (TextView) toolbar.findViewById(R.id.txt_title);
        txtTitle.setText(title);
        llPassenger = (LinearLayout) toolbar.findViewById(R.id.ll_passenger);
        switchPassenger = (SwitchCompat) toolbar.findViewById(R.id.switch_passenger);
        imgPassenger = (ImageView) toolbar.findViewById(R.id.img_passenger);
    }

    public void hideToolbar(){
        getSupportActionBar().hide();
    }

    public void setUserImage(String url) {
        imgUser = (ImageView) toolbar.findViewById(R.id.img_user);
        imgUser.setVisibility(View.VISIBLE);
        Picasso.with(this).load(url).into(imgUser);
    }

    public void setPassengerToogle(boolean visible) {
        if (llPassenger != null) {
            if (visible) {
                llPassenger.setVisibility(View.VISIBLE);
            } else
                llPassenger.setVisibility(View.GONE);
        }
    }

    public void setPassengerImage(int visible) {
        imgPassenger.setVisibility(visible);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
