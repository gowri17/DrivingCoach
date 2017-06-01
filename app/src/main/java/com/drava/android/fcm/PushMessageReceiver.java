package com.drava.android.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.drava.android.R;
import com.drava.android.activity.HomeActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.drava.android.base.AppConstants.PUSH_MESSAGE;

public class PushMessageReceiver extends BroadcastReceiver {

    private static final int MAX_NOTIFICATION = 5;
    private static final String TAG = "PushMessageReceiver";
    private static int notificationId = 0;
    public static final String KEY_PUSH_MESSAGE = "push_message";
    private Intent intent;
    static PushNotification message;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && context != null) {
            message = parseMassage(intent);
            Log.e("GG", "PushMessageReceiver Reciever. Message Initial." + message);
//            postNotification(context, message, intent);
        }
    }

    protected void postNotification(Context context, final PushNotification pushNotification, Intent intent) {
        sendNotification(context, pushNotification);
    }

    private void sendNotification(Context context, PushNotification pushNotification){
        NotificationPreference notificationPreference = new NotificationPreference(context);
        int notificationId = notificationPreference.getNotificationId();
        Notification notification = createNotification(context, pushNotification, notificationId);
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);

        if(notificationId == 5)
            notificationId = 1;
        notificationPreference.setNotificationId(notificationId+1);
    }

    private Notification createNotification(Context context, PushNotification notification, int notificationId){
        String message = "";
        if(notification.type == 0){
            message = "Invite request to monitor the Trip";
        }else if(notification.type == 1){
            message = "Trip has been voilated by mentee";
        }else if(notification.type == 2){
            message = "GPS status has changed by mentee";
        }else if(notification.type == 3){
            message = "Mentee device has been switched off";
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(getNotificationIcon());
        builder.setAutoCancel(true);
        builder.setColor(ContextCompat.getColor(context, R.color.theme_color));
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(notification.getMessage());
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        builder.setContentIntent(getContentIntent(context, notification, notificationId));
        return builder.build();
    }

    public static void cancelAllNotification(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        for (int i = 0; i < MAX_NOTIFICATION; i++) {
            nm.cancel(i);
        }
    }

    private PushNotification parseMassage(Intent intent) {
        if (intent != null) {
            Bundle data = intent.getExtras();
            Iterator<String> it = data.keySet().iterator();
            String key;
            Object value;

            Map<String, Object> map = new HashMap<String, Object>();

            while (it.hasNext()) {
                key = it.next();
                value = data.get(key);
                Log.e(TAG, "key: " + key + " , Value: " + value);
                map.put(key, value);
            }

            JSONObject obj = new JSONObject(map);

            String msg = obj.optString("message");
            int type = obj.optInt("type");
            int userId = obj.optInt("userId");
            int productCount = obj.optInt("productCount");

            return new PushNotification(productCount, msg, userId, type);
        } else {
            return null;
        }
    }

    private int getNotificationIcon(){
        return R.mipmap.ic_launcher;
    }

    private PendingIntent getContentIntent(Context context, PushNotification notification, int previousId){
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(PUSH_MESSAGE, notification);
        return PendingIntent.getActivity(context, previousId+1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
    }
}




