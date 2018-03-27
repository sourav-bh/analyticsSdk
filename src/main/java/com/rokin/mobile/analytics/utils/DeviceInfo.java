package com.rokin.mobile.analytics.utils;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class DeviceInfo {
    private BatteryChargeStatusListener batteryUpdate;
    public static final int DEVICE_LOW_RES = 1;
    public static final int DEVICE_MEDIUM_RES = 2;
    public static final int DEVICE_HIGH_RES = 3;
    public static final int DEVICE_VERY_HIGH_RES = 5;

    public static int threadCount = 5;
    public static int tileCacheSize = 10;
    public static int deviceProfile = DEVICE_MEDIUM_RES;
    private static int width;
    private static int height;
    private static String deviceIMEI = "";

    public static float LDPI_DEVICE_DENSITY = 0.75f;
    public static float MDPI_DEVICE_DENSITY = 1.0f;
    public static float HDPI_DEVICE_DENSITY = 1.5f;
    public static float XHDPI_DEVICE_DENSITY = 2.0f;
    public static float XXHDPI_DEVICE_DENSITY = 3.0f;

    public DeviceInfo() {
    }

    public static String getApplicationVersion(Context context) {
        try {
//            version = context.getPackageManager().getPackageInfo(context.getApplicationInfo().packageName, 0).versionName;
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getApplicationInfo().packageName, 0);
            return info.versionName;
        } catch (Exception e) {
            return "";
        }
    }

    public static String getDeviceGeolyticName() {
        String name = getDeviceBrand().trim() + " " + getDeviceModel().trim() + " " + getOSVersion().trim();
        name = name.replace(" ", "_");
        return name;
    }

    public static String getDeviceBrand() {
        String PhoneBrand = Build.BRAND;
        // Device brand
        return PhoneBrand;
    }

    public static String getDeviceModel() {
        // Device model
        String PhoneModel = Build.MODEL;
        return PhoneModel;
    }

    public static String getOSVersion() {
        // Android version
        String AndroidVersion = Build.VERSION.RELEASE;
        return AndroidVersion;
    }

    public static String getIMEINumber() {
        return deviceIMEI;
    }

    public MemoryInfo getMemoryInfo(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo memoryInfo = new MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        return memoryInfo;
    }

    public static boolean isGPSOn(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return true;

        return false;
    }

    public boolean isNetAvilable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni.isAvailable())
            return true;
        return false;
    }

    public void getBatteryState(Context context, BatteryChargeStatusListener bat) {
        this.batteryUpdate = bat;
        BroadcastReceiver battReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {

                context.unregisterReceiver(this);
                int rawlevel = intent.getIntExtra("level", -1);
                int scale = intent.getIntExtra("scale", -1);
                if (rawlevel >= 0 && scale > 0) {
                    //level = (rawlevel * 100) / scale;
                    //setBatteryLevel((rawlevel * 100) / scale);
                    batteryUpdate.onBatteryStatusChanged((rawlevel * 100) / scale);
                }
            }
        };
        IntentFilter battFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(battReceiver, battFilter);

    }

    public static boolean isSDKVersionSuppoted() {
        int required_version;
//		if (AGConfig.ENABLE_CUSTOM_LOCALE && AGConfig.CUSTOM_LOCALE_NAME.equalsIgnoreCase("af"))
        required_version = Build.VERSION_CODES.HONEYCOMB;
//		else
//			required_version = android.os.Build.VERSION_CODES.GINGERBREAD;
        if (Build.VERSION.SDK_INT >= required_version)
            return true;
        else
            return false;
    }

    public static boolean isSDKVersionSupported(int apiVersion) {
        if (Build.VERSION.SDK_INT >= apiVersion) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if the device has sdcard support else return false
     */
    public static boolean isSDCardAvailable() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    private String humanReadableSize(int blocks, int blockSize) {
        int sizeInKB = 0, sizeInMB = 0, sizeInGB = 0;

        int blockSizeKB = blockSize / 1024;
        if (blockSizeKB > 0) {
            sizeInKB = blocks * blockSizeKB;
        } else {
            int invBlockSizeKB = 1024 / blockSize;
            sizeInKB = blocks / invBlockSizeKB;
        }

        if (sizeInKB == 0) {
            return "0 KB";
        }

        sizeInMB = sizeInKB / 1024;
        if (sizeInMB == 0) {
            return sizeInKB + " KB";
        }

        sizeInGB = sizeInMB / 1024;
        if (sizeInGB == 0) {
            return sizeInMB + " MB";
        }

        return sizeInGB + " GB";
    }

    @RequiresPermission(allOf = Manifest.permission.READ_PHONE_STATE)
    public static void profileDevice(Context context) {
        FileLogger.getInstance().writeLog("INFO", "Device profiling started");
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String deviceIMEIStr = telephonyManager.getDeviceId();
        if (deviceIMEIStr != null)
            deviceIMEI = deviceIMEIStr;

        //http://stackoverflow.com/questions/13628389/android-how-to-get-the-real-screen-size-of-the-device
        final DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Method mGetRawH = null, mGetRawW = null;
        int realWidth = 240, realHeight = 320;

        try {
            // For JellyBeans and onward
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                display.getMetrics(metrics);
                realWidth = metrics.widthPixels;
                realHeight = metrics.heightPixels;
            } else if (Build.VERSION.SDK_INT >= 14) {
                // Below Jellybeans you can use reflection method
                mGetRawH = Display.class.getMethod("getRawHeight");
                mGetRawW = Display.class.getMethod("getRawWidth");

                try {
                    realWidth = (Integer) mGetRawW.invoke(display);
                    realHeight = (Integer) mGetRawH.invoke(display);
                } catch (IllegalArgumentException e) {
                    Logger.consolePrintStackTrace(e);
                } catch (IllegalAccessException e) {
                    Logger.consolePrintStackTrace(e);
                } catch (InvocationTargetException e) {
                    Logger.consolePrintStackTrace(e);
                }
            } else {
                display.getMetrics(metrics);
                realWidth = metrics.widthPixels;
                realHeight = metrics.heightPixels;
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        //http://stackoverflow.com/questions/7587854/is-there-a-list-of-screen-resolutions-for-all-android-based-phones-and-tablets
        int orientation = getScreenOrientation(realWidth, realHeight);
        if (realWidth <= 320 || realHeight <= 320) {
            threadCount = 5;
            tileCacheSize = 10;
            deviceProfile = DEVICE_LOW_RES;
        } else {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (realHeight > 1000) {
                    threadCount = 15;
                    tileCacheSize = 150;
                    if (metrics.density > 1.5) {
                        deviceProfile = DEVICE_VERY_HIGH_RES;
                    } else {
                        deviceProfile = DEVICE_HIGH_RES;
                    }
                } else if (realHeight > 800) {
                    threadCount = 15;
                    tileCacheSize = 100;
                    if (metrics.density > 1.5) {
                        deviceProfile = DEVICE_VERY_HIGH_RES;
                    } else {
                        deviceProfile = DEVICE_HIGH_RES;
                    }
                } else if (realHeight >= 800) {
                    threadCount = 10;
                    tileCacheSize = 50;
                    deviceProfile = DEVICE_MEDIUM_RES;
                }
            } else {
                if (realWidth > 700) {
                    threadCount = 15;
                    tileCacheSize = 150;
                    deviceProfile = DEVICE_VERY_HIGH_RES;
                } else if (realWidth > 400) {
                    threadCount = 15;
                    tileCacheSize = 100;
                    deviceProfile = DEVICE_HIGH_RES;
                } else if (realWidth >= 400) {
                    threadCount = 10;
                    tileCacheSize = 50;
                    deviceProfile = DEVICE_MEDIUM_RES;
                }
            }
        }
        width = realWidth;
        height = realHeight;
        FileLogger.getInstance().writeLog("INFO", "HEIGHT/WIDTH/ORIENTATION/THREADS/CACHE: " +
                realHeight + "/" + realWidth + "/" + orientation + "/" + threadCount + "/" + tileCacheSize);
        Logger.consolePrint("INFO", "HEIGHT/WIDTH/ORIENTATION/THREADS/CACHE: " +
                realHeight + "/" + realWidth + "/" + orientation + "/" + threadCount + "/" + tileCacheSize);
    }

    // To get the the relative width.
    // When in landscape mode, height will be with and vice versa
    public static int getRelativeWidth() {
        int orientation = getScreenOrientation(width, height);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            return height;
        else
            return width;
    }

    public static int getRelativeHeight() {
        int orientation = getScreenOrientation(width, height);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            return width;
        else
            return height;
    }

    // Actual width of the device
    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static int getScreenOrientation(int width, int height) {
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if (width == height) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (width < height)
                orientation = Configuration.ORIENTATION_PORTRAIT;
            else
                orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }

    public static double getZrpr() {
        if (DeviceInfo.deviceProfile == DeviceInfo.DEVICE_LOW_RES) {
            return -200.0;
        } else if (DeviceInfo.deviceProfile == DeviceInfo.DEVICE_MEDIUM_RES) {
            return -350.0;
        } else if (DeviceInfo.deviceProfile == DeviceInfo.DEVICE_HIGH_RES) {
            return -450.0;
        } else if (DeviceInfo.deviceProfile == DeviceInfo.DEVICE_VERY_HIGH_RES) {
            return -550.0;
        }
        return -350;
    }

    /**
     * @param ctxt
     * @return the density of the device such as LDPI_DEVICE_DENSITY...XXHDPI_DEVICE_DENSITY
     */
    public static float getDeviceDensity(Context ctxt) {
        float densityScale = ctxt.getResources().getDisplayMetrics().density;
        return densityScale;
    }


    /**
     * @param context
     * @return the density with DPI
     */
    public static String getDevieDensityWithDPI(Context context) {
        float densityScale = context.getResources().getDisplayMetrics().density;

        String densityWithDPI = "";
        if (densityScale <= .75) {
            densityWithDPI = "ldpi";
        } else if (densityScale <= 1) {
            densityWithDPI = "mdpi";
        } else if (densityScale <= 1.5) {
            densityWithDPI = "hdpi";
        } else if (densityScale <= 2) {
            densityWithDPI = "xhdpi";
        } else if (densityScale <= 3) {
            densityWithDPI = "xxhdpi";
        }
        return densityWithDPI;
    }


    public static int getGridImageThumbSize(Context context) {
        float densityScale = context.getResources().getDisplayMetrics().density;
        int gridImageSize = 0;

        if (!isTablet(context)) {
            if (densityScale <= .75) {
                gridImageSize = 38;
            } else if (densityScale <= 1) {
                gridImageSize = 64;
            } else if (densityScale <= 1.5) {
                gridImageSize = 118;
            } else if (densityScale <= 2) {
                gridImageSize = 167;
            } else if (densityScale <= 3) {
                gridImageSize = 250;
            }
        } else {
            gridImageSize = 100;
        }

        return gridImageSize;
    }


    public static int getPoiImageSize(Context context) {
        float densityScale = context.getResources().getDisplayMetrics().density;
        int gridImageSize = 0;

        if (!isTablet(context)) {
            if (densityScale <= .75) {
                gridImageSize = 24;
            } else if (densityScale <= 1) {
                gridImageSize = 36;
            } else if (densityScale <= 1.5) {
                gridImageSize = 50;
            } else if (densityScale <= 2) {
                gridImageSize = 75;
            } else if (densityScale <= 3) {
                gridImageSize = 100;
            }
        } else {
            gridImageSize = 50;
        }

        return gridImageSize;
    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }


    public static boolean isGooglePlayServicesAvailable(Context context) {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
//            GooglePlayServicesUtil.getErrorDialog(status, (Activity)context, 0).show();
            return false;
        }
    }

}
