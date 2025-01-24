package macro.hd.wallpapers.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBase {

    private DatabaseHelper dbHelper;

    private SQLiteDatabase sqLiteDb;

    private Context HCtx = null;

    private static final String DATABASE_NAME = "Wallpaper";
    private static final int DATABASE_VERSION = 1;

    public static final String table_download = "tbl_download";
    public static final int Int_download = 0;
//message.getPostId(), message.getType(), message.getCategory(),  message.getImg(),message.getProgress(),message.getDownload_id()

//    {"post_id":"429","type":"video","category":"New","title":"Tiger ","video":"video\/1536659828575_VIDEO.mp4","text":"",
// "img":"image\/1536659832579_THUMBNAIL.png", "width":"1270","height":"720","view":"1","like":"0",
//            "download":"0","status":"1","created":"4 day ago","c":"1"}
    public static String[][] tables = new String[][]{
            {"sr_no", "id", "type", "category", "img", "progress","download_id"}
    };

    private static final String TABLE_1_CREATE = "create table IF NOT EXISTS "
            + table_download + " (sr_no integer primary key autoincrement,"
            + "id text,type text,category text,img text,progress text,download_id text);";

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context ctx) {
            super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TABLE_1_CREATE);


        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//			db.execSQL(TABLE_1_CREATE);
        }
    }

    /**
     * Constructor
     */
    public DataBase(Context ctx) {
        HCtx = ctx;
    }

    synchronized public DataBase open() throws SQLException {
        dbHelper = new DatabaseHelper(HCtx);
        sqLiteDb = dbHelper.getWritableDatabase();
        return this;
    }


    public void close() {
        dbHelper.close();
    }

    public synchronized long createAlert(String DATABASE_TABLE, int tableNo,
                                         String[] values) {
        ContentValues vals = new ContentValues();
        for (int i = 0; i < values.length; i++) {
            vals.put(tables[tableNo][i + 1], values[i]);
        }
        return sqLiteDb.insert(DATABASE_TABLE, null, vals);
    }



    public synchronized Cursor fetchAlert(String DATABASE_TABLE, int tableNo,
                                          String where,String orderby) throws SQLException {
        Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo], where,
                null, null, null, orderby);
        return ret;
    }

    public boolean updateAlert(String DATABASE_TABLE, int tableNo, long rowId,
                               String[] values) {
        ContentValues vals = new ContentValues();

        for (int i = 0; i < values.length; i++) {
            vals.put(tables[tableNo][i + 1], values[i]);
        }
        return sqLiteDb.update(DATABASE_TABLE, vals, tables[tableNo][0] + "="
                + rowId, null) > 0;
    }

    public boolean updateAlert_one_colunm(String DATABASE_TABLE, int tableNo,
                                          String where, String values, int column_no) {
        ContentValues vals = new ContentValues();

        vals.put(tables[tableNo][column_no], String.valueOf(values));
        // for (int i = 0; i < values.length; i++) {
        // vals.put(tables[tableNo][i + 1], values[i]);
        // }
        return sqLiteDb.update(DATABASE_TABLE, vals, where, null) > 0;
    }


    public boolean updateConversation(String DATABASE_TABLE,String update_time, String last_msg_id,String convesation_id)
    {

        ContentValues values = new ContentValues();
        values.put("updated_at",update_time);
        values.put("last_msg_id", last_msg_id);
        sqLiteDb.update(DATABASE_TABLE, values, "conversation_id=?", new String[] {convesation_id});

        return true;
    }

    public boolean updateAlert(String DATABASE_TABLE, int tableNo, long rowId,
                               ContentValues vals) {
        return sqLiteDb.update(DATABASE_TABLE, vals, tables[tableNo][0] + "="
                + rowId, null) > 0;
    }


    public synchronized Cursor fetchAllAlerts(String DATABASE_TABLE,
                                              int tableNo, String orderby) {
        try {
            return sqLiteDb.query(DATABASE_TABLE, tables[tableNo], null, null,
                    null, null, orderby);

        } catch (Exception e) {
            // Log.d("yo", e.getMessage());
            return null;
        }
    }

    public synchronized Cursor fetchAlert(String DATABASE_TABLE, int tableNo,
                                          long rowId) throws SQLException {
        Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo],
                tables[tableNo][0] + "=" + rowId, null, null, null, null);
        return ret;
    }

    public synchronized Cursor fetchAlert(String DATABASE_TABLE, int tableNo,
                                          String where) throws SQLException {
        Cursor ret = sqLiteDb.query(DATABASE_TABLE, tables[tableNo], where,
                null, null, null, null);
        return ret;
    }


}