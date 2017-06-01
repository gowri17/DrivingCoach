package com.drava.android.activity.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.base.BaseFragment;
import com.drava.android.parser.UserInformationParser;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.ui.SeekbarWithIntervals;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DravaLog;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by admin on 12/9/2016.
 */

public class SettingsFragmentMentor extends BaseFragment implements CompoundButton.OnCheckedChangeListener ,SeekbarInterface{
    private SeekbarWithIntervals mSeekbarWithIntervals = null;
    private SwitchCompat switchVoilation, switchSwitchOff, switchGpsOff, switchForcequitApp;
    private int isSwitchOff = 0, isGpsOff = 0, isViolation = 0, isForceQuit ;
    private RelativeLayout tutorialLayout;
    private String LocationHoursLimit="";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_settings_mentee, container, false);
        init(view);
        setDefaults();
        return view;
    }

    private void init(View view) {
        List<String> seekbarIntervals = getIntervals();
        getSeekbarWithIntervals(view).setIntervals(seekbarIntervals,this);
        switchVoilation = (SwitchCompat) view.findViewById(R.id.switch_speed_limit_exceeded);
        switchSwitchOff = (SwitchCompat) view.findViewById(R.id.switch_mentee_turned_off_phone);
        switchGpsOff = (SwitchCompat) view.findViewById(R.id.switch_mentee_not_reachable);
        switchForcequitApp = (SwitchCompat)view.findViewById(R.id.switch_mentee_forcequit);
        tutorialLayout = (RelativeLayout)view.findViewById(R.id.tutorial_settings_mentee);
        callUserInformationWebService();
    }

    private void setDefaults(){
        if(!getApp().getUserPreference().getIsTutMenteeSettingsViewed()){
            getApp().getUserPreference().setIsTutMenteeSettingsViewed(true);
        }else{
            tutorialLayout.setVisibility(View.GONE);
        }

        tutorialLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tutorialLayout.setVisibility(View.GONE);
            }
        });
    }

    private void setupEvents() {
        switchVoilation.setOnCheckedChangeListener(this);
        switchSwitchOff.setOnCheckedChangeListener(this);
        switchGpsOff.setOnCheckedChangeListener(this);
        switchForcequitApp.setOnCheckedChangeListener(this);
    }

    private List<String> getIntervals() {
        return new ArrayList<String>() {{
            add("15 mins");
            add("30 mins");
            add("1 hr");
            add("2 hrs");
            add("3 hrs");
            add("4 hrs");
            add("5 hrs");
            add("6 hrs");
            add("7 hrs");
            add("8 hrs");
        }};
    }

    private SeekbarWithIntervals getSeekbarWithIntervals(View view) {
        if (mSeekbarWithIntervals == null) {
            mSeekbarWithIntervals = (SeekbarWithIntervals) view.findViewById(R.id.seekbarWithIntervals);
        }
        return mSeekbarWithIntervals;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        switch (compoundButton.getId()) {
            case R.id.switch_speed_limit_exceeded:
                if (isChecked) {
                    isViolation = 1;
                    getApp().getUserPreference().setNotificationSpeedExceed(true);
                } else {
                    isViolation = 0;
                    getApp().getUserPreference().setNotificationSpeedExceed(false);
                }
                callSettingWebservice();
                break;

            case R.id.switch_mentee_turned_off_phone:
                if (isChecked) {
                    isSwitchOff = 1;
                    getApp().getUserPreference().setNotificationMenteeDeviceSwithOff(true);
                } else {
                    isSwitchOff = 0;
                    getApp().getUserPreference().setNotificationMenteeDeviceSwithOff(false);
                }
                callSettingWebservice();
                break;

            case R.id.switch_mentee_not_reachable:
                if (isChecked) {
                    isGpsOff = 1;
                    getApp().getUserPreference().setNotificationMenteeNotReachable(true);
                } else {
                    isGpsOff = 0;
                    getApp().getUserPreference().setNotificationMenteeNotReachable(false);
                }
                callSettingWebservice();
                break;

            case R.id.switch_mentee_forcequit:
                if(isChecked){
                    isForceQuit = 1;
                    getApp().getUserPreference().setNotificationForceQuit(true);
                }else{
                    isForceQuit = 0;
                    getApp().getUserPreference().setNotificationForceQuit(false);
                }
                callSettingWebservice();
                break;
        }
    }

    private void callSettingWebservice() {

        if(!isAdded() || getActivity() == null || getApp()==null){      //  R.L
            return;
        }
        if(!DeviceUtils.isInternetConnected(getActivity())){
            AlertUtils.showAlert(getActivity(), getString(R.string.check_your_internet_connection));
        }else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("IsGpsOff", isGpsOff);
                jsonObject.put("IsViolation", isViolation);
                jsonObject.put("IsSwitchOff", isSwitchOff);
                jsonObject.put("IsForceQuit", isForceQuit);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), (jsonObject.toString()));
            getApp().getRetrofitInterface().changeSettings(requestBody).enqueue(new RetrofitCallback<ResponseBody>() {
                @Override
                public void onSuccessCallback(Call<ResponseBody> call, String content) {
                    super.onSuccessCallback(call, content);
                    DravaLog.print("Setting_webservice_response==>onSuccessCallback==>" + content);
                }

                @Override
                public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                    super.onFailureCallback(call, t, message, code);
                    DravaLog.print("Setting_webservice_response==>onFailureCallback==>" + message);
                }
            });
        }
    }

    public void callUserInformationWebService() {
        DravaApplication.getApp().getRetrofitInterface().getUserInformation().enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                UserInformationParser userInformationParser = new Gson().fromJson(content, UserInformationParser.class);
                if (userInformationParser != null && userInformationParser.getMeta().code == 200) {
                    isGpsOff = Integer.parseInt(userInformationParser.getUserDetails().IsGpsOff);
                    isViolation = Integer.parseInt(userInformationParser.getUserDetails().IsViolation);
                    isSwitchOff = Integer.parseInt(userInformationParser.getUserDetails().IsSwitchOff);
                    isForceQuit = Integer.parseInt(userInformationParser.getUserDetails().IsForceQuit);
                    //Pending
                    if (isGpsOff == 1){
                        switchGpsOff.setChecked(true);
                    }
                    if (isSwitchOff == 1){
                        switchSwitchOff.setChecked(true);
                    }
                    if (isViolation == 1){
                        switchVoilation.setChecked(true);
                    }
                    if(isForceQuit == 1){
                        switchForcequitApp.setChecked(true);
                    }
                }
                setupEvents();
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                setupEvents();
            }
        });
    }

    @Override
    public void onSeekBarChange(String value) {
        LocationHoursLimit = /*value*/"";
        callSettingWebservice();
    }
}
