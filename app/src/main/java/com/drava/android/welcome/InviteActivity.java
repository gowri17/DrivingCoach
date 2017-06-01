package com.drava.android.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.HomeActivity;
import com.drava.android.activity.contacts.GetContactsActivity;
import com.drava.android.base.AppConstants;
import com.drava.android.base.BaseActivity;
import com.drava.android.base.Log;

/**
 * Created by admin on 10/19/2016.
 */

public class InviteActivity extends BaseActivity implements AppConstants{

    public static final String INVITE = "invite";

    private TextView txtInivite,txtCongratulation,txtYourAlmostDone,txtInviteDesc,txtMentorYouInvite,txtLink,txtSeeDriving,txtGetNotified,txtViewScores,txtViewTrips;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_mentees);
        init();
        setupDefaults();
        setupEvents();
    }

    private void init(){
        txtInivite = (TextView)findViewById(R.id.txt_invite);
        txtCongratulation = (TextView)findViewById(R.id.txt_congratulation);
        txtYourAlmostDone = (TextView)findViewById(R.id.txt_your_almost_done);
        txtInviteDesc = (TextView)findViewById(R.id.txt_invite_desc);
        txtMentorYouInvite = (TextView)findViewById(R.id.txt_mentor_you_invite);
        txtLink = (TextView)findViewById(R.id.txt_link);
        txtSeeDriving = (TextView) findViewById(R.id.txt_see_driving);
        txtGetNotified = (TextView)findViewById(R.id.txt_get_notified);
        txtViewScores = (TextView)findViewById(R.id.txt_view_scores);
        txtViewTrips = (TextView)findViewById(R.id.txt_view_trips);
    }

    private void setupDefaults() {
        getApp().getUserPreference().setIsInvitePageShown(true);
        setStatusBarColor();
        Log.e("Page Status", "Visited Invite Initial Screen");
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTOR)){
            setToolbar(getString(R.string.invite_mentees));
            txtInviteDesc.setText(getResources().getString(R.string.invite_mentees_description));
            txtCongratulation.setVisibility(View.GONE);
            txtYourAlmostDone.setVisibility(View.GONE);
            txtMentorYouInvite.setVisibility(View.GONE);
            txtInivite.setText(getString(R.string.invite_mentees));
            txtLink.setText(getString(R.string.link_seamlessly_to_multiple_mentees));
            txtSeeDriving.setText(getString(R.string.see_driving_expections));
            txtGetNotified.setText(getString(R.string.get_notified));
            txtViewScores.setText(getString(R.string.view_driver_scores));
            txtViewTrips.setText(getString(R.string.view_trips_taken));

        }else if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)){
            setToolbar(getString(R.string.invite_mentor));
            txtInviteDesc.setText(getResources().getString(R.string.invite_mentor_description));
            txtCongratulation.setVisibility(View.VISIBLE);
            txtYourAlmostDone.setVisibility(View.VISIBLE);
            txtMentorYouInvite.setVisibility(View.VISIBLE);
            txtInivite.setText(getString(R.string.str_invite_mentor));
            txtLink.setText(getString(R.string.seamlessly_linked_to_you));
            txtSeeDriving.setText(getString(R.string.see_your_driving_exception));
            txtGetNotified.setText(getString(R.string.get_notified_of_your_driving));
            txtViewScores.setText(getString(R.string.be_able_to_view_your_safety_scores));
            txtViewTrips.setText(getString(R.string.be_able_to_view_your_trip_history));
        }
    }

    private void setupEvents(){
        txtInivite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(InviteActivity.this, GetContactsActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_skip,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_skip){
            startActivity(new Intent(InviteActivity.this, HomeActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        super.onBackPressed();
    }
}
