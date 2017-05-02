package com.example.capstone;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Charles Henninger on 4/27/2017.
 */
/***********************************************
 * This is the Waypoint Database helper class that will be used to read and write to the SQLite database
 * in order to save each waypoint so that it can be accessed in other activities.
 */

public class WaypointDB extends SQLiteOpenHelper {

    // Database info
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "waypointDB";
    private static final String TABLE_CONTACTS = "Waypoints";

    // Columns
    private static final String KEY_ID = "id";
    private static final String KEY_TOUR = "tour";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESC = "desc";
    private static final String KEY_ASSET = "asset";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LNG = "lng";

    public WaypointDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " TEXT PRIMARY KEY," + KEY_TOUR + " TEXT NOT NULL," + KEY_TITLE + " TEXT NOT NULL,"
                + KEY_DESC + " TEXT," + KEY_ASSET  + " TEXT," +KEY_LAT + " REAL NOT NULL," +KEY_LNG + " REAL NOT NULL" + ")";
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
    void addWaypoint(Waypoint thisWaypoint) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, thisWaypoint.getID());
        values.put(KEY_TOUR, thisWaypoint.getTour());
        values.put(KEY_TITLE, thisWaypoint.getTitle());
        values.put(KEY_DESC, thisWaypoint.getDesc());
        values.put(KEY_ASSET, thisWaypoint.getAsset());
        values.put(KEY_LAT, thisWaypoint.getLat());
        values.put(KEY_LNG, thisWaypoint.getLng());

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single waypoint
    Waypoint getWaypoint(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                        KEY_TOUR, KEY_TITLE, KEY_DESC, KEY_ASSET, KEY_LAT, KEY_LNG}, KEY_ID + "=?",
                new String[] { id }, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()){
            Waypoint thisWaypoint = new Waypoint(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getDouble(5), cursor.getDouble(6));
            cursor.close();
            db.close();
            return thisWaypoint;
        }
        return null;
    }

    //Get all waypoints related to a tour
    public List<Waypoint> getTourWaypoints(String tourID) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Waypoint> WaypointList = new ArrayList<Waypoint>();
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                        KEY_TOUR, KEY_TITLE, KEY_DESC, KEY_ASSET, KEY_LAT, KEY_LNG}, KEY_TOUR + "=?",
                new String[] { tourID }, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()){
            do{
                Waypoint thisWaypoint = new Waypoint(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getDouble(5), cursor.getDouble(6));
                WaypointList.add(thisWaypoint);
            }while(cursor.moveToNext());
            cursor.close();
            db.close();
            return WaypointList;
        }
        return null;
    }

    // Getting All waypoints
    public List<Waypoint> getAllWaypoints() {
        List<Waypoint> WaypointList = new ArrayList<Waypoint>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Waypoint thisWaypoint = new Waypoint(cursor.getString(0), cursor.getString(1), cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getDouble(5), cursor.getDouble(6));
                // Adding tour to list
                WaypointList.add(thisWaypoint);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        // return waypoint list
        return WaypointList;
    }

    // Delete all waypoints related to a specific tour
    public void deleteTourWaypoints(String tourID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
                        KEY_TOUR, KEY_TITLE, KEY_DESC, KEY_ASSET, KEY_LAT, KEY_LNG}, KEY_TOUR + "=?",
                new String[] { tourID }, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()){
            do{
                db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                        new String[] { String.valueOf(cursor.getString(0)) });
            }while(cursor.moveToNext());
            cursor.close();
            db.close();
        }
    }

    // Deleting single Waypoint
    public void deleteWaypoint(Waypoint thisWaypoint) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + " = ?",
                new String[] { String.valueOf(thisWaypoint.getID()) });
        db.close();
    }


    // Getting waypoint Count
    public int getTourCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

}
