package callerid.truecaller.trackingnumber.phonenumbertracker.block.bank_info;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.appsqlite.SqliteHelper;


public class SQLiteAdapterBank {
    private static final String MYDATABASE_NAME = "bank_checker";
    private Context context;
    private SQLiteDatabase sqLiteDatabase;
    private SqliteHelper sqLiteHelper;

    public SQLiteAdapterBank(Context c) {
        this.context = c;
    }

    public SQLiteAdapterBank openToRead() throws SQLException {
        this.sqLiteHelper = new SqliteHelper(this.context, MYDATABASE_NAME, null, 1);
        this.sqLiteDatabase = this.sqLiteHelper.getReadableDatabase();
        return this;
    }


    public void close() {
        this.sqLiteHelper.close();
    }

    public Cursor GetSearchResultByMobileno() throws SQLException {
        Cursor mCursor = null;
        try {
            this.sqLiteDatabase = this.sqLiteHelper.getReadableDatabase();
            mCursor = this.sqLiteDatabase.rawQuery("SELECT bank_name, bank_fav FROM tbl_bank", null);
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
        } catch (Exception e) {
        }
        return mCursor;
    }

    public Cursor Getbankdetals(String bankname) throws SQLException {
        Cursor mCursor = null;
        try {
            this.sqLiteDatabase = this.sqLiteHelper.getReadableDatabase();
            mCursor = this.sqLiteDatabase.rawQuery("SELECT * FROM tbl_bank WHERE bank_name=" + "='" + bankname + "'", null);
            if (mCursor != null) {
                mCursor.moveToFirst();
            }
        } catch (Exception e) {
        }
        return mCursor;
    }
}
