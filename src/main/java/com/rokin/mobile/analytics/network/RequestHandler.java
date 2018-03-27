package com.rokin.mobile.analytics.network;

import android.support.annotation.NonNull;

/**
 * Created by Sourav
 */

public interface RequestHandler {
    void onServerRequest(@NonNull Request request);
}
