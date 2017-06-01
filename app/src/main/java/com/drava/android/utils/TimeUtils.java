package com.drava.android.utils;


import android.content.Context;

import com.drava.android.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"; // "2013-09-18 09:41:25"

    public static String converToTimeForRecentActivity(Context context, long timeInMillis) {
        String s = "";
        long diff = System.currentTimeMillis() - timeInMillis;
        long secs = diff / 1000;
        long mins = secs / 60;

        if (mins < 60 && mins > 0) {
            //s = mins + " " + (mins == 1 ? "minute ago" : "minutes ago");
            s = context.getString(mins == 1 ? R.string.minute_ago : R.string.minutes_ago, mins);
        } else if (mins > 0) {
            int h = (int) (mins / 60);
            if (h >= 24) {
                int days = h / 24;

                if (days > 30) {
                    int months = days / 30;

                    if (months > 12) {
                        int years = months / 12;
                        //s = years + "" + (years == 1 ? "year ago" : "years ago");
                        s = context.getString(years == 1 ? R.string.year_ago : R.string.years_ago, years);
                    } else {
                        //s = months + "" + (months == 1 ? "month ago" : "months ago");
                        s = context.getString(months == 1 ? R.string.month_ago : R.string.months_ago, months);
                    }
                } else {
                    if (days > 7 && days < 30) {
                        int weeks = days / 7;
                        //s = weeks + " " + (weeks == 1 ? "week ago" : "weeks ago");
                        s = context.getString(weeks == 1 ? R.string.week_ago : R.string.weeks_ago, weeks);
                    } else {
                        //s = days + " " + (days == 1 ? "day ago" : "days ago");
                        s = context.getString(days == 1 ? R.string.day_ago : R.string.days_ago, days);
                    }
                }
            } else {
                //s = h + " " + (h == 1 ? "hour ago" : "hours ago");
                s = context.getString(h == 1 ? R.string.hour_ago : R.string.hours_ago, h);
            }
        } else {
            //s = secs + " " + (secs == 1 ? "second ago" : "seconds ago");
            if (secs < 5) {
                //s = "Just now";
                s = context.getString(R.string.just_now);
            } else {
                //s = "1 minute ago";
                s = context.getString(R.string.one_minute_ago);
            }
        }
        return s;
    }

    private static boolean isDayEqual(int dayToAdd, long millis) {
        Calendar c1 = Calendar.getInstance();
        c1.add(Calendar.DAY_OF_YEAR, dayToAdd);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(millis);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isToday(long millis) {
        return isDayEqual(0, millis);
    }

    public static boolean isYesterday(long millis) {
        return isDayEqual(-1, millis);
    }

    public static boolean isTomorrow(long millis) {
        return isDayEqual(1, millis);
    }

    public static String getDateForLanes(Context context, long time) {
        String text;
        if (isToday(time)) {
            text = context.getString(R.string.today);
        } else if (isTomorrow(time)) {
            text = context.getString(R.string.tomorrow);
        } else if (isYesterday(time)) {
            text = context.getString(R.string.yesterday);
        } else {
            //text = DateConversion.getTimeFromLong(time, "EEEE, MMMM d, yyyy"); //Tuesday, May 12, 2015
//            text = DateConversion.getFullDate(time);
        }
        return "";
    }

    public static String getDateForLanesDiaporama(Context context, long time) {
        String text;
        if (isToday(time)) {
            text = context.getString(R.string.today);
        } else if (isTomorrow(time)) {
            text = context.getString(R.string.tomorrow);
        } else if (isYesterday(time)) {
            text = context.getString(R.string.yesterday);
        } else {
            text = DateConversion.getTimeFromLong(time, context.getString(R.string.date_format_diaporama));
        }

        //return text + " " + context.getString(R.string.at) + " " + DateConversion.getTimeFromLong(time, "h:mm aa");
        return text + " " + context.getString(R.string.at) + " " + DateConversion.getTimeFromLong(time, context.getString(R.string.time_format));
    }

    public static String getDateForLanesDetail(Context context, long time) {
        String text;
        if (isToday(time)) {
            text = context.getString(R.string.today);
        } else if (isTomorrow(time)) {
            text = context.getString(R.string.tomorrow);
        } else if (isYesterday(time)) {
            text = context.getString(R.string.yesterday);
        } else {
            text = DateConversion.getTimeFromLong(time, "d MMMM yyyy");
        }
        //return text + " " + context.getString(R.string.at) + " " + DateConversion.getTimeFromLong(time, "h:mm aa");
        return text + " " + context.getString(R.string.at) + " " + DateConversion.getTimeFromLong(time, context.getString(R.string.time_format));
    }

    private static long getOffsetMillis(String offset) {
        long millis = 0;

        if (TextUtils.isEmpty(offset) || offset.length() < 2) {
            return millis;
        }

        int sign = 1;
        if (offset.substring(0, 1).equals("-")) {
            sign = -1;
        }
        String s = offset.replace("+", "").replace("-", "");
        int dotIndex = s.indexOf(".");
        int hours = Integer.parseInt(s.substring(0, dotIndex));
        int mins = Integer.parseInt(s.substring(dotIndex + 1));
        int minutes = sign * (hours * 60) + mins;
        millis = (minutes * 60 * 1000);
        return millis;
    }

    public static String getISOTime(Context context, String date) {
        //2015-09-09T10:00:09.38
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        long time = 0;
        try {
            Date d = dateFormat.parse(date);
            time = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String text = null;
        //long time = new DateTime(date).getMillis();
        if (isToday(time)) {
            text = context.getString(R.string.today);
        } else if (isTomorrow(time)) {
            text = context.getString(R.string.tomorrow);
        } else if (isYesterday(time)) {
            text = context.getString(R.string.yesterday);
        } else {
            //text = DateConversion.getTimeFromLong(time, "EEEE, MMMM d, yyyy");
//            text = DateConversion.getFullDate(time);
        }

        //text += " " + context.getString(R.string.at) + " " + DateConversion.getTimeFromLong(time, "h:mm aa");

        text += " " + context.getString(R.string.at) + " " + DateConversion.getTimeFromLong(time, context.getString(R.string.time_format));
        return text;

    }
}
