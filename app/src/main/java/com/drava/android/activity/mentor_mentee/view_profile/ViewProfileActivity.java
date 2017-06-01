package com.drava.android.activity.mentor_mentee.view_profile;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.mentor_mentee.MentorListParser;
import com.drava.android.base.AppConstants;
import com.drava.android.base.BaseActivity;
import com.drava.android.base.Log;
import com.drava.android.utils.TextUtils;
import com.squareup.picasso.Picasso;

public class ViewProfileActivity extends BaseActivity implements AppConstants {

    protected Toolbar toolbar;
    protected ActionBar actionBar;
    protected TabLayout tabLayout;
    protected ViewPager viewPager;
    protected FragmentTabAdapter fragmentTabAdapter;
    protected CollapsingToolbarLayout collapsingToolbarLayout;
    protected MentorListParser.MentorList mentorDetails;
    protected ImageView imgProfile;
    protected TextView txtViewToolbarTitle, txtLocation;
    protected AppBarLayout appBarLayout;
    private String mentorName, profilePhoto, currentLocation, userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        init();
        setupDefaults();
        setupEvents();
    }

    public void init() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_feed);
        viewPager = (ViewPager) findViewById(R.id.view_pager_feed);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        imgProfile = (ImageView) findViewById(R.id.img_profile);
        txtViewToolbarTitle = (TextView) findViewById(R.id.txt_view_toobar_title);
        txtLocation = (TextView) findViewById(R.id.txt_location);
    }

    public void setupDefaults() {
        //When control comes from MenteeMentorAdapter to view profile
        if(getIntent().hasExtra(MENTOR_LIST)){
            mentorDetails = (MentorListParser.MentorList) getIntent().getExtras().getSerializable(MENTOR_LIST);
            assert mentorDetails != null;
            if(mentorDetails != null) {
                Log.d("MentorDetails", "" + mentorDetails.LastName);
                mentorName = mentorDetails.FirstName + " " + mentorDetails.LastName;
                profilePhoto = mentorDetails.Photo;
                currentLocation = mentorDetails.CurrentLocation;
                userId = mentorDetails.UserId;
            }
        }

        //When control comes from MenteeList of bottom sheet mentor click
        if(getIntent().hasExtra(FIRST_NAME)){
            mentorName = getIntent().getStringExtra(FIRST_NAME)+" "+getIntent().getStringExtra(LAST_NAME);
            profilePhoto = getIntent().getStringExtra(PROFILE_PHOTO);
            currentLocation = getIntent().getStringExtra(CURRENT_LOCATION);
            userId = getIntent().getStringExtra(MENTEE_ID);
        }

        collapsingToolbarLayout.setTitle(mentorName);
        if (!TextUtils.isNullOrEmpty(profilePhoto)) {
            Picasso.with(getApplicationContext()).load(profilePhoto).placeholder(R.drawable.mentee).into(imgProfile);
        } else {
            imgProfile.setImageResource(R.drawable.mentee);
        }
        if (!TextUtils.isNullOrEmpty(currentLocation)) {
            txtLocation.setText(currentLocation);
        }else {
            txtLocation.setVisibility(View.GONE);
        }
        settingActionBar();
        setuptabLayout();
    }

    public void setupEvents() {
    }

    public void settingActionBar() {
        actionBar = getSupportActionBar();
        assert actionBar != null;
        setStatusBarTranslucent(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    public void setuptabLayout() {

        tabLayout.addTab(tabLayout.newTab().setText(R.string.str_this_month));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.str_overall_month));
        profilePhoto = (TextUtils.isNullOrEmpty(profilePhoto))?"":profilePhoto;
        fragmentTabAdapter = new FragmentTabAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), userId, profilePhoto);
        viewPager.setAdapter(fragmentTabAdapter);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void settingMenteeProfile() {


    }
}
