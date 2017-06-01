/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drava.android.fcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.drava.android.R;
import com.drava.android.activity.HomeActivity;
import com.drava.android.utils.DravaLog;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.drava.android.base.AppConstants.PUSH_MESSAGE;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private JSONObject jsonObject;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            jsonObject = new JSONObject(remoteMessage.getData());
            DravaLog.print("Data:"+remoteMessage.getData().toString());
//            showToast("Data:"+remoteMessage.getData().toString());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            try {
                jsonObject = new JSONObject(remoteMessage.getNotification().getBody());
                DravaLog.print("Notify:"+remoteMessage.getNotification().getBody());
//                showToast("Notify:"+remoteMessage.getNotification().getBody());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(jsonObject != null){
            int productCount = jsonObject.optInt("productCount");
            int userId = jsonObject.optInt("userId");
            int type = jsonObject.optInt("type");
            String message = jsonObject.optString("message");
            PushNotification pushNotification = new PushNotification(productCount, message, userId, type);
            sendNotification(MyFirebaseMessagingService.this.getApplicationContext(), pushNotification);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */

    private void showToast(final String message) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MyFirebaseMessagingService.this.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(getNotificationIcon());
        builder.setAutoCancel(true);
        builder.setColor(ContextCompat.getColor(context, R.color.theme_color));
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(notification.getMessage());
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()));
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
        builder.setContentIntent(getContentIntent(context, notification, notificationId));
        return builder.build();
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
        return R.drawable.ic_notification;
    }

    private PendingIntent getContentIntent(Context context, PushNotification notification, int previousId){
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(PUSH_MESSAGE, notification);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, previousId+1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
    }
}
