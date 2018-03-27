package com.rokin.mobile.analytics.data;

import android.location.Location;


import java.io.Serializable;

import com.rokin.mobile.analytics.constant.AppConstant;
import com.rokin.mobile.analytics.utils.DateTimeUtil;
import com.rokin.mobile.analytics.utils.Utils;
import com.rokin.mobile.analytics.utils.telephony.CellInfo;


/**
 * This is the data structure class to represent a GPS location
 *
 * @author Sourav
 */

public class MLocation implements Serializable
{
    private static final long serialVersionUID = -326456351564604276L;

    private double accuracy;
    private double altitude;
    private float speed;
    private Location location;
    private float bearing;
    private String provider;
    private long time;
    private long deviceTime;

    /**
     * Constructor
     * @param location is a object of Location
     */
    public MLocation(Location location)
    {
        this.location = location;
        this.altitude = location.getAltitude();
        if (location.getSpeed() < 0) {
            this.speed = 0;
        }
        else {
            this.speed = location.getSpeed();
        }
        this.provider = location.getProvider();
        this.accuracy = location.getAccuracy();
        this.bearing = location.getBearing();
        this.time = location.getTime();
        this.deviceTime = System.currentTimeMillis();
    }

    /**
     * @return the location
     */

    public Location getLocation()
    {
        return this.location;
    }

    /**
     * @return the altitude
     */
    public double getAltitude()
    {
        return altitude;
    }

    /**
     * @return the speed
     */
    public float getSpeed()
    {
        return speed;
    }

    /**
     *
     * @return Accuracy
     */
    public double getAccuracy()
    {
        return accuracy;
    }

    /**
     * @return the bearing
     */
    public float getBearing()
    {
        return this.bearing;
    }

    /**
     * @return the time
     */
    public long getTime()
    {
        return this.time;
    }


    public long getDeviceTime() {
        return deviceTime;
    }

    public void setDeviceTime(long deviceTime) {
        this.deviceTime = deviceTime;
    }

    @Override
    public String toString()
    {
        String str = location.toString();
        return str;
    }

    public String getFormattedLogString(String appSessionId, CellInfo cellInfo, String networkType)
    {
        StringBuffer sb = new StringBuffer();

        sb.append(AppConstant.SEPARATOR);
        sb.append(appSessionId);

        sb.append(AppConstant.SEPARATOR);
        sb.append(Utils.roundDownNumber(location.getLatitude(), 6));

        sb.append(AppConstant.SEPARATOR);
        sb.append(Utils.roundDownNumber(location.getLongitude(), 6));

        sb.append(AppConstant.SEPARATOR);
        sb.append(getAltitude());

        sb.append(AppConstant.SEPARATOR);
        sb.append(getBearing());

        sb.append(AppConstant.SEPARATOR);
        sb.append(getSpeed() * 3.6); // speed obtained in m/s, convert to km/h

        sb.append(AppConstant.SEPARATOR);
        sb.append(DateTimeUtil.getFormattedTime(getDeviceTime(), true));

        sb.append(AppConstant.SEPARATOR);
        sb.append(cellInfo.getCellID());

        sb.append(AppConstant.SEPARATOR);
        sb.append(cellInfo.getMNC());

        sb.append(AppConstant.SEPARATOR);
        sb.append(cellInfo.getMCC());

        sb.append(AppConstant.SEPARATOR);
        sb.append(cellInfo.getSignalStrength());

        sb.append(AppConstant.SEPARATOR);
        sb.append(cellInfo.getLAC());

        sb.append(AppConstant.SEPARATOR);
        sb.append(networkType);

        sb.append(AppConstant.SEPARATOR);
        sb.append(getAccuracy());

        return sb.toString();
    }
}
