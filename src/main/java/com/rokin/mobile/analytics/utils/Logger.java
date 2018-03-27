package com.rokin.mobile.analytics.utils;

import android.util.Log;

import com.rokin.mobile.analytics.settings.AGConfig;


public class Logger
{
	public static void consolePrint(String tag, String info)
	{
		if (AGConfig.DEBUGGABLE)
			Log.d(tag, info);
	}
	
	public static void consolePrintInfo(String tag, String info)
	{
		if (AGConfig.DEBUGGABLE)
			Log.i(tag, info);
	}
	
	public static void consolePrintError(String tag, String err)
	{
		if(AGConfig.DEBUGGABLE)
		{
			Log.e(tag, err);
		}
	}
	
	public static void consolePrintStackTrace(Exception e)
	{
		if(AGConfig.DEBUGGABLE)
        {
            e.printStackTrace();
        }
	}
	
}
