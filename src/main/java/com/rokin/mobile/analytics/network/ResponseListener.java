package com.rokin.mobile.analytics.network;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * @author Sourav
 */
public interface ResponseListener
{
	void serverResponse(@NonNull Response response, @IntRange(from = 0) int type);
}
