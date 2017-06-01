package com.drava.android.activity.my_profile;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.activity.HomeActivity;
import com.drava.android.activity.contacts.GetContactsActivity;
import com.drava.android.activity.mentor_mentee.MentorListParser;
import com.drava.android.base.BaseFragment;
import com.drava.android.parser.UserInformationParser;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.TextUtils;
import com.drava.android.welcome.InviteActivity;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class MyProfileFragment extends BaseFragment {
    private ImageView menuIconClick, menteeProfileImage;
    private com.drava.android.ui.DravaTextView txtName, txtOverallScore,txtReferralPoints,txtLocation, txtAboutme;
    private RelativeLayout rlOverallScore;
    private LinearLayout llMyMentor;
    private MentorListParser.MentorList mentorMenteeList;
    private View aboutmeBottomLine;
    private ImageView markerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);
        init(view);
        setupDefault();
        setupEvents();
        return view;
    }

    public void init(View view) {
        menuIconClick = (ImageView) view.findViewById(R.id.mentee_profile_menu_icon);
        txtName = (com.drava.android.ui.DravaTextView) view.findViewById(R.id.txt_name);
        txtOverallScore = (com.drava.android.ui.DravaTextView) view.findViewById(R.id.txt_overall_score);
        txtReferralPoints = (com.drava.android.ui.DravaTextView) view.findViewById(R.id.txt_referral_point);
        txtLocation = (com.drava.android.ui.DravaTextView) view.findViewById(R.id.txt_location);
        menteeProfileImage = (ImageView) view.findViewById(R.id.mentee_profile_image);
        rlOverallScore = (RelativeLayout) view.findViewById(R.id.rl_overall_Score);
        llMyMentor = (LinearLayout) view.findViewById(R.id.ll_my_mentor);
        txtAboutme = (com.drava.android.ui.DravaTextView)view.findViewById(R.id.txt_about_me);
        aboutmeBottomLine = view.findViewById(R.id.about_me_bottom_line);
        markerView = (ImageView)view.findViewById(R.id.img_marker);
    }

    public void setupDefault() {
        txtName.setText(getApp().getUserPreference().getFirstName()+" "+getApp().getUserPreference().getLastName());
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)){
            rlOverallScore.setVisibility(View.VISIBLE);
            txtOverallScore.setText(getApp().getUserPreference().getOverallScores()+""+getResources().getString(R.string.points));
            txtReferralPoints.setText(getApp().getUserPreference().getReferralPoints()+""+getResources().getString(R.string.points_earned));
        }else {
            llMyMentor.setVisibility(View.GONE);
            rlOverallScore.setVisibility(View.GONE);
            txtAboutme.setVisibility(View.GONE);
            aboutmeBottomLine.setVisibility(View.GONE);

            final ProgressDialog progDailog1 = new ProgressDialog(getActivity());       //R.L v1.1
            progDailog1.setMessage("Loading...  ");
            progDailog1.setIndeterminate(false);
            progDailog1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog1.setCancelable(false);
            progDailog1.show();

            DravaApplication.getApp().getRetrofitInterface().getUserInformation().enqueue(new RetrofitCallback<ResponseBody>() {
                @Override
                public void onSuccessCallback(Call<ResponseBody> call, String content) {
                    super.onSuccessCallback(call, content);
                    if (null != getActivity() && progDailog1.isShowing() && isAdded()) {
                        progDailog1.dismiss();
                    }
                    UserInformationParser userInformationParser = new Gson().fromJson(content, UserInformationParser.class);
                    if(userInformationParser.getMeta().code == 200){
                        getApp().getUserPreference().setOverallScores(userInformationParser.getUserDetails().OverallScores);
                        getApp().getUserPreference().setReferralPoints(userInformationParser.getUserDetails().ReferralPoints);
                        txtReferralPoints.setText(getApp().getUserPreference().getReferralPoints()+""+getResources().getString(R.string.points_earned));
                    }
                }

                @Override
                public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                    super.onFailureCallback(call, t, message, code);
                    if (null != getActivity() && progDailog1.isShowing() && isAdded()) {
                        progDailog1.dismiss();
                    }
                }
            });
        }
        if (TextUtils.isNullOrEmpty(getApp().getUserPreference().getPhoto())) {
            menteeProfileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            menteeProfileImage.setImageResource(R.drawable.default_image);
            menteeProfileImage.setBackgroundColor(Color.parseColor("#E9E9E9"));
        } else {
            Picasso.with(getActivity()).load(getApp().getUserPreference().getPhoto()).into(menteeProfileImage);
        }
        if(TextUtils.isNullOrEmpty(getApp().getUserPreference().getCurrentLocation())){
            markerView.setVisibility(View.GONE);
        }else {
            markerView.setVisibility(View.VISIBLE);
            txtLocation.setText(getApp().getUserPreference().getCurrentLocation());
        }
    }

    public void setupEvents() {
        menuIconClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((HomeActivity) getActivity()).mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    ((HomeActivity) getActivity()).mDrawerLayout.closeDrawer(GravityCompat.START);

                } else {
                    ((HomeActivity) getActivity()).mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
    }
}

