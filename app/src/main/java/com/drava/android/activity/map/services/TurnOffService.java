package com.drava.android.activity.map.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.drava.android.rest.RetrofitCallback;

import okhttp3.ResponseBody;
import retrofit2.Call;

import static com.drava.android.DravaApplication.getApp;

public class TurnOffService extends Service {
    private String TAG = TurnOffService.class.getSimpleName();


    public TurnOffService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        callTurnOffWebService();
    }

    private void callTurnOffWebService(){
        getApp().getRetrofitInterface().updateGPSStatus("0", "1","0","2").enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                Log.e(TAG, "Device Notification updated Success");
                stopSelf();
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                Log.e(TAG, "Device Notification updated Success");
                stopSelf();
            }
        });
    }
}
