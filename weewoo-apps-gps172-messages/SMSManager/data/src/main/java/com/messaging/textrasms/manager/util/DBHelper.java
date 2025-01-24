package com.messaging.textrasms.manager.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DBHelper extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "snag.db";
    private static final String TABLE_WORDS = "words";
    private static final String TABLE_LIKELY_SPAM = "likely_spam";
    private static final int DATABASE_VERSION = 1;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public int thisWordCount(String word, int category) {
        int count = 0;
        String countQuery = "select " + Constants.CATEGORIES[category] + " from " + TABLE_WORDS + " where word='" + word + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            count = cursor.getInt(0);

        }
        cursor.close();

        return count;
    }

    public int allWordCount(int category) {
        int count = 0;
        String countQuery = "select sum(" + Constants.CATEGORIES[category] + ") from " + TABLE_WORDS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            Log.d("splitWord", "splitWord11: cat" + category + ">>>" + cursor);
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }

    public int wordCount(String word) {
        int count = 0;
        String countQuery = "select " + Constants.CATEGORIES[0] + " + " + Constants.CATEGORIES[1] + ""
                + " from " + TABLE_WORDS + " where word='" + word + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            count = cursor.getInt(0);
        }
        cursor.close();

        return count;
    }

}