package com.rokin.mobile.analytics.location;

import com.rokin.mobile.analytics.data.MLocation;

/**
 * @author Sourav
 */

public interface MLocationUpdateListener {
    void onLocationUpdated(MLocation locData, int requestType);

    void onLocationRequestTimedOut(boolean isTimeOut);

    void onLocationStatusChanged(int status);
}

