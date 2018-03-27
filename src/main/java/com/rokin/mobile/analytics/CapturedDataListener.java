package com.rokin.mobile.analytics;

import com.rokin.mobile.analytics.data.MLocation;

/**
 * This interface is used get location data updates periodically
 *
 * Created by Sourav.
 */
public interface CapturedDataListener {
    void capturedData(MLocation geoLocation);
}
