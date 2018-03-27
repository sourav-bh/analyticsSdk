package com.rokin.mobile.analytics.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rokin.mobile.analytics.AnalyticsManager;

/**
 * 
 * @author Sourav
 * 
 */
public class FileLogger
{
    private static FileLogger instance;

	private BufferedWriter out;
	private boolean isLogFileCreated = false;

	private FileLogger()
	{

	}

    /**
     * @return instance of this Class
     */
	public static FileLogger getInstance()
	{
		if (instance == null)
		{
			instance = new FileLogger();
		}
		
		return instance;
	}

    /**
     * this method will create log file at sd card according to Application Name
     */
	public void createLogFile()
	{
		try 
		{
		    File root = Environment.getExternalStorageDirectory();
		    if (root.canWrite())
		    {
		        File gpxFile = new File(root, getFileName());
		        FileWriter gpxWriter = new FileWriter(gpxFile);
		        out = new BufferedWriter(gpxWriter);
		        out.write(AnalyticsManager.getInstance().getConfiguration().getApplicationName() + ", LOGGER STARTED");
		        out.flush();
                isLogFileCreated = true;
			}
		}
		catch (Exception e) 
		{
			Logger.consolePrint("ErrorLog", "Could not write file " + e.getMessage());
		}
	}

    /**
     * method writes log files to sd card
     * @param category is log category
     * @param logText is log text which needs to be write
     */
	public synchronized void writeLog(String category, String logText)
	{
		try
		{
			if (out != null && isLogFileCreated)
			{
				Logger.consolePrint("ConsoleLog", "LOGGER: " + logText);
				out.newLine();
				out.write(category + ": [" + getCurrentDateNTime()+ "] - " + logText);
				out.flush();
			}
		}
		catch(Exception e)
		{
			Logger.consolePrint("ErrorLog", e.toString() + " in " + getClass().getName());
		}
	}
	
	public String getCurrentDateNTime()
	{
		SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		String format = s.format(new Date());
		return format;
	}
	
	
	public String getFileName()
	{
		SimpleDateFormat s = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		String format = s.format(new Date());
		return AnalyticsManager.getInstance().getConfiguration().getApplicationName() + "_" + format + ".txt";
	}
	
	public void destroy()
	{
		try
		{
			if (out != null)
			{
				writeLog("INFO", "LOGGER CLOSED");
				out.close();
			}
		}
		catch (Exception e)
		{
			Logger.consolePrint("ErrorLog", e.toString() + " in " + getClass().getName());
		}

        isLogFileCreated = false;
		instance = null;
	}
}
