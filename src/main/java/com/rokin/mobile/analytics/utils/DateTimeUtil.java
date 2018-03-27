package com.rokin.mobile.analytics.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Sourav.
 */
public class DateTimeUtil
{
    /**
     * Get the current date & time corrected with UTC offset value.
     *
     * @param context is the Application context{@link android.content.Context}
     * @param hasMilliseconds is a boolean value to indicate that if output should contain the milliseconds or not
     *
     * @return a String of the corrected value of current date & time.
     */
    public static String getFormattedCurrentTime(Context context, boolean hasMilliseconds)
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long deviceCorrectedTime = calendar.getTimeInMillis();
        SimpleDateFormat df;
        if (hasMilliseconds) {
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
        else {
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        String deviceTime = df.format(new Date(deviceCorrectedTime));
        return deviceTime;
    }

    /**
     * Get the given date & time corrected with UTC offset value.
     *
     * @param dateTime is a long value for given date & time
     * @param hasMilliseconds is a boolean value to indicate that if output should contain the milliseconds or not
     *
     * @return a String of the corrected value of given date & time.
     */
    public static String getFormattedTime(long dateTime, boolean hasMilliseconds)
    {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(new Date(dateTime));

        long deviceCorrectedTime = calendar.getTimeInMillis();
        SimpleDateFormat df;
        if (hasMilliseconds) {
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
        else {
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        String deviceTime = df.format(new Date(deviceCorrectedTime));
        return deviceTime;
    }

    /**
     * this method used for at gps data uploading time and it passes gps time here as a dateTime
     * so don't use it anywhere. If needed use getFormattedTimeWithOffset(long dateTime)
     * @param dateTime
     * @return
     */
    public static String getFormattedTimeWithoutOffset(long dateTime, boolean hasMilliseconds)
    {
        SimpleDateFormat df;
        if (hasMilliseconds) {
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
        else {
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        String deviceTime = df.format(new Date(dateTime));
        return deviceTime;
    }
}
