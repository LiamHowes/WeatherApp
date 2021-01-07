// Name:            Liam Howes
// Student Number:  5880331
// Email:           lh15fh@brocku.ca
// Project:         WeatherApp

package ca.brocku.weatherapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class addingLocation extends AppCompatActivity implements LocationListener {

    final String appid = "0e4101dc78f5cdf2af145c2285b6d4f4";
    Toolbar toolbar;
    EditText searchBar;
    String url_base = "https://api.openweathermap.org/data/2.5/weather?";
    ProgressDialog pd;
    String selected_folder;
    String location_searchWIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adding_location);
        // lets the app get the url connection in the main UI thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        selected_folder = getIntent().getStringExtra("folder"); //store the folder name

        toolbar = findViewById(R.id.adding_location_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(addingLocation.this, MainActivity.class);
                intent.putExtra("selectedFolder", selected_folder);
                startActivity(intent);
            }
        });
        if(savedInstanceState!=null){
            searchBar = findViewById(R.id.search_bar);
            searchBar.setText(savedInstanceState.getString("LOCATION_SEARCH"));
        }
    }

    public void search(View view) {
        searchBar = findViewById(R.id.search_bar);
        // search for the city
        // API call
        String city_name = searchBar.getText().toString();
        String url = url_base+"q="+city_name+"&units=metric&APPID="+appid; // finish the API call
        new fetchWeather(this, url, selected_folder, false); // get json values and store them in SQLiteDatabase
        Intent intent = new Intent(addingLocation.this, MainActivity.class);
        intent.putExtra("selectedFolder", selected_folder);
        startActivity(intent); //go back to main screen

    }

    @Override
    protected void onPause() { // for screen rotation
        super.onPause();
        searchBar = findViewById(R.id.search_bar);
        location_searchWIP = searchBar.getText().toString(); // get the text so far

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // send the text currently being typed
        outState.putString("LOCATION_SEARCH", location_searchWIP);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }
}
