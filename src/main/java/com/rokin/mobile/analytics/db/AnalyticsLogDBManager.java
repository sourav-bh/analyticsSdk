package com.rokin.mobile.analytics.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.util.ArrayMap;

import java.util.ArrayList;

import com.rokin.mobile.analytics.utils.Logger;


/**
 * Contains all required methods to store, update, delete, fetch analytics logs into/from database.
 *
 * @author Sourav
 */
public class AnalyticsLogDBManager {
    private static AnalyticsLogDBManager instance = null;
    private AnalyticsTableOpenHelper openHelper = null;

    /**
     * Constant for request logs, i.e. anNavSession, anApplicationBug, anAppSession
     */
    public static final String LOG_TYPE_REQUEST_LOG = "RequestLog";

    /**
     * Constant for GPS data logs
     */
    public static final String LOG_TYPE_GPS_DATA_LOG = "GPSDataLog";

    private AnalyticsLogDBManager(Context context) {
        openHelper = AnalyticsTableOpenHelper.getInstance(context);
    }

    public static AnalyticsLogDBManager getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsLogDBManager(context);
        }
        return instance;
    }

    /**
     * Add {@link #LOG_TYPE_REQUEST_LOG} logs into database
     *
     * @param request        a String containing the request URL
     * @param app_session_id is the application session id for the running session
     */
    public synchronized void addRequestLog(String request, String app_session_id, boolean uploadable) {
        SQLiteDatabase db = openHelper.open();
        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(AnalyticsLog.COLUMN_LOG_TYPE, LOG_TYPE_REQUEST_LOG);
            initialValues.put(AnalyticsLog.COLUMN_LOG_VALUE, request);
            initialValues.put(AnalyticsLog.COLUMN_APP_SESSION_ID, app_session_id);
            initialValues.put(AnalyticsLog.COLUMN_INSERTION_TIME, System.currentTimeMillis());
            initialValues.put(AnalyticsLog.COLUMN_UPLOADED, uploadable ? 1 : 0);
            db.insert(AnalyticsLog.DATABASE_TABLE, null, initialValues);
        } catch (Exception e) {
            Logger.consolePrintStackTrace(e);
        } finally {
            openHelper.close();
        }
    }

    /**
     * Add {@link #LOG_TYPE_GPS_DATA_LOG} logs into database
     *
     * @param gpsDataString  a String containing the GPS data values
     * @param app_session_id is the application session id for the running session
     */
    public synchronized void addGPSLog(String gpsDataString, String app_session_id, boolean uploadable) {
        SQLiteDatabase db = openHelper.open();
        Cursor cursorData = null;
        try {
            cursorData = db.query(AnalyticsLog.DATABASE_TABLE, AnalyticsLog.ALL_COLUMNS, AnalyticsLog.COLUMN_LOG_VALUE + "='" + gpsDataString + "'", null, null, null, null);
            if (cursorData.getCount() <= 0) {
                ContentValues initialValues = new ContentValues();
                initialValues.put(AnalyticsLog.COLUMN_LOG_TYPE, LOG_TYPE_GPS_DATA_LOG);
                initialValues.put(AnalyticsLog.COLUMN_LOG_VALUE, gpsDataString);
                initialValues.put(AnalyticsLog.COLUMN_APP_SESSION_ID, app_session_id);
                initialValues.put(AnalyticsLog.COLUMN_INSERTION_TIME, System.currentTimeMillis());
                initialValues.put(AnalyticsLog.COLUMN_UPLOADED, uploadable ? 1 : 0);

                db.insert(AnalyticsLog.DATABASE_TABLE, null, initialValues);
            }
        } catch (Exception e) {
        } finally {
            openHelper.close();
        }

        Logger.consolePrintInfo(getClass().getSimpleName(), "an logLocationData data added into Database >>>>>>" + gpsDataString);
    }

    /**
     * Check for is there any pending analytics log into database
     *
     * @return true if any logs pending otherwise false
     */
    public synchronized boolean isLogRemainingForSend() {
        int count = 0;
        SQLiteDatabase db = openHelper.open();
        Cursor cursorData = null;
        try {
            cursorData = db.query(AnalyticsLog.DATABASE_TABLE, AnalyticsLog.ALL_COLUMNS, null, null, null, null, null);
            count = cursorData.getCount();
        } catch (Exception e) {
            Logger.consolePrintStackTrace(e);
        } finally {
            if (cursorData != null) {
                cursorData.close();
            }
            openHelper.close();
        }
        return count > 0;
    }

    /**
     * Get 10 topmost pending analytics logs for uploading
     *
     * @return a {@link java.util.ArrayList} containing the pending analytics logs data
     */
    public synchronized ArrayList<ArrayMap<String, String>> getRemainingLogsToUpload() {
        ArrayList<ArrayMap<String, String>> logList = new ArrayList<ArrayMap<String, String>>();
        SQLiteDatabase db = openHelper.open();
        Cursor cursorData = null;// openHelper.getDataForUpload();
        try {
            cursorData = db.query(AnalyticsLog.DATABASE_TABLE, AnalyticsLog.ALL_COLUMNS, null, null, null, null, AnalyticsLog.COLUMN_ID + " LIMIT 10");

            if (cursorData != null) {
                db.query(AnalyticsLog.DATABASE_TABLE, AnalyticsLog.ALL_COLUMNS, null, null, null, null, AnalyticsLog.COLUMN_ID + " LIMIT 10");
                // Get last 10 data for uploading, parse it from cursor
                // and then send it in a structured way i.e arraylist
                if (cursorData.moveToFirst()) {
                    do {
                        int id = cursorData.getInt(0);
                        String logType = cursorData.getString(1);
                        String logValue = cursorData.getString(2);
                        String appSesstionID = cursorData.getString(3);
                        String navSesstionID = cursorData.getString(4);
                        int uploadable = cursorData.getInt(6);

                        if (uploadable == 0) {
                            break;
                        }

                        ArrayMap<String, String> map = new ArrayMap<String, String>();
                        map.put(AnalyticsLog.COLUMN_ID, "" + id);
                        map.put(AnalyticsLog.COLUMN_LOG_TYPE, logType);
                        map.put(AnalyticsLog.COLUMN_LOG_VALUE, logValue);
                        map.put(AnalyticsLog.COLUMN_APP_SESSION_ID, appSesstionID);
                        map.put(AnalyticsLog.COLUMN_NAV_SESSION_ID, navSesstionID);

                        logList.add(map);

                    } while (cursorData.moveToNext());
                }
            }

        } catch (Exception e) {

        } finally {
            if (cursorData != null) {
                cursorData.close();
            }
            openHelper.close();
        }

        return logList;
    }

    /**
     * Remove the uploaded analytics log from database
     *
     * @param logType a String indicates the type of analytics log
     * @param data    the value to delete
     * @return true if delete is successful otherwise false
     */
    public synchronized boolean removeLog(String logType, String data) {
        SQLiteDatabase db = openHelper.open();
        try {
            if (logType.equalsIgnoreCase(AnalyticsLogDBManager.LOG_TYPE_GPS_DATA_LOG))
                return db.delete(AnalyticsLog.DATABASE_TABLE, AnalyticsLog.COLUMN_ID + " IN " + "(" + data + ") AND " + AnalyticsLog.COLUMN_LOG_TYPE + "='" + logType + "'", null) > 0;
            else
                return db.delete(AnalyticsLog.DATABASE_TABLE, AnalyticsLog.COLUMN_LOG_VALUE + "='" + data + "' AND " + AnalyticsLog.COLUMN_LOG_TYPE + "='" + logType + "'", null) > 0;
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            openHelper.close();
        }
        return true;
    }


    /**
     * @param values which need to compare with db
     * @return true if db contains this values
     */
    public boolean contains(String values) {
        SQLiteDatabase db = openHelper.open();

        Cursor cursor = null;
        try {

            cursor = db.query(AnalyticsLog.DATABASE_TABLE, AnalyticsLog.ALL_COLUMNS, null, null, null, null, AnalyticsLog.COLUMN_LOG_VALUE + "='" + values + "'");


            if (cursor != null && cursor.getCount() > 0)
                return true;
        } catch (Exception e) {
            Logger.consolePrintStackTrace(e);

        } finally {
            if (cursor != null) {
                cursor.close();
            }
            openHelper.close();
        }

        return false;
    }

    public void setUploadable(String sessiionId, String appSessionId, String timeStamp, String timeStampWithmilis, boolean uploadable) {
        /// at this method we need to make all rows uploadable under this sessionId
        ArrayList<ArrayMap<String, String>> arrayMaps = new ArrayList<ArrayMap<String, String>>();
        SQLiteDatabase db = openHelper.open();

        Cursor cursorDataWithSession = null;
        try {
            cursorDataWithSession = db.query(AnalyticsLog.DATABASE_TABLE, AnalyticsLog.ALL_COLUMNS, AnalyticsLog.COLUMN_INSERTION_TIME + "=" + Long.parseLong(sessiionId), null, null, null, null);
            if (cursorDataWithSession != null && cursorDataWithSession.moveToFirst()) {
                do {
                    int id = cursorDataWithSession.getInt(0);
                    String logType = cursorDataWithSession.getString(1);
                    String logValue = cursorDataWithSession.getString(2);

                    ArrayMap<String, String> arrayMap = new ArrayMap<String, String>();
                    arrayMap.put("id", "" + id);
                    arrayMap.put("values", logValue);
                    arrayMaps.add(arrayMap);

                } while (cursorDataWithSession.moveToNext());

                for (int i = 0; i < arrayMaps.size(); i++) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(AnalyticsLog.COLUMN_LOG_VALUE, arrayMaps.get(i).get("values"));
                    contentValues.put(AnalyticsLog.COLUMN_APP_SESSION_ID, appSessionId);
                    contentValues.put(AnalyticsLog.COLUMN_UPLOADED, uploadable ? 1 : 0);
                    db.update(AnalyticsLog.DATABASE_TABLE, contentValues, AnalyticsLog.COLUMN_ID + "=" + Integer.parseInt(arrayMaps.get(i).get("id")), null);
                }
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(AnalyticsLog.COLUMN_APP_SESSION_ID, appSessionId);
                contentValues.put(AnalyticsLog.COLUMN_UPLOADED, 1);
                db.update(AnalyticsLog.DATABASE_TABLE, contentValues, AnalyticsLog.COLUMN_INSERTION_TIME + "=" + Long.parseLong(sessiionId), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (cursorDataWithSession != null) {
                cursorDataWithSession.close();
            }
            openHelper.close();
        }
    }

    public void makePreviousUploadable() {
        // at this method we need to change all db row as uploadable true
        SQLiteDatabase db = openHelper.open();
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(AnalyticsLog.COLUMN_UPLOADED, 1);
            db.update(AnalyticsLog.DATABASE_TABLE, contentValues, null, null);
        } catch (Exception e) {
            Logger.consolePrintStackTrace(e);
        } finally {
            openHelper.close();
        }
    }

    public static final class AnalyticsLog {
        public static final String DATABASE_TABLE = "RokinAnalytics";

        public static final String COLUMN_ID = "pk_id";
        public static final String COLUMN_LOG_TYPE = "log_type";
        public static final String COLUMN_LOG_VALUE = "log_value";
        public static final String COLUMN_APP_SESSION_ID = "app_session_id";
        public static final String COLUMN_NAV_SESSION_ID = "nav_session_id";
        public static final String COLUMN_INSERTION_TIME = "insertion_time";
        public static final String COLUMN_UPLOADED = "uploadable";

        public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_LOG_TYPE, COLUMN_LOG_VALUE, COLUMN_APP_SESSION_ID, COLUMN_NAV_SESSION_ID, COLUMN_INSERTION_TIME, COLUMN_UPLOADED};
    }
}
