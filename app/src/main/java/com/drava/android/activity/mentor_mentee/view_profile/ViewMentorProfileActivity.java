package com.drava.android.activity.mentor_mentee.view_profile;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.mentor_mentee.MentorListParser;
import com.drava.android.base.BaseActivity;
import com.drava.android.base.Log;
import com.drava.android.utils.TextUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

public class ViewMentorProfileActivity extends BaseActivity {

    private MentorListParser.MentorList mentorDetails;
    private TextView txtFirstName, txtLastName, txtAddress;
    private ImageView imgMentor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_mentor_profile);
        init();
        setDefaults();
    }

    private void init(){
        txtFirstName = (TextView)findViewById(R.id.txt_first_name);
        txtLastName = (TextView)findViewById(R.id.txt_last_name);
        txtAddress = (TextView)findViewById(R.id.txt_address);
        imgMentor = (ImageView)findViewById(R.id.img_mentor);
    }

    private void setDefaults(){
        setStatusBarColor();
        setToolbar(getString(R.string.str_mentor_profile));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if(getIntent().hasExtra(MENTOR_LIST)){
            mentorDetails = (MentorListParser.MentorList) getIntent().getExtras().getSerializable(MENTOR_LIST);
            assert mentorDetails != null;
            if(mentorDetails != null) {
                Log.d("MentorDetails", "" + mentorDetails.LastName);
                txtFirstName.setText(mentorDetails.FirstName);
                txtLastName.setText(mentorDetails.LastName);
                if(!TextUtils.isNullOrEmpty(mentorDetails.Photo)){
                    Picasso.with(ViewMentorProfileActivity.this).load(mentorDetails.Photo).placeholder(R.drawable.mentee).into(imgMentor);
                }else{
                    Picasso.with(ViewMentorProfileActivity.this).load(R.drawable.mentee).placeholder(R.drawable.mentee).into(imgMentor);
                }

                txtAddress.setText(mentorDetails.CurrentLocation);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
