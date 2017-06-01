package com.drava.android.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AppLog {
    public static void print(Context context,String str) {
        createLogFile(context,str);
    }

    private static void createLogFile(Context context,String str) {
        if (context != null) {
            File root = new File(context.getExternalCacheDir(), "AppLog");
//        Log.d("File", "createLogFile: "+context.getExternalCacheDir());
            if (!root.exists()) {
                root.mkdirs();
            }
            File dravaLogFile = new File(root, "AppLog_" + getCurrentDate() + ".txt");
            try {
                if (!dravaLogFile.exists()) {
                    dravaLogFile.createNewFile();
                }
                FileOutputStream fOut = new FileOutputStream(dravaLogFile, true);
                OutputStreamWriter writer = new OutputStreamWriter(fOut);
                writer.append("\n").append(new SimpleDateFormat("HH mm").format(Calendar.getInstance().getTime())+"   "+str);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getCurrentDate() {
        String currentDateandTime = "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        currentDateandTime = dateFormat.format(cal.getTime());
        return currentDateandTime;
    }
}
