package com.drava.android.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.AuthorizationActivity;
import com.drava.android.base.AppConstants;
import com.drava.android.base.BaseActivity;

public class ProfileActivity extends BaseActivity implements AppConstants{
    private LinearLayout llMentor,llMentee;
    private TextView txtMentor,txtMentee;
    private ImageView imgMentor,imgMentee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        init();
        setUpEvents();
    }

    private void init(){
        llMentor = (LinearLayout) findViewById(R.id.ll_mentor);
        llMentee = (LinearLayout) findViewById(R.id.ll_mentee);
        imgMentee = (ImageView) findViewById(R.id.img_mentee);
        imgMentor = (ImageView) findViewById(R.id.img_mentor);
        txtMentee = (TextView) findViewById(R.id.txt_mentee);
        txtMentor = (TextView) findViewById(R.id.txt_mentor);
        setToolbar(getResources().getString(R.string.profile));
        setStatusBarColor();
    }

    private void setUpEvents(){
        llMentee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgMentor.setSelected(false);
                txtMentor.setTextColor(ContextCompat.getColor(ProfileActivity.this,R.color.text_dark_color));
                imgMentee.setSelected(true);
                txtMentee.setTextColor(ContextCompat.getColor(ProfileActivity.this,R.color.theme_color));
                getApp().getUserPreference().setMentorOrMentee(MENTEE);
                startActivity( new Intent(ProfileActivity.this,ReferralActivity.class));
            }
        });
        llMentor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imgMentee.setSelected(false);
                txtMentee.setTextColor(ContextCompat.getColor(ProfileActivity.this,R.color.text_dark_color));
                imgMentor.setSelected(true);
                txtMentor.setTextColor(ContextCompat.getColor(ProfileActivity.this,R.color.theme_color));
                getApp().getUserPreference().setMentorOrMentee(MENTOR);
                startActivity(new Intent(ProfileActivity.this,ReferralActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
        startActivity(new Intent(ProfileActivity.this, AuthorizationActivity.class));
    }
}
