package callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.data.STD_Data;
import callerid.truecaller.trackingnumber.phonenumbertracker.block.number_location.data.State_Data;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class MySQLiteHelper extends SQLiteAssetHelper {
    public static final String CODES_CITY = "City_Name";
    public static final String CODES_CODE = "Code";
    public static final String CODES_ID = "_id";
    public static final String CODES_STATE_ID = "State_IDs";
    private static final String DATABASE_NAME = "myDB.db";
    private static final int DATABASE_VERSION = 1;
    public static final String STATES_ID = "_id";
    public static final String STATES_NAME = "State";
    public static final String TABLE_CODES = "Codes";
    public static final String TABLE_STATES = "States";
    private static final String TAG = "MSSS : DatabaseHelper";
    private String DB_PATH;
    private final Context myContext;
    private SQLiteDatabase myDataBase;

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.myContext = context;
        this.DB_PATH = "/data/data/" + this.myContext.getApplicationContext().getPackageName() + "/databases/";
        try {
            createDataBase();
        } catch (Exception e) {
        }
    }


    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (newVersion > oldVersion) {
            try {
                copyDataBase();
            } catch (IOException e) {
            }
        }
    }

    public void createDataBase() throws IOException {
        if (checkDataBase()) {
            openDataBase();
            return;
        }
        try {
            copyDataBase();
        } catch (IOException e) {
            throw new Error("Error createDataBase().");
        }
    }

    private boolean checkDataBase() {
        Boolean checkDB = false;
        try {
            checkDB = new File(this.DB_PATH + DATABASE_NAME).exists();
        } catch (SQLiteException e) {
        }
        return checkDB;
    }

    private void copyDataBase() throws IOException {
        try {
            InputStream myInput = this.myContext.getAssets().open(DATABASE_NAME);
            OutputStream myOutput = new FileOutputStream(this.DB_PATH + DATABASE_NAME);
            byte[] buffer = new byte[1024];
            while (true) {
                int length = myInput.read(buffer);
                if (length <= 0) {
                    myOutput.flush();
                    myOutput.close();
                    myInput.close();
                    return;
                }
                myOutput.write(buffer, 0, length);
            }
        } catch (IOException e) {
        }
    }

    public void openDataBase() throws SQLException, IOException {
        try {
            this.myDataBase = SQLiteDatabase.openDatabase(this.DB_PATH + DATABASE_NAME, null, 0);
        } catch (SQLiteException e) {
        }
    }

    public ArrayList<STD_Data> getSTDList(Context context) {
        ArrayList<STD_Data> list = new ArrayList();
        try {
            Cursor cursor = this.myDataBase.rawQuery("SELECT * FROM Codes", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    STD_Data data = new STD_Data();
                    data.setId(cursor.getInt(cursor.getColumnIndexOrThrow(STATES_ID)));
                    data.setState_id(cursor.getInt(cursor.getColumnIndexOrThrow(CODES_STATE_ID)));
                    data.setCity(cursor.getString(cursor.getColumnIndexOrThrow(CODES_CITY)));
                    data.setCode(cursor.getInt(cursor.getColumnIndexOrThrow(CODES_CODE)));
                    list.add(data);
                }
            }
            cursor.close();
            Collections.sort(list, new Comparator<STD_Data>() {
                public int compare(STD_Data emp1, STD_Data emp2) {
                    return emp1.getCity().compareToIgnoreCase(emp2.getCity());
                }
            });
        } catch (Exception e) {
        }
        return list;
    }

    public ArrayList<STD_Data> getSTDListForState(Context context, int State_ID) {
        ArrayList<STD_Data> list = new ArrayList();
        try {
            SQLiteDatabase sQLiteDatabase = this.myDataBase;
            String str = TABLE_CODES;
            String[] strArr = new String[]{STATES_ID, CODES_STATE_ID, CODES_CITY, CODES_CODE};
            String[] strArr2 = new String[DATABASE_VERSION];
            strArr2[0] = String.valueOf(State_ID);
            Cursor cursor = sQLiteDatabase.query(true, str, strArr, "State_IDs=?", strArr2, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    STD_Data data = new STD_Data();
                    data.setId(cursor.getInt(cursor.getColumnIndexOrThrow(STATES_ID)));
                    data.setState_id(cursor.getInt(cursor.getColumnIndexOrThrow(CODES_STATE_ID)));
                    data.setCity(cursor.getString(cursor.getColumnIndexOrThrow(CODES_CITY)));
                    data.setCode(cursor.getInt(cursor.getColumnIndexOrThrow(CODES_CODE)));
                    list.add(data);
                }
            }
            cursor.close();
            Collections.sort(list, new Comparator<STD_Data>() {
                public int compare(STD_Data emp1, STD_Data emp2) {
                    return emp1.getCity().compareToIgnoreCase(emp2.getCity());
                }
            });
        } catch (Exception e) {
        }
        return list;
    }

    public ArrayList<State_Data> getStateList(Context context) {
        ArrayList<State_Data> Statelist = new ArrayList();
        try {
            Cursor cursor = this.myDataBase.rawQuery("SELECT * FROM States", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    State_Data data = new State_Data();
                    data.setId(cursor.getInt(cursor.getColumnIndexOrThrow(STATES_ID)));
                    data.setState(cursor.getString(cursor.getColumnIndexOrThrow(STATES_NAME)));
                    Statelist.add(data);
                }
            }
            cursor.close();
            Collections.sort(Statelist, new Comparator<State_Data>() {
                public int compare(State_Data emp1, State_Data emp2) {
                    return emp1.getState().trim().toLowerCase().compareToIgnoreCase(emp2.getState().trim().toLowerCase());
                }
            });
        } catch (Exception e) {
        }
        return Statelist;
    }
}
