package com.drava.android.activity.map.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.drava.android.model.EndTrip;
import com.drava.android.parser.ContentParser;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.TextUtils;

import static com.drava.android.DravaApplication.getApp;
import static com.drava.android.DravaApplication.mContext;
import static com.drava.android.base.AppConstants.MENTEE;

/**
 * Created by admin on 1/2/2017.
 */

public class TurnOffReceiver extends BroadcastReceiver {

    private Context mReceiverContext;
    private static String mLastState;

    @Override
    public void onReceive(Context context, Intent intent) {
        DravaLog.print("===>TurnOffReceiver==>");
        mReceiverContext = context;
        AppLog.print(mReceiverContext, "===>TurnOffReceiver==>");
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
            endTripBeforeSwitchOff();
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE); //R.L v1.1
//            if(!TextUtils.isNullOrEmpty(state) && !state.equals(mLastState)){   //handling of broadcast receiver called more than one time during switch off
//                mLastState = state;
            callTurnOffWebService(context);
//            }
        } else {
            AppLog.print(mReceiverContext, "===>TurnOffReceiver==>For mentor");
        }
    }

    private void endTripBeforeSwitchOff() {
        if (getApp().getDBSQLite().getFirstTripId() != 0) {
            long offlineTripId = getApp().getDBSQLite().getFirstTripId();
            AppLog.print(mReceiverContext, "===>TurnOffReceiver==>offlineTripId==>" + offlineTripId);
            DravaLog.print("===>TurnOffReceiver==>offlineTripId==>" + offlineTripId);
            EndTrip endTripObject = getApp().getDBSQLite().getEndTripInfo(offlineTripId);
            if (endTripObject == null || endTripObject.EndLatitude == null || endTripObject.EndLongitude == null) {
                //Trip not ended
                AppLog.print(mReceiverContext, "===>TurnOffReceiver==>endTripObject==>" + endTripObject);
                EndTrip endTrip = getApp().getUserPreference().getEndTripInfo();
                AppLog.print(mReceiverContext, "===>TurnOffReceiver==>endTrip.EndTime==>" + endTrip.EndTime + "==>endTrip.EndLatitude=>" + endTrip.EndLatitude
                        + "==> endTrip.EndLongitude==>" + endTrip.EndLongitude + "=>endTrip.Distance=>" + endTrip.Distance + "=>endTrip.MinSpeed=>" + endTrip.MinSpeed + "=>endTrip.MaxSpeed=>" + endTrip.MaxSpeed);
                getApp().getDBSQLite().updateEndTrip(offlineTripId,
                        endTrip.EndTime,
                        endTrip.EndLatitude,
                        endTrip.EndLongitude,
                        endTrip.Distance,
                        endTrip.MinSpeed,
                        endTrip.MaxSpeed);
            }
        }
    }

    private void callTurnOffWebService(Context context) {
        Intent intent = new Intent(context, TurnOffService.class);
        context.startService(intent);
    }
}
