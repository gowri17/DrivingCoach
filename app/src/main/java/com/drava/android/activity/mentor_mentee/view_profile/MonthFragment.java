package com.drava.android.activity.mentor_mentee.view_profile;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drava.android.activity.mentor_mentee.ViewTripsActivity;
import com.drava.android.base.BaseFragment;
import com.drava.android.base.Log;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.ui.DravaButton;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.TextUtils;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.IllegalFormatCodePointException;
import java.util.Iterator;

import com.drava.android.R;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class MonthFragment extends BaseFragment {
    protected View rootView;
    protected TextView txtviewTotalScore;
    protected TextView txtviewMaxSpeed;
    protected TextView txtviewViolation;
    protected TextView txtviewTimehr;
    protected TextView txtviewTimemin;
    protected TextView txtviewDistance;
    protected String start;
    protected TextView txtviewNoResult;
    protected LinearLayout layoutFragment;
    protected ProfileBeanClass.MenteeProfileBean rowContent;
    protected ProfileBeanClass rowProfile;
    protected ProgressDialog progressDialog;
    protected Button btnViewTrips;
    private String userId;
    private String profilePhoto;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_month, container, false);
        init();
        setupDefaults();
        setupEvents();
        return rootView;
    }

    public void init() {
        txtviewTotalScore = (TextView) rootView.findViewById(R.id.txtviewTotalScore);
        txtviewMaxSpeed = (TextView) rootView.findViewById(R.id.txtviewMaxSpeed);
        txtviewViolation = (TextView) rootView.findViewById(R.id.txtviewViolation);
        txtviewTimehr = (TextView) rootView.findViewById(R.id.txtviewTimeHour);
        txtviewTimemin = (TextView) rootView.findViewById(R.id.txtviewTimeMinute);
        txtviewDistance = (TextView) rootView.findViewById(R.id.txtviewDistance);
        start = "0";
        txtviewNoResult = (TextView) rootView.findViewById(R.id.txt_no_record_found);
        layoutFragment = (LinearLayout) rootView.findViewById(R.id.monthFragmentLayout);
        btnViewTrips = (Button) rootView.findViewById(R.id.btnViewTrips);
    }

    public void setupDefaults() {
        userId = getArguments().getString("menteeID");
        profilePhoto = getArguments().getString("profilePhoto");
        setupGettingTripReport();
    }

    public void setupEvents() {
        btnViewTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isNullOrEmpty(userId)){
                    Intent intent = new Intent(getActivity(), ViewTripsActivity.class);
                    intent.putExtra(MENTEE_ID, userId);
                    intent.putExtra(PROFILE_PHOTO, profilePhoto);
                    startActivity(intent);
                }
            }
        });
    }

    public void setupGettingTripReport() {

        if (DeviceUtils.isInternetConnected(getActivity())) {

            getTripReport(true);
        } else {
            AlertUtils.showAlert(getActivity(), getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }, false);
        }

    }

    public void getTripReport(boolean showProgressBar) {

        showProgress(showProgressBar);
        getApp().getRetrofitInterface().getTripReport(userId, String.valueOf(start), "1").enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                super.onFailure(call, t);
            }

            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);

                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                    progressDialog.dismiss();
                }

//                Log.i("Error Code", "Month Code" + rowProfile.reportList.report);

                try {

                    layoutFragment.setVisibility(View.VISIBLE);
                    rowProfile = new Gson().fromJson(content, ProfileBeanClass.class);

                    if (rowProfile != null && rowProfile.meta.code == 200) {
                        JSONObject mainJSONObj = null;
                        mainJSONObj = new JSONObject(content);

                        JSONObject menteeReportObject = mainJSONObj.getJSONObject(rowProfile.meta.dataPropertyName);

                        JSONObject categoryJSONObj = menteeReportObject.getJSONObject("report");

                        Iterator<String> iterator = categoryJSONObj.keys();

                        while (iterator.hasNext()) {
                            String key = iterator.next();

                            Log.i("TAG Prethivi", "key:" + key + "--Value::" + categoryJSONObj.optString(key));

                            categoryJSONObj.optString(key);

                            JSONArray dynamicArray = categoryJSONObj.getJSONArray(key);
                            rowContent = new Gson().fromJson(dynamicArray.get(0).toString(), ProfileBeanClass.MenteeProfileBean.class);

                        }
                        txtviewTotalScore.setText(rowContent.Scores);
                        txtviewMaxSpeed.setText(rowContent.MaxSpeed);
                        txtviewDistance.setText(rowContent.TotalDistance);
                        txtviewTimehr.setText(rowContent.TotalTravelHours);
                        txtviewTimemin.setText(rowContent.TotalTravelMinutes);
                        txtviewViolation.setText(rowContent.ViolationCount);

                    } else {
                        layoutFragment.setVisibility(View.GONE);
                        txtviewNoResult.setVisibility(View.VISIBLE);
                        txtviewNoResult.setText(rowProfile.meta.errorMessage);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                DravaLog.print(" Month Response Successfull Prethivi=>" + content);
                AppLog.print(getActivity(), "getTripReport===================>Successfull");
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                    progressDialog.dismiss();
                }
                if (t!=null &&t.getCause()!=null) {
                    AppLog.print(getActivity(), "getTripReport===================>Failed==============>Cause=>"+t.getCause());
                }
            }
        });
    }

    public void showProgress(boolean showProgress) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        if (showProgress) {
            progressDialog.show();
        }

    }

}
