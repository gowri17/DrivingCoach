package com.drava.android.activity.map.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ScheduledService extends IntentService {

  public ScheduledService() {
    super("ScheduledService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Toast.makeText(this,"Called",Toast.LENGTH_LONG).show();
    for(int i=0;i<5;i++) {
      Log.e(getClass().getSimpleName(), "I ran!"+i);
      try {
        Thread.sleep(1000);
//        stopSelf();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      if(i==4){
        stopSelf();
      }

    }
  }
}