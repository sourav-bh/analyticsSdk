package com.rokin.mobile.analytics.db;

import android.database.sqlite.SQLiteDatabase;

public interface TableHelper {

     void onCreate(SQLiteDatabase db);

     void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

     String getTableName();

     String getCreateScript(String tablename);
}
