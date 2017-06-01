package com.drava.android.fcm;



import android.content.Context;
import android.content.SharedPreferences;


public class NotificationPreference {

	private static final String PREFS_NAME = "notification_preference";
	private static final String DEVICE_TOKEN = "gcm_device_token";
	//	private static final String END_POINT_ARN = "endpoint_arn";
	private static final String REGISTERED = "registered";
	private static final String NOTIFICATION = "notification_status";
	private static final String MESSAGE_VISIBLE = "is_message_visible";
	private static final String NOTIFICATION_ID = "notification_id";

	private SharedPreferences mPrefrence;
	private SharedPreferences.Editor mEditor;

	public NotificationPreference(Context context){
		mPrefrence = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		mEditor = mPrefrence.edit();
	}

	public void register(String deviceToken){
		mEditor.putString(DEVICE_TOKEN, deviceToken);
		mEditor.putBoolean(REGISTERED, true);
		mEditor.commit();
	}

	public boolean isRegistered(){
		return mPrefrence.getBoolean(REGISTERED, false);
	}

	public String getDeviceToken(){
		return mPrefrence.getString(DEVICE_TOKEN, null);
	}

	public boolean isNotificationIsOn(){
		return mPrefrence.getBoolean(NOTIFICATION, true);
	}

	public void setNotificationStatus(boolean isEnabled){
		mEditor.putBoolean(NOTIFICATION, isEnabled);
		mEditor.commit();
	}

	public void setMessageVisible(boolean value){
		mEditor.putBoolean(MESSAGE_VISIBLE, value);
		mEditor.commit();
	}

	public boolean isMessagePageVisible(){
		return mPrefrence.getBoolean(MESSAGE_VISIBLE, false);
	}

	public void removeSimpleMessageDetail(){
		mEditor.putString("PushNotification", "");
		mEditor.commit();
	}

	public void setNotificationId(int id){
		mEditor.putInt(NOTIFICATION_ID, id);
		mEditor.commit();
	}

	public int getNotificationId(){
		return mPrefrence.getInt(NOTIFICATION_ID, 1);
	}

}
