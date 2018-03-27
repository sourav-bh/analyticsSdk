package com.rokin.mobile.analytics.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.rokin.mobile.analytics.settings.AGConfig;


/**
 * @author Sourav
 */
public class AnalyticsTableOpenHelper extends SQLiteOpenHelper {

    private static AnalyticsTableOpenHelper instance = null;

    private TableHelper[] tableHelpers = new TableHelper[]{
            new AnalyticsLogTableHelper()
    };


    private AnalyticsTableOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static AnalyticsTableOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AnalyticsTableOpenHelper(context, AGConfig.APPLICATION_NAME, null, AGConfig.DATABASE_VERSION);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (TableHelper table : tableHelpers) {
            table.onCreate(db);
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (TableHelper table : tableHelpers) {
            table.onUpgrade(db, oldVersion, newVersion);
        }
    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

    }

    /**
     * @return SQLiteDatabase, it will call getWritableDatabase at {@link SQLiteOpenHelper}
     */
    public SQLiteDatabase open() {
        SQLiteDatabase db = getWritableDatabase();
        return db;
    }


    /**
     * close any open data base
     */
    public void close() {
        super.close();
    }

}
