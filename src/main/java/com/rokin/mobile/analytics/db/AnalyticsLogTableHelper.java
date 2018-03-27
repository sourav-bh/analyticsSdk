package com.rokin.mobile.analytics.db;

import android.database.sqlite.SQLiteDatabase;

public class AnalyticsLogTableHelper implements TableHelper {
    private String createTableQuery = AnalyticsLogDBManager.AnalyticsLog.DATABASE_TABLE + "(pk_id integer primary key autoincrement, "
            + "log_type text, " + "log_value text, " + "app_session_id text, " + "nav_session_id text, "
            + "insertion_time BIGINT, " + AnalyticsLogDBManager.AnalyticsLog.COLUMN_UPLOADED + " integer); ";

    public void onCreate(SQLiteDatabase db) {
        String qry = "create table " + createTableQuery;
        db.execSQL(qry);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public String getTableName() {
        return null;
    }

    public String getCreateScript(String tablename) {
        return null;
    }

}
