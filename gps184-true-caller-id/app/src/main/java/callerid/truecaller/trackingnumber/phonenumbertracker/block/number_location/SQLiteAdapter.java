package callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.appsqlite.SqliteHelper;


public class SQLiteAdapter {
    public static final String ISD_CODES_TABLE = "isdcodes";
    public static final String KEY_CITY = "city";
    public static final String KEY_COUNTRY = "country";
    public static final String KEY_ICON_VALUE = "iconval";
    public static final String KEY_ISD_CODE = "isdcode";
    public static final String KEY_ISD_CODES_ROW_ID = "rowid";
    public static final String KEY_LATITUDE = "lat";
    public static final String KEY_LONGITUDE = "lang";
    public static final String KEY_MOBILE_NO = "mobilenumber";
    public static final String KEY_OPERATOR_NAME = "operatorname";
    public static final String KEY_STATE_NAME = "statename";
    public static final String KEY_STD_CODE = "stdcode";
    public static final String KEY_STD_CODES_ROW_ID = "rowid";
    public static final String MOBILE_NUMBER_FINDER_TABLE = "mobileNumberfinder";
    public static final String MYDATABASE_NAME = "monolocator.db";
    public static final int MYDATABASE_VERSION = 1;
    public static final String STD_CODES_TABLE = "stdcodes";
    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    private SqliteHelper sqLiteHelper;

    public SQLiteAdapter(Context c) {
        this.context = c;
    }

    public SQLiteAdapter openToRead() {
        this.sqLiteHelper = new SqliteHelper(this.context, MYDATABASE_NAME, null, 1);
        this.sqLiteDatabase = this.sqLiteHelper.getReadableDatabase();
        return this;
    }


    public void close() {
        this.sqLiteHelper.close();
    }

    public Cursor GetSearchResultByMobileno(String mobileno) throws SQLException {
        Cursor mCursor = null;
        try {
            this.sqLiteDatabase = this.sqLiteHelper.getReadableDatabase();
            mCursor = this.sqLiteDatabase.rawQuery("SELECT * FROM mobileNumberfinder WHERE mobilenumber=" + mobileno, null);
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
        } catch (Exception e) {

        }
        return mCursor;
    }
}
