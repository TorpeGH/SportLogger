package com.teocri.sportlogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseManager extends SQLiteOpenHelper {

    public static final String TAG = "LOD";
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "sportLogger_DB.db";

    // names of  table and columns
    public static final String TABLE_NAME = "locations";
    public static final String COLUMN_NAME_LATITUDE  = "latitude";
    public static final String COLUMN_NAME_LONGITUDE = "longitude";
    public static final String COLUMN_NAME_DATE      = "date";
    public static final String COLUMN_NAME_TIME      = "time";
    public static final String COLUMN_NAME_ACCURACY  = "accuracy";

    // create command
    public static final String SQL_CREATE = "create table " + TABLE_NAME + " (_id integer primary key autoincrement, "
            + COLUMN_NAME_LATITUDE  + " real, "
            + COLUMN_NAME_LONGITUDE + " real, "
            + COLUMN_NAME_DATE      + " text, "
            + COLUMN_NAME_TIME      + " text, "
            + COLUMN_NAME_ACCURACY  + " real) ";
    // drop command
    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    // constructor
    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        Log.w(TAG,"onCreate");
        db.execSQL(SQL_CREATE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        onCreate(db);
    }

    void addLocation(double lat, double lon, String date, String time, double acc) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_LATITUDE,    lat);
        values.put(COLUMN_NAME_LONGITUDE,   lon);
        values.put(COLUMN_NAME_DATE,        date);
        values.put(COLUMN_NAME_TIME,        time);
        values.put(COLUMN_NAME_ACCURACY,    acc);
        // Insert the new row, returning the primary key value of the new row
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(DatabaseManager.TABLE_NAME, null, values);
        db.close();
        Log.i(TAG,"Database insertion returned:" + id);
    }

    /**********_QUERIES_**********/
    ArrayList<String> getRows() {         // return an array of strings representing the entries
        String [] columns = {COLUMN_NAME_LATITUDE, COLUMN_NAME_LONGITUDE, COLUMN_NAME_DATE, COLUMN_NAME_TIME, COLUMN_NAME_ACCURACY};
        ArrayList<String> a = new ArrayList<>();
        try {
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.query(TABLE_NAME, columns, null, null, null, null, null);
            Log.i(TAG, "Locations returned: " + c.getCount());
            while (c.moveToNext()) {
                double lat  = c.getDouble(c.getColumnIndexOrThrow(COLUMN_NAME_LATITUDE));
                double lon  = c.getDouble(c.getColumnIndexOrThrow(COLUMN_NAME_LONGITUDE));
                String date = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_DATE));
                String time = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_TIME));
                double acc  = c.getDouble(c.getColumnIndexOrThrow(COLUMN_NAME_ACCURACY));

                String s = new String("");
                s += lat + "A" + lon + "O" + date + "D" + time + "T" + acc;

                a.add(s);
            }
            c.close();  db.close();
        } catch (Throwable t) {Log.e(TAG,"getRows: " + t.toString(),t);}
        return a;
    }

    ArrayList<String> getSingleDates() {
        ArrayList<String> a = new ArrayList<>();
        try {
            String SINGLE_DATES_QUERY = String.format("SELECT %s FROM %s GROUP BY %s",
                            COLUMN_NAME_DATE,
                            TABLE_NAME,
                            COLUMN_NAME_DATE,
                            COLUMN_NAME_DATE);
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery(SINGLE_DATES_QUERY, null);

            while (c.moveToNext()) {
                String date = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_DATE));
                a.add(date);
            }
            c.close();  db.close();
        } catch (Throwable t) {Log.e(TAG,"getSingleDates: " + t.toString(),t);}
        return a;
    }

    String getDateIstances(String date) {
        String n = "9999";
        try {
            String SINGLE_DATES_QUERY = String.format("SELECT %s FROM %s WHERE %s = '%s'",
                            COLUMN_NAME_DATE,
                            TABLE_NAME,
                            COLUMN_NAME_DATE,
                            date);
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery(SINGLE_DATES_QUERY, null);
            n = String.valueOf(c.getCount());
            c.close();  db.close();
        } catch (Throwable t) {Log.e(TAG,"getDateIstances: " + t.toString(),t);}
        return n;
    }

    String getDateLatitude(String date) {
        String lat = "36.1640305";
        try {
            String DATE_LATITUDE_QUERY = String.format("SELECT %s FROM %s WHERE %s = '%s' ORDER BY %s ASC",
                            COLUMN_NAME_LATITUDE,
                            TABLE_NAME,
                            COLUMN_NAME_DATE,
                            date,
                            COLUMN_NAME_TIME);
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery(DATE_LATITUDE_QUERY, null);
            c.moveToFirst();
            lat = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_LATITUDE));
            c.close();  db.close();
        } catch (Throwable t) {Log.e(TAG,"getDateLatitude: " + t.toString(),t);}
        return lat;
    }

    String getDateLongitude(String date) {
        String lon = "-115.1382751";
        try {
            String DATE_LONGITUDE_QUERY = String.format("SELECT %s FROM %s WHERE %s = '%s' ORDER BY %s ASC",
                            COLUMN_NAME_LONGITUDE,
                            TABLE_NAME,
                            COLUMN_NAME_DATE,
                            date,
                            COLUMN_NAME_TIME);
            SQLiteDatabase db = getReadableDatabase();
            Cursor c = db.rawQuery(DATE_LONGITUDE_QUERY, null);
            c.moveToFirst();
            lon = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_LONGITUDE));
            c.close();  db.close();
        } catch (Throwable t) {Log.e(TAG,"getDatelONGITUDE: " + t.toString(),t);}
        return lon;
    }

    /**********_READ_METHODS_**********/
    public String readLatitude (String s) {
        if (s.length() == 0)
            return "";

        String lat = "";
        for (int i = 0; s.charAt(i) != 'A'  &&  i < s.length(); i++)
            lat += s.charAt(i);

        return lat;
    }

    public String readLongitude (String s) {
        if (s.length() == 0)
            return "";

        int start;
        for (start = 0; s.charAt(start) != 'A'  &&  start < s.length(); start++) {} start++;

        String lat = "";
        for (int i = start; s.charAt(i) != 'O'  &&  i < s.length(); i++)
            lat += s.charAt(i);

        return lat;
    }

    public  String readDate (String s) {
        if (s.length() == 0)
            return "";

        int start;
        for (start = 0; s.charAt(start) != 'O'  &&  start < s.length(); start++) {} start++;

        String lat = "";
        for (int i = start; s.charAt(i) != 'D' &&  i < s.length(); i++)
            lat += s.charAt(i);

        return lat;
    }

    public String readTime (String s) {
        if (s.length() == 0)
            return "";

        int start;
        for (start = 0; s.charAt(start) != 'D'  &&  start < s.length(); start++) {} start++;

        String lat = "";
        for (int i = start; s.charAt(i) != 'T' &&  i < s.length(); i++)
            lat += s.charAt(i);

        return lat;
    }

    public String readAccuracy (String s) {
        if (s.length() == 0)
            return "";

        int start;
        for (start = 0; s.charAt(start) != 'T'  &&  start < s.length(); start++) {} start++;

        String lat = "";
        for (int i = start; i < s.length(); i++)
            lat += s.charAt(i);

        return lat;
    }
}
