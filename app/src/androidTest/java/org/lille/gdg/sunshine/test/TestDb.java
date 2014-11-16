package org.lille.gdg.sunshine.test;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import org.lille.gdg.sunshine.data.WeatherDbHelper;

public class TestDb extends AndroidTestCase {

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

}
