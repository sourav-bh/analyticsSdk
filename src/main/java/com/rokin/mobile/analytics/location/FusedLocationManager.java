package com.rokin.mobile.analytics.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.OnSuccessListener;
import com.rokin.mobile.analytics.utils.Logger;

/**
 * Created by Sourav.
 */
public class FusedLocationManager extends LocationCallback {

    private static FusedLocationManager instance = null;
    private Context context;
    private LocationRequest mLocationRequest;
    private Location lastLocation = null;
    private FusedLocationProviderClient mFusedLocationClient;

    private FusedLocationManager(Context context) {
        this.context = context;
        try {
            createLocationRequest();
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        } catch (Exception e) {
            Logger.consolePrintStackTrace(e);
        }
    }

    /**
     * @param context is the application Context
     * @return instance of this class
     */
    public static FusedLocationManager getInstance(Context context) {
        if (instance == null) {
            instance = new FusedLocationManager(context);
        }
        return instance;
    }

    public void initialize() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        lastLocation = location;
                    }
                }
            });

            mFusedLocationClient.requestLocationUpdates(mLocationRequest, this, null);
        }
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        if (locationResult == null) {
            return;
        }
        for (Location location : locationResult.getLocations()) {
            lastLocation = location;
        }
    };

    public Location getLastLocation() {
        return lastLocation;
    }

}
