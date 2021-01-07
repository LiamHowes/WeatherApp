// Name:            Liam Howes
// Student Number:  5880331
// Email:           lh15fh@brocku.ca
// Project:         WeatherApp

package ca.brocku.weatherapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION=2;
    public static final String DB_NAME = "app data";
    public static final String FOLDER_DB_TABLE="folders";
    public static final String LOCATIONS_DB_TABLE="locations";
    public static final int DB_VERSION=1;
    private static final String CREATE_FOLDER_TABLE="CREATE TABLE "+FOLDER_DB_TABLE+
            " (folder TEXT PRIMARY KEY, locations INT);";
    private static final String CREATE_LOCATIONS_TABLE="CREATE TABLE "+LOCATIONS_DB_TABLE+
            " (location TEXT PRIMARY KEY, folder TEXT, temperature DOUBLE, description TEXT, icon TEXT, time TEXT);";

    DataHelper(Context context){
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FOLDER_TABLE); //make table to hold folders
        db.execSQL(CREATE_LOCATIONS_TABLE); //make table to hold locations and what folder they're in
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
