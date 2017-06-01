package com.drava.android.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateConversion {

    public static Date stringToDate(String strDate, String parseFormat) {
        DateFormat formatter;
        Date date = null;
        formatter = new SimpleDateFormat(parseFormat, Locale.getDefault());
        try {
            date = (Date) formatter.parse(strDate);
        } catch (ParseException | IllegalArgumentException e) {
            date = new Date(strDate);
        }

        return date;
    }

    public static String calendarToStringwithslash(Calendar cal) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return df.format(cal.getTime());
    }

    public static Calendar getPreviousMonth(Calendar cal) {
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
        return cal;
    }

    public static Calendar getNextMonth(Calendar cal) {
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        return cal;
    }

    public static String getTimeFromLong(long time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        return sdf.format(new Date(time));
    }

    public static long getTimeFromString(String dateTime, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        try {
            Date date = sdf.parse(dateTime);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static String getTimeFromDate(Date date, String format) {
        return getTimeFromDate(date, format, TimeZone.getDefault());
    }

    public static String getTimeFromDate(Date date, String format, TimeZone timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        if (timeZone != null) {
            sdf.setTimeZone(timeZone);
        }
        return sdf.format(date);
    }

    public static long getCurrentTime(String timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        return sdf.getCalendar().getTimeInMillis();
    }

    public static long getLocalizedTime(String timezone, String time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        long millist = 0;
        try {
            Date date = sdf.parse(time);
            millist = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millist;
    }

    public static String formatDateTravelList(String gmt0Time) {
        long timeInLong = DateConversion.getLocalizedTime("GMT-0", gmt0Time, "yyyy-MM-dd HH:mm:ss"); /*2015-10-30 09:00:03*/
//        long timeInLocalGMT = getLocalizedTime("GMT-0", timeInLong);
        return getTimeFromLong(timeInLong, "HH'H'mm");
    }

    public static long getDateDiff(long time, long current) {
        long diff = Math.max(time, current) - Math.min(time, current);
        return diff / (1000 * 60 * 60 * 24);
    }

    public static long getLocalTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        long millis = 0l;
        try {
            Date date = sdf.parse(time);
            millis = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millis;
    }

    public static long getTimeMillisForApp(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT0"));
        long millis = 0l;
        try {
            Date date = sdf.parse(time);
            millis = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millis;
    }

    public static long getLocalizedTime(String timeZone, long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aaa", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
        String dateS = sdf.format(new Date(time)); // /1351330745
        return DateConversion.stringToDate(dateS, "dd-MM-yyyy hh:mm:ss aaa").getTime();
    }

    public static String convertCalendarToString(Calendar cal, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault());
        return df.format(cal.getTime());
    }

    public static long getAvailableTime(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        long millis = 0l;
        try {
            Date date = sdf.parse(time);
            millis = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millis;
    }

    public static String getLocalTimeFromGMT(String dateStr) {
        String formattedDate="";
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = df.parse(dateStr);
            df.setTimeZone(TimeZone.getDefault());
            formattedDate = df.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }

    public static String getCurrentDate() {
        String currentDateandTime = "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        currentDateandTime = dateFormat.format(cal.getTime());
        return currentDateandTime;
    }

    public static String getCurrentDateAndTime() {
        String currentDateandTime = "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        currentDateandTime = dateFormat.format(cal.getTime());
        return currentDateandTime;
    }

    public static String getCurrentDateAndTimeInGMT0() {
        String currentDateandTime = "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar cal = Calendar.getInstance();
        currentDateandTime = dateFormat.format(cal.getTime());
        return currentDateandTime;
    }

    public static long getTimeForGMT0(String time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        long millist = 0;
        try {
            Date date = sdf.parse(time);
            millist = date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return millist;
    }


    public static String formatDate(String date, String fromDateFormat,String toFormat) {
        Date tempDate = stringToDate(date, fromDateFormat);
        DateFormat dateFormat = new SimpleDateFormat(toFormat, Locale.getDefault());
        return dateFormat.format(tempDate);
    }

    public static long getMilliSecondFromStringForDate(String date,String format){
        long timeInMilliseconds = -1;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if(!TextUtils.isEmpty(date)){
            try {
                Date mDate = sdf.parse(date);
                timeInMilliseconds = mDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return timeInMilliseconds;
    }

    public static long getTimeinMillis(){
        return (Calendar.getInstance()).getTimeInMillis();
    }
}