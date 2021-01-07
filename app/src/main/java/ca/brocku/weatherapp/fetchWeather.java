// Name:            Liam Howes
// Student Number:  5880331
// Email:           lh15fh@brocku.ca
// Project:         WeatherApp

package ca.brocku.weatherapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class fetchWeather {
    String data;
    String description;
    Double temperature;
    String location_name;
    String icon_name;

    public fetchWeather(Context context, String the_url, String selected_folder, boolean update){
        try {
            URL url = new URL(the_url);
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            data = br.readLine();
            JSONObject json = new JSONObject(data);
            // only the weather is in an object, within an array for some reason lol
            JSONArray jsonWeather = json.getJSONArray("weather");
            JSONObject jsonWeatherDetails = jsonWeather.getJSONObject(0);
            description = jsonWeatherDetails.getString("description");
            icon_name = jsonWeatherDetails.getString("icon");

            JSONObject jsonMain = json.getJSONObject("main");
            temperature = jsonMain.getDouble("temp");

            location_name = json.getString("name");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e){
            Toast.makeText(context, "City Name Not Found", Toast.LENGTH_SHORT).show(); // if the city name can't be found, let the user know
            return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        // add temperature, description to SQL database
        DataHelper dh = new DataHelper(context);
        SQLiteDatabase dataWriter = dh.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("location", location_name);
        cv.put("folder", selected_folder);
        cv.put("temperature", temperature);
        cv.put("description", description);
        cv.put("icon", icon_name);
        // get all the date and time information
        int year, month, day, hour, minute;
        String am_pm;
        String minute_string;
        Calendar now = Calendar.getInstance();
        year = now.get(Calendar.YEAR);
        month = now.get(Calendar.MONTH) + 1; //(index starts at 0)
        day = now.get(Calendar.DAY_OF_MONTH);
        hour = now.get(Calendar.HOUR);
        minute = now.get(Calendar.MINUTE);
        if(hour==0){
            hour=12; //I don't want it to display something like 12:00am as 0:00am. Looks bad.
        }
        if(minute<10){
            minute_string = "0"+minute; // don't want time to display as, say 9:6pm. I want 9:06pm.
        }
        else{
            minute_string = ""+minute;
        }
        if(now.get(Calendar.AM_PM) == Calendar.AM){
            am_pm = "am";
        }
        else{
            am_pm = "pm";
        }
        String date_and_time =  year+"/"+month+"/"+day+" "+hour+":"+minute_string+am_pm;
        cv.put("time", date_and_time); // string-build the date and time of update
        // write to the database
        dataWriter.insert(DataHelper.LOCATIONS_DB_TABLE, null, cv);
        dataWriter.close();

        // update the locations int count in the folder database
        // read the current count
        DataHelper dataHelper = new DataHelper(context);
        SQLiteDatabase dataReader = dataHelper.getReadableDatabase();
        String[] fields = {"locations"};
        // get how many locations the folder has
        Cursor cursor = dataReader.query(DataHelper.FOLDER_DB_TABLE, fields, "folder = '"+selected_folder+"'", null, null, null, null);
        cursor.moveToFirst();
        int num_of_locations = cursor.getInt(0); // get locations count
        cursor.close();
        dataReader.close();
        // write the updated count
        DataHelper dHelper = new DataHelper(context);
        SQLiteDatabase dWriter = dHelper.getWritableDatabase();
        // get how many locations the folder has
        ContentValues contentValues = new ContentValues();
        contentValues.put("locations", num_of_locations+1); // update locations to +1
        dWriter.update(DataHelper.FOLDER_DB_TABLE, contentValues, "folder = '"+selected_folder+"'", null);
        dataWriter.close();

        if(update){ // if we're updating the location
            Toast.makeText(context, "Location(s) Updated", Toast.LENGTH_SHORT).show(); // let user know that the location has been UPDATED
        }
        else{
            Toast.makeText(context, "Location Added", Toast.LENGTH_SHORT).show(); // let user know that the location has been ADDED
        }
    }
}

