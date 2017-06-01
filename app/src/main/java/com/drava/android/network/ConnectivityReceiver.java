package com.drava.android.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.drava.android.utils.DeviceUtils;

public class ConnectivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        onStateChange(DeviceUtils.isInternetConnected(context));
    }

    public void onStateChange(boolean isConnected) {
    }
}
