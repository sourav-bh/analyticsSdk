/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rokin.mobile.analytics.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.text.format.DateFormat;


import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import com.rokin.mobile.analytics.settings.AGConfig;


/**
 * @author Sourav
 */
public class Utils {
    /**
     *
     */
    public static final double PIover2 = Math.PI / 2;
    /**
     * Constant for PI divided by 3.
     */
    public static final double PIover3 = Math.PI / 3;
    /**
     * Constant for PI divided by 4.
     */
    public static final double PIover4 = Math.PI / 4;
    /**
     * Constant for PI divided by 6.
     */
    public static final double PIover6 = Math.PI / 6;
    /**
     * Constant for PI divided by 12.
     */
    public static final double PIover12 = Math.PI / 12;
    /**
     * Constant used in the <code>atan</code> calculation.
     */
    private static final double ATAN_CONSTANT = 1.732050807569;

    public static String getCurrentTimeAndDate() {
        DateFormat df = new DateFormat();
        String currentTime = df.format("yyyy-MM-dd hh:mm:ss",
                new Date()).toString();
        return currentTime;
    }

    /**
     * Convert longitude to display
     *
     * @param longitude
     * @return
     */
    public static String convertToDisplayLongitude(double longitude) {
        StringBuffer sb = new StringBuffer();

        int lon = (int) Math.round(longitude * 10000);

        if (lon <= 0) {
            sb.append('W');
            lon = -lon;
        } else {
            sb.append('E');
        }

        if (lon < 10000) {
            sb.append('0');
        }

        sb.append(lon);

        if (sb.length() >= 4)
            sb.insert(sb.length() - 4, '.');

        return sb.toString();
    }

    public static String convertToDisplayLatitude(double latitude) {
        StringBuffer sb = new StringBuffer();

        int lat = (int) Math.round(latitude * 10000);

        if (lat < 0) {
            sb.append('S');
            lat = -lat;
        } else {
            sb.append('n');
        }

        if (lat < 10000) {
            sb.append('0');
        }

        sb.append(lat);

        if (sb.length() >= 4)
            sb.insert(sb.length() - 4, '.');

        return sb.toString();
    }

    public static double getHaversineDistance(Location from, Location to) // In KM
    {
        double R = 6371; // km
        double dLat = Math.toRadians(to.getLatitude() - from.getLatitude());
        double dLon = Math.toRadians(to.getLongitude() - from.getLongitude());
        double a = Math.sin(dLat / 2.0) * Math.sin(dLat / 2.0)
                + Math.cos(Math.toRadians(from.getLatitude()))
                * Math.cos(Math.toRadians(to.getLatitude()))
                * Math.sin(dLon / 2.0) * Math.sin(dLon / 2.0);
        double c = 2.0 * atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        double dis = R * c;

        return dis;
    }

    public static double atan(double a) {
        // Special cases.
        if (Double.isNaN(a)) {
            return Double.NaN;
        }

        if (a == 0.0) {
            return a;
        }

        // Compute the arc tangent.
        boolean negative = false;
        boolean greaterThanOne = false;
        int i = 0;

        if (a < 0.0) {
            a = -a;
            negative = true;
        }

        if (a > 1.0) {
            a = 1.0 / a;
            greaterThanOne = true;
        }

        double t;

        for (; a > PIover12; a *= t) {
            i++;
            t = a + ATAN_CONSTANT;
            t = 1.0 / t;
            a *= ATAN_CONSTANT;
            a--;
        }

        double aSquared = a * a;

        double arcTangent = aSquared + 1.4087812;
        arcTangent = 0.55913709 / arcTangent;
        arcTangent += 0.60310578999999997;
        arcTangent -= 0.051604539999999997 * aSquared;
        arcTangent *= a;

        for (; i > 0; i--) {
            arcTangent += PIover6;
        }

        if (greaterThanOne) {
            arcTangent = PIover2 - arcTangent;
        }

        if (negative) {
            arcTangent = -arcTangent;
        }

        return arcTangent;
    }


    /**
     * @param time need to be format
     * @return given time to a formatted time
     */
    public static String getFormattedTimeWithFormat(long time)//time should be in seconds
    {
        int hr = 0, min = 0, sec = 0;
        String totalTime = "";
        if (time >= 3600) {
            hr = (int) (time / 3600);
            time = time % 3600;
        }
        totalTime += hr + ":";
        if (time >= 60) {
            min = (int) (time / 60);
            time = time % 60;
        }

        if (min < 10) {
            totalTime += "0" + min + ":";
        } else {
            totalTime += min + ":";
        }

        sec = (int) time;
        int second = (int) sec;

        if (second < 10) {
            totalTime += "0" + second;
        } else {
            totalTime += second;
        }

        return totalTime;
    }

    /**
     * @param y - the ordinate coordinate
     * @param x - the abscissa coordinate
     * @return the <i>theta</i> component of the point (r, <i>theta</i>) in
     * polar coordinates that corresponds to the point (x, y) in
     * Cartesian coordinates.
     */
    public static double atan2(double y, double x) {
        // Special cases.
        if (Double.isNaN(y) || Double.isNaN(x)) {
            return Double.NaN;
        } else if (Double.isInfinite(y)) {
            if (y > 0.0) // Positive infinity
            {
                if (Double.isInfinite(x)) {
                    if (x > 0.0) {
                        return PIover4;
                    } else {
                        return 3.0 * PIover4;
                    }
                } else if (x != 0.0) {
                    return PIover2;
                }
            } else // Negative infinity
            {
                if (Double.isInfinite(x)) {
                    if (x > 0.0) {
                        return -PIover4;
                    } else {
                        return -3.0 * PIover4;
                    }
                } else if (x != 0.0) {
                    return -PIover2;
                }
            }
        } else if (y == 0.0) {
            if (x > 0.0) {
                return y;
            } else if (x < 0.0) {
                return Math.PI;
            }
        } else if (Double.isInfinite(x)) {
            if (x > 0.0) // Positive infinity
            {
                if (y > 0.0) {
                    return 0.0;
                } else if (y < 0.0) {
                    return -0.0;
                }
            } else // Negative infinity
            {
                if (y > 0.0) {
                    return Math.PI;
                } else if (y < 0.0) {
                    return -Math.PI;
                }
            }
        } else if (x == 0.0) {
            if (y > 0.0) {
                return PIover2;
            } else if (y < 0.0) {
                return -PIover2;
            }
        }

        // Implementation a simple version ported from a PASCAL implementation:
        // http://everything2.com/index.pl?node_id=1008481

        double arcTangent;

        // Use arctan() avoiding division by zero.
        if (Math.abs(x) > Math.abs(y)) {
            arcTangent = atan(y / x);
        } else {
            arcTangent = atan(x / y); // -PI/4 <= a <= PI/4

            if (arcTangent < 0) {
                arcTangent = -PIover2 - arcTangent; // a is negative, so we're
                // adding
            } else {
                arcTangent = PIover2 - arcTangent;
            }
        }

        // Adjust result to be from [-PI, PI]
        if (x < 0) {
            if (y < 0) {
                arcTangent = arcTangent - Math.PI;
            } else {
                arcTangent = arcTangent + Math.PI;
            }
        }

        return arcTangent;
    }

    /**
     * @param context
     * @param data    which needed to save
     * @param key
     */
    public static void setToSharedPreference(Context context, String data, String key) {
        SharedPreferences preferences = context.getSharedPreferences(AGConfig.APPLICATION_NAME, Context.MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(key, data);
        editor.commit();
    }

    /**
     * @param context
     * @param key
     * @return stored value at this key, if no values were saved then it will return null
     */
    public static String getFromSharedPreference(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(AGConfig.APPLICATION_NAME, Context.MODE_PRIVATE);
        String data = preferences.getString(key, null);
        return data;
    }

    public static String[] splitString(String string, String delimiters) {
        StringTokenizer st = new StringTokenizer(string, delimiters);
        Vector<String> v = new Vector<String>(0, 1);

        while (st.hasMoreTokens()) {
            v.add(st.nextToken());
        }

        String returnStrings[] = new String[v.size()];
        v.copyInto(returnStrings);

        return returnStrings;
    }

    public static String roundDownNumber(double f, int roundto) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.US);
        formatter.setMaximumFractionDigits(2);
        if (roundto <= 0) {
            return "" + Math.round(f);
        } else if (roundto <= 6) {
            formatter.setMaximumFractionDigits(roundto);
        }
        String str = formatter.format(f); //String.format("%." + roundto + "f", f);
        return str;
    }

    /**
     * Convert the distance value into human readable format by appending km/m.
     *
     * @param distance in meter
     * @return distance value in a formatted string.
     */
    public static String[] getDistanceWithUnitArr(double distance) {
        String[] arrVal = new String[2];
        if (distance >= 1000000) {        //distance greater than 1000KM
            arrVal[0] = "" + ((int) (distance / 1000));
            arrVal[1] = "  km";
            return arrVal;
        } else if (distance >= 10000) {       //greater then 10 km and less then 1000 km
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            formatter.setMaximumFractionDigits(1);
            arrVal[0] = "" + formatter.format(distance / 1000);//String.format("%.1f", distance/ 1000f);
            arrVal[1] = "  km";
            return arrVal;
        } else if (distance >= 1000) {       //greater then 1 km and less then 10 km
            NumberFormat formatter = NumberFormat.getInstance(Locale.US);
            formatter.setMaximumFractionDigits(2);
            arrVal[0] = "" + formatter.format(distance / 1000);//String.format("%.2f", distance/ 1000f);
            arrVal[1] = "  km";
            return arrVal;
        }
        arrVal[0] = "" + Math.round(distance);
        arrVal[1] = "  m";
        return arrVal;
    }

    public static String getURLEncodedString(Hashtable<String, String> params) {
        StringBuffer url = new StringBuffer();
        try {
            if (params != null) {
                Enumeration<String> elements = params.keys();
                int i = 0;
                while (elements.hasMoreElements()) {
                    String key = (String) elements.nextElement();
                    String value = (String) params.get(key);

                    if (i > 0) {
                        url.append("&");
                    }
                    url.append(key);
                    url.append("=");
                    url.append(URLEncoder.encode(value, "UTF-8"));
                    i++;
                }
            }
        } catch (Exception e) {
            Logger.consolePrintStackTrace(e);
        }
        return url.toString();
    }

    public static boolean isEmptyString(String value) {
        if (value == null || value.equalsIgnoreCase("")) {
            return true;
        }
        return false;

    }
}
