package com.example.capstone;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Charles Henninger on 4/02/2017.
 */
/***********************************************
 * This is the Tour database helper used to read and write to the Tour SQLite database.
 * Other activitys/classes will create an instance of this class to access data in the SQLite database
 */

public class ToursDB extends SQLiteOpenHelper {

    // Database info
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "toursDB";
    private static final String TABLE_CONTACTS = "Tours";

    // Columns
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESC = "desc";

    public ToursDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " TEXT PRIMARY KEY," + KEY_NAME + " TEXT NOT NULL,"
                + KEY_DESC + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    // Adding new tour
    void addTour(Tour thisTour) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, thisTour.getID());
        values.put(KEY_NAME, thisTour.getName());
        values.put(KEY_DESC, thisTour.getDesc());

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single tour
    Tour getTour(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                        KEY_NAME, KEY_DESC }, KEY_ID + "=?",
                new String[] { id }, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()){
            Tour thisTour = new Tour(cursor.getString(0),
                    cursor.getString(1), cursor.getString(2));
            cursor.close();
            return thisTour;
        }
        return null;
    }

    // Getting All Tours
    public List<Tour> getAllTours() {
        List<Tour> contactList = new ArrayList<Tour>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Tour thisTour = new Tour();
                thisTour.setID(cursor.getString(0));
                thisTour.setName(cursor.getString(1));
                thisTour.setDesc(cursor.getString(2));
                // Adding tour to list
                contactList.add(thisTour);
            } while (cursor.moveToNext());
        }

        cursor.close();
        // return tour list
        return contactList;
    }

    // Updating single tour
    public int updateTour(Tour thisTour) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, thisTour.getName());
        values.put(KEY_DESC, thisTour.getDesc());

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(thisTour.getID()) });
    }

    // Deleting single tour
    public void deleteTour(String thisTour) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(thisTour) });
        db.close();
    }


    // Getting tour Count
    public int getTourCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

}
