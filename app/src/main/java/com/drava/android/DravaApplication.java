package com.drava.android;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;


import com.drava.android.model.EndTrip;
import com.drava.android.activity.map.services.PollReceiver;
import com.drava.android.base.AppConstants;
import com.drava.android.base.Log;
import com.drava.android.database.DBSQLite;
import com.drava.android.fcm.NotificationPreference;
import com.drava.android.preference.DravaPreference;
import com.drava.android.rest.DravaApiClient;
import com.drava.android.rest.DravaApiInterface;
import com.crashlytics.android.Crashlytics;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.rest.SyncHelper;
import com.drava.android.utils.AppLog;

import io.fabric.sdk.android.Fabric;
import okhttp3.ResponseBody;
import retrofit2.Call;


public class DravaApplication extends Application implements AppConstants {
    public static Context mContext;
    private DBSQLite mDBSQLite;
    protected static DravaApplication mInstance;
    private DravaPreference mSharedPreferences;
    private DravaApiClient mDravaApi;
    private static NotificationPreference mSNSPreference;
    private static final String TAG = "DravaApplication";
    private boolean mIsInternetAvailable;
    private SyncHelper syncHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mContext = getApplicationContext();
        mSharedPreferences = new DravaPreference(this);
        mDravaApi = new DravaApiClient(this);
        mDBSQLite = DBSQLite.getInstance(this);
        syncHelper = new SyncHelper(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mDBSQLite.getDatabase();
            }
        }).start();

        if (BuildConfig.ENABLE_CRASHLYTICS) {
            Fabric.with(this, new Crashlytics());
        } else {
            Log.e(TAG, "crashlytics disable");
        }
    }


    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized Context getContext() {
        return mContext;
    }

    public static DravaApplication getApp() {
        if (mInstance != null && mInstance instanceof DravaApplication) {
            return mInstance;
        } else {
            mInstance = new DravaApplication();
            mInstance.onCreate();
            return mInstance;
        }
    }

    public DravaPreference getUserPreference() {
        return mSharedPreferences;
    }

    public DravaApiClient getWebService() {
        return mDravaApi;
    }

    public DravaApiInterface getRetrofitInterface() {
        return mDravaApi.getClientInterface();
    }

    public static NotificationPreference getSnsPreference() {

        if (mSNSPreference == null) {
            mSNSPreference = new NotificationPreference(getContext());
        }
        return mSNSPreference;

    }

    public void appDestroyed(){
        scheduleServiceCall();
    }

    public DBSQLite getDBSQLite() {
        return mDBSQLite;
    }

    public void closeDateBase() {
        if (mDBSQLite != null) {
            mDBSQLite.close();
        }
    }

    private void scheduleServiceCall() {
        /* Request the AlarmManager object */
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        /* Create the PendingIntent that will launch the BroadcastReceiver */
        PendingIntent pending = PendingIntent.getBroadcast(this, 0, new Intent(this, PollReceiver.class), 0);

        /* Schedule Alarm with and authorize to WakeUp the device during sleep */
        manager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1 * 1000, pending);
    }

    public void setInternetStatus(boolean isConnected) {
        mIsInternetAvailable = isConnected;
    }

    public boolean isInternetConnected() {
        return mIsInternetAvailable;
    }

    public SyncHelper getSyncHelper() {
        return syncHelper;
    }

    public String getDBURL(){
        String BASE_URL;
        if(BuildConfig.IS_CUSTOMER_VERSION){
            BASE_URL = "http://dravabeta.us-west-2.elasticbeanstalk.com/";
        }else{
            BASE_URL = "http://drava.us-west-2.elasticbeanstalk.com/";
//            BASE_URL = "http://172.21.4.104/carfit/";
        }
        return BASE_URL;
    }
}
