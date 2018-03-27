package com.rokin.mobile.analytics.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.List;

import com.rokin.mobile.analytics.data.MLocation;
import com.rokin.mobile.analytics.settings.AGConfig;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


/**
 * @author Sourav
 */

public class MLocationManager {
    private LocationManager locManager = null;
    //	private Activity callerActivity = null;
    private MLocationUpdateListener locChangedListener;
    private int preiodicalTime = 1000; // 1 sec
    private Handler handler = null;
    private Context context;
    private int requestType = -11;


    public MLocationManager(Context ctxt, MLocationUpdateListener listener, int requestType) {
        this.context = ctxt;
        this.locChangedListener = listener;
        this.requestType = requestType;
    }

    private Runnable runnable = new Runnable() {
        public void run() {
            if (locChangedListener != null) {
                locChangedListener.onLocationRequestTimedOut(true);
            }
            stopListeningForUpdate();
        }
    };

    @RequiresPermission(allOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    public void updateLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Location provider requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission");
        }
        this.updateLocation(AGConfig.GPS_TIMEOUT_INTERVAL);
    }

    private void updateLocation(int gpsTimeOut) {
        if (locManager != null)
            return;

        locManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            List<String> providers = locManager.getAllProviders();
            for (String provider : providers) {
                locManager.requestLocationUpdates(provider, 0, 0, locListener);
            }
        }

        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }

        if (handler == null) {
            handler = new Handler();
        }

        if (gpsTimeOut > 0) {
            handler.postDelayed(runnable, gpsTimeOut);
        }
    }

    public void removeListener() {
        if (locChangedListener != null)
            locChangedListener = null;
    }

    public void addListener(MLocationUpdateListener listener) {
        if (locChangedListener == null)
            this.locChangedListener = listener;
    }

    public void stopListeningForUpdate() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }


        if (locListener != null && locManager != null) {
            locManager.removeUpdates(locListener);
            locManager = null;
        } else {
            // Logger.consolePrint("Listener not initialized");
        }
    }

    // GPS
    private final LocationListener locListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            MLocation data = new MLocation(location);

            // Checking the GPS data validity in respect to the time stamp when the GPS data was fetched,
            // required to determine that if the GPS module send old data from cache.
//			if (location == null || (System.currentTimeMillis() - location.getTime()) > Config.GPSTIME_VALIDITY_THRESHOLD)
//			{
//				// Discard the data as it is null or more than 30 seconds older.
//				return;
//			}
//			else
//			{
//			}
            if (location == null)
                return;

            if (handler != null && runnable != null) {
                handler.removeCallbacks(runnable);
            }

            if (locChangedListener != null) {
                locChangedListener.onLocationUpdated(data, requestType);
            }
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            String stat = "";
            if (status == LocationProvider.AVAILABLE)
                stat = "AVAILABLE";
            else if (status == LocationProvider.OUT_OF_SERVICE)
                stat = "OUT_OF_SERVICE";
            else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
                stat = "TEMPORARILY_UNAVAILABLE";

            if (locChangedListener != null)
                locChangedListener.onLocationStatusChanged(status);
        }
    };

    @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    public Location getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Location provider requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission");
        }

        if (locManager == null) {
            locManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        List<String> providers = locManager.getProviders(false); //BestProvider(criteria, true);
        Location loc = null;
        for (String provider : providers) {
            Location location = locManager.getLastKnownLocation(provider);
                if (location != null) {
                    location.getTime();
                    loc = location;
                }
        }
        return loc;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    public void destroy() {
        stopListeningForUpdate();
        removeListener();
    }
}
