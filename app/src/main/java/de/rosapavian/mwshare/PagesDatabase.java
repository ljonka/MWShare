package de.rosapavian.mwshare;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by leonid on 26.11.16.
 */

public class PagesDatabase extends SQLiteOpenHelper {
    public static final String DATABASE_NAME =
            "pages.db";
    public static final String TABLE_PAGE =
            "page";
    public static final String FIELD_TITLE = "title";
    public static final int DATABASE_VERSION = 1;

    PagesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PAGE +
                "(_id integer PRIMARY KEY," +
                FIELD_TITLE + " TEXT);");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Handle database upgrade as needed
    }

    public void saveRecord(String title) {
        long id = findPageID(title);
        if (id > 0) {
            updateRecord(id, title);
        } else {
            addRecord(title);
        }
    }
    public long addRecord(String title) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FIELD_TITLE, title);
        return db.insert(TABLE_PAGE, null, values);
    }
    public int updateRecord(long id, String title) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("_id", id);
        values.put(FIELD_TITLE, title);
        return db.update(TABLE_PAGE, values, "_id = ?",
                new String[]{String.valueOf(id)});
    }
    public int deleteRecord(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PAGE, "_id = ?", new
                String[]{String.valueOf(id)});
    }
    public long findPageID(String word) {
        long returnVal = -1;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT _id FROM " + TABLE_PAGE +
                        " WHERE " + FIELD_TITLE + " = ?", new String[]{word});
        Log.i("findWordID", "getCount()=" + cursor.getCount());
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            returnVal = cursor.getInt(0);
        }
        return returnVal;
    }

    public Cursor getTitleList() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT _id, " + FIELD_TITLE +
                " FROM " + TABLE_PAGE + " ORDER BY " + FIELD_TITLE +
                " ASC";
        try {
            Cursor c = db.rawQuery(query, null);
            return c;
        }catch(Exception ex){
            Log.e("DB Error", ex.getMessage());
            return null;
        }

    }

    public int removeAll(){
        SQLiteDatabase db = getReadableDatabase();
        String query = "DELETE FROM " + TABLE_PAGE;
        try {
            return db.delete(TABLE_PAGE, null, null);
        }catch(Exception ex){
            Log.e("DB Error", ex.getMessage());
            return 0;
        }
    }
}
