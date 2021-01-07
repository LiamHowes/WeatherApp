// Name:            Liam Howes
// Student Number:  5880331
// Email:           lh15fh@brocku.ca
// Project:         WeatherApp

package ca.brocku.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    ArrayList<String> folderRows = new ArrayList<>();
    String selected_folder;
    int selected_position;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter rAdapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<weatherItem> weatherList;
    String url_base = "https://api.openweathermap.org/data/2.5/weather?";
    final String appid = "0e4101dc78f5cdf2af145c2285b6d4f4";
    private static final int PERMISSION_ACCESS_INTERNET = 333;

    private Boolean showingAddFolderDialog = false;
    AlertDialog addFolderDialog;
    private Boolean showingDeleteFolderDialog = false;
    AlertDialog deleteFolderDialog;

    private Boolean showingDeletelocationDialog = false;
    AlertDialog deleteLocationDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // lets the app get the url connection in the main UI thread, without needing to make fetchWeather AsyncTask
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        selected_folder = getIntent().getStringExtra("selectedFolder"); //get and store the folder name

        // get saved instances of alert dialogs, so they re-appear instead of crashing when user rotates screen
        if(savedInstanceState!=null){
            // get selected folder
            selected_folder = savedInstanceState.getString("SELECTED_FOLDER");

            showingDeletelocationDialog = savedInstanceState.getBoolean("SHOWING_DELETE_LOCATION_DIALOG");
            showingDeleteFolderDialog = savedInstanceState.getBoolean("SHOWING_DELETE_FOLDER_DIALOG");
            showingAddFolderDialog = savedInstanceState.getBoolean("SHOWING_ADD_FOLDER_DIALOG");
            if(showingAddFolderDialog){
                findViewById(R.id.newFolderButton).callOnClick(); // restore dialogs if user rotates screen while they're open
            }
            if(showingDeleteFolderDialog){
                findViewById(R.id.deleteFolderButton).callOnClick();
            }
            if(showingDeletelocationDialog){
                findViewById(R.id.removeLocationButton).callOnClick();
            }
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        navigationView.bringToFront();
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        getFolders();
        createHomeFolder();
        displayFolderName(); //if none selected, defaults to Home folder

        createWeatherList();
        buildRecyclerView();
        rAdapter.notifyDataSetChanged();

        // on-click listener for if a user switches folders
        ListView folders = findViewById(R.id.folderList);
        folders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selected_position = position;
                selected_folder = parent.getAdapter().getItem(position).toString();
                TextView selected = findViewById(R.id.folderName);
                selected.setText(selected_folder);
                drawerLayout.closeDrawer(GravityCompat.START);
                // re-load the weather locations stored in the newly-selected folder
                weatherList.clear();
                createWeatherList();
                buildRecyclerView();
                rAdapter.notifyDataSetChanged();
            }
        });
    }

    public void createWeatherList(){ //cycle through database and build the list
        weatherList = new ArrayList<>();
        //for each location in folder...
        String[] fields = new String[]{"location", "folder", "temperature", "description", "icon", "time"};
        DataHelper dh = new DataHelper(this);
        SQLiteDatabase dataReader = dh.getReadableDatabase();
        // get all the locations for the selected folder (default is Home)
        Cursor cursor = dataReader.query(DataHelper.LOCATIONS_DB_TABLE, fields, "folder = '"+selected_folder+"'", null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            // get all the weather data...
            String locationName = cursor.getString(0);
            String temperature = String.valueOf(cursor.getDouble(2)); //skip 1, which references the folder name
            String description = cursor.getString(3);
            String icon = cursor.getString(4);
            String date = cursor.getString(5);
            // and insert it
            insertWeatherLocation(locationName, temperature, description, icon, date); // add the location's information to the screen
            cursor.moveToNext();
        }
        cursor.close();
        dataReader.close();
        dh.close();
    }
    public void buildRecyclerView(){
        recyclerView = findViewById(R.id.recyclerView); // get the recycler view
        layoutManager = new LinearLayoutManager(this);
        rAdapter = new weatherAdapter(weatherList); // pass to adapter

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(rAdapter); // set the adapter
    }

    public void insertWeatherLocation(String loc, String temp, String desc, String icon, String date){
        temp = temp+"°C"; //add the degrees symbol
        // translate the icon code into one of my custom weather icons
        switch (icon) {
            case "01d":
            case "01n":
                weatherList.add(new weatherItem(loc, temp, desc, R.drawable.full_sun, date));
                break;
            case "02d":
            case "02n":
                weatherList.add(new weatherItem(loc, temp, desc, R.drawable.few_clouds, date));
                break;
            case "03d":
            case "03n":
                weatherList.add(new weatherItem(loc, temp, desc, R.drawable.scattered_clouds, date));
                break;
            case "04d":
            case "04n":
                weatherList.add(new weatherItem(loc, temp, desc, R.drawable.broken_clouds, date));
                break;
            case "09d":
            case "09n":
                weatherList.add(new weatherItem(loc, temp, desc, R.drawable.shower_rain, date));
                break;
            case "10d":
            case "10n":
                weatherList.add(new weatherItem(loc, temp, desc, R.drawable.rain, date));
                break;
            case "11d":
            case "11n":
                weatherList.add(new weatherItem(loc, temp, desc, R.drawable.thunderstorm, date));
                break;
            case "13d":
            case "13n":
                weatherList.add(new weatherItem(loc, temp, desc, R.drawable.snow, date));
                break;
            case "50d":
            case "50n":
                weatherList.add(new weatherItem(loc, temp, desc, R.drawable.mist, date));
                break;
        }
    }

    @Override
    public void onBackPressed() { // allows folders drawer to be closed on back button press
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }


    public void newFolder(View view){
        final AlertDialog.Builder alert = new AlertDialog.Builder((this));
        // adding a text field for user input, gets the name of the new folder
        final EditText editText = new EditText(this);
        alert.setMessage(Html.fromHtml("<font color='#766fa2'>Enter New Folder Name</font>"));
        alert.setTitle(Html.fromHtml("<font color='#766fa2'>New Folder</font>"));
        alert.setView(editText);

        // save a new folder with the inputted name
        alert.setPositiveButton(Html.fromHtml("<font color='#766fa2'>Save</font>"),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newFolderName = editText.getText().toString();
                DataHelper dh = new DataHelper(getApplicationContext());
                SQLiteDatabase dataWriter = dh.getWritableDatabase();
                ContentValues folder = new ContentValues();
                folder.put("folder", newFolderName);
                folder.put("locations", 0); //initialize to 0, because we haven't added any locations yet

                dataWriter.insert(DataHelper.FOLDER_DB_TABLE, null, folder);
                dataWriter.close();

                Toast.makeText(getApplicationContext(), "Folder Created", Toast.LENGTH_SHORT).show();
                getFolders(); //re-load folder list to see the new folder we've created
                showingAddFolderDialog = false; // dialog is closed
            }
        });
        alert.setNegativeButton(Html.fromHtml("<font color='#766fa2'>Cancel</font>"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showingAddFolderDialog = false; // dialog is closed
                    }}); // close alert dialog
        addFolderDialog = alert.create();
        addFolderDialog.show();
        showingAddFolderDialog = true;
    }

    @Override
    protected void onPause() { // for screen rotation
        super.onPause();
        // check if any of the 3 dialogs are open, and close them if they are
        if(addFolderDialog!=null && addFolderDialog.isShowing()) {
            addFolderDialog.dismiss();
        }
        if(deleteFolderDialog!=null && deleteFolderDialog.isShowing()) {
            deleteFolderDialog.dismiss();
        }
        if(deleteLocationDialog!=null && deleteLocationDialog.isShowing()) {
            deleteLocationDialog.dismiss();
        }
    }

    public void deleteFolder(View view){
        final LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        AlertDialog.Builder alert = new AlertDialog.Builder((this));

        String[] fields = new String[]{"folder"};
        DataHelper dh = new DataHelper(getApplicationContext());
        SQLiteDatabase dataReader = dh.getReadableDatabase();
        Cursor cursor = dataReader.query(DataHelper.FOLDER_DB_TABLE, fields, null, null, null, null, null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()){
            if(!cursor.getString(0).equals("Home")) { // not allowed to delete the Home Folder
                count++;
                LinearLayout layout = new LinearLayout(getApplicationContext());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                String folderName = cursor.getString(0);
                // make checkboxes for all the locations
                CheckBox cb = new CheckBox(getApplicationContext());
                cb.setText(folderName);
                cb.setId(count);
                layout.addView(cb);
                layout.setMinimumHeight(50);
                mainLayout.addView(layout);

            }
            cursor.moveToNext();
        }
        final int final_count = count;
        cursor.close();
        dataReader.close();

        alert.setTitle(Html.fromHtml("<font color='#766fa2'>Delete Folder(s)</font>"));
        alert.setView(mainLayout);
        // save a new folder with the inputted name
        alert.setPositiveButton(Html.fromHtml("<font color='#766fa2'>Delete</font>"),
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    DataHelper dataHelper = new DataHelper(getApplicationContext());
                    SQLiteDatabase dataWriter = dataHelper.getWritableDatabase();
                    String[] names = new String[final_count+1];
                    for(int i=1; i<=final_count; i++){
                        CheckBox cb = mainLayout.findViewById(i);
                        if(cb.isChecked()){
                            names[i] = cb.getText().toString();
                            // delete folder from folder Database
                            dataWriter.delete(DataHelper.FOLDER_DB_TABLE, "folder = '"+names[i]+"'",
                                    null);
                            // and delete all locations in that folder from locations Database
                            dataWriter.delete(DataHelper.LOCATIONS_DB_TABLE, "folder = '"+names[i]+"'",
                                    null);
                        }
                    }
                    dataWriter.close();
                    Toast.makeText(getApplicationContext(), "Folder(s) Deleted",
                            Toast.LENGTH_SHORT).show();
                    TextView folder = findViewById(R.id.folderName);
                    selected_folder = "Home"; // return to Home folder after deleting folder
                    selected_position = 0;
                    folder.setText(selected_folder);

                    weatherList.clear(); // re-load the weather list
                    createWeatherList();
                    buildRecyclerView();

                    getFolders(); // re-load the folders
                    showingDeleteFolderDialog = false; // dialog is closed
                }
            });
        alert.setNegativeButton(Html.fromHtml("<font color='#766fa2'>Cancel</font>"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showingDeleteFolderDialog = false; // dialog is closed
                    }}); // close alert dialog
        deleteFolderDialog = alert.create();
        deleteFolderDialog.show();
        showingDeleteFolderDialog = true;
    }

    public void getFolders(){
        String[] fields = new String[]{"folder", "locations"};
        ListView folderList = findViewById(R.id.folderList);
        DataHelper dh = new DataHelper(this);
        SQLiteDatabase dataReader = dh.getReadableDatabase();
        Cursor cursor = dataReader.query(DataHelper.FOLDER_DB_TABLE, fields,
                null, null, null, null, null);
        folderRows.clear();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            folderRows.add(cursor.getString(0)); // get the folder name
            cursor.moveToNext();
        }
        if(cursor!=null && !cursor.isClosed()){
            cursor.close();
        }
        dataReader.close();
        ArrayAdapter<String> arrayAdptr = new ArrayAdapter<>(this, R.layout.list_color_text,
                folderRows);
        folderList.setAdapter(arrayAdptr);
    }

    public void createHomeFolder(){
        if(folderRows.isEmpty()) {
            String newFolderName = "Home";
            DataHelper dh = new DataHelper(getApplicationContext());
            SQLiteDatabase dataWriter = dh.getWritableDatabase();
            ContentValues folder = new ContentValues();
            folder.put("folder", newFolderName);
            folder.put("locations", 0); //initialize to 0, because we haven't added any locations yet

            dataWriter.insert(DataHelper.FOLDER_DB_TABLE, null, folder);
            dataWriter.close();
            getFolders(); //re-load folders list, now with a "Home" folder
        }
    }

    public void displayFolderName(){
        TextView folderName = findViewById(R.id.folderName);
        ListView folders = findViewById(R.id.folderList);
        if(selected_folder==null){ // if no selected folder (first load)
            selected_folder = folders.getItemAtPosition(0).toString(); //get first listed folder (default = Home)
        }
        // otherwise get the selected folder and display it
        folderName.setText(selected_folder);
    }

    public void refreshFolder(View view){
        // fetches data for all the locations stored in the folder you're currently viewing.
        DataHelper dh = new DataHelper(getApplicationContext());
        SQLiteDatabase folderReader = dh.getReadableDatabase();
        String[] folder_field = {"locations"};
        Cursor folderCursor = folderReader.query(DataHelper.FOLDER_DB_TABLE, folder_field, "folder = '"+selected_folder+"'", null, null, null, null);
        int num_locations = 0;
        folderCursor.moveToFirst();
        num_locations = folderCursor.getInt(0); // get number of locations
        folderCursor.close();

        SQLiteDatabase dataReader = dh.getReadableDatabase();
        String[] fields = {"location"}; // read all the locations in the folder
        Cursor cursor = dataReader.query(DataHelper.LOCATIONS_DB_TABLE, fields, "folder = '"+selected_folder+"'", null, null, null, null);
        cursor.moveToFirst();
        int count = 0;
        String[] locations = new String[num_locations];
        if(cursor.getCount()==0){ // no results returned (no saved locations)
            return; // end refreshFolder method
        }
        else{
            while (!cursor.isAfterLast()){
                locations[count] = cursor.getString(0); // get location names
                count++;
                cursor.moveToNext();
            }
            cursor.close();

            // we got the location names and stored them in "locations" String array, so we can delete the database entries that have those names in this folder
            SQLiteDatabase dataDeleter = dh.getWritableDatabase();
            dataDeleter.delete(DataHelper.LOCATIONS_DB_TABLE, "folder = '"+selected_folder+"'", null);

            SQLiteDatabase dataUpdate = dh.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("locations", 0); //update the locations count to reflect the recently (and temporarily) deleted locations
            dataUpdate.update(DataHelper.FOLDER_DB_TABLE, contentValues, "folder = '"+selected_folder+"'", null);
            //
            // and then, for each location in the selected folder:
            for(int i=0; i<count; i++){
                String url = url_base+"q="+locations[i]+"&units=metric&APPID="+appid; // API call for each location name
                new fetchWeather(this, url, selected_folder, true); // get updated json values and store them in SQLiteDatabase
            }

            // and finally, update the weather on-screen to match the updated values in the database.
            weatherList.clear();
            createWeatherList();
            buildRecyclerView();
        }
    }

    public void addLocation(View view){
        Intent intent = new Intent(MainActivity.this, addingLocation.class);
        intent.putExtra("folder", selected_folder);
        startActivity(intent);
    }

    public void removeLocation(View view){
        // start checkbox alertdialog, similar to deleting folders
        final LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        AlertDialog.Builder alert = new AlertDialog.Builder((this));

        String[] fields = new String[]{"location"};
        DataHelper dh = new DataHelper(getApplicationContext());
        SQLiteDatabase dataReader = dh.getReadableDatabase();

        Cursor cursor = dataReader.query(DataHelper.LOCATIONS_DB_TABLE, fields, "folder = '"+selected_folder+"'", null, null, null, null);
        cursor.moveToFirst();
        int count = 0;
        while (!cursor.isAfterLast()){
            count++;
            LinearLayout layout = new LinearLayout(getApplicationContext());
            layout.setOrientation(LinearLayout.HORIZONTAL);
            String locationName = cursor.getString(0);
            CheckBox cb = new CheckBox(getApplicationContext());
            cb.setText(locationName);
            cb.setId(count);
            layout.addView(cb);
            layout.setMinimumHeight(50);
            mainLayout.addView(layout);
            cursor.moveToNext();
        }
        final int final_count = count;
        cursor.close();
        dataReader.close();

        alert.setTitle(Html.fromHtml("<font color='#766fa2'>Delete Location(s)</font>"));
        alert.setView(mainLayout);
        // save a new folder with the inputted name
        alert.setPositiveButton(Html.fromHtml("<font color='#766fa2'>Delete</font>"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DataHelper dataHelper = new DataHelper(getApplicationContext());
                        SQLiteDatabase dataWriter = dataHelper.getWritableDatabase();
                        String[] names = new String[final_count+1];
                        for(int i=1; i<=final_count; i++){
                            CheckBox cb = mainLayout.findViewById(i);
                            if(cb.isChecked()){
                                names[i] = cb.getText().toString();
                                // delete folder from folder Database
                                dataWriter.delete(DataHelper.LOCATIONS_DB_TABLE, "location = '"+names[i]+"'",
                                        null);
                            }
                        }
                        dataWriter.close();
                        Toast.makeText(getApplicationContext(), "Location(s) Deleted",
                                Toast.LENGTH_SHORT).show();
                        weatherList.clear();
                        createWeatherList(); // re-load the locations
                        buildRecyclerView();
                        rAdapter.notifyDataSetChanged(); // tell adapter to update
                        showingDeletelocationDialog = false; // dialog is closed
                    }
                });
        alert.setNegativeButton(Html.fromHtml("<font color='#766fa2'>Cancel</font>"),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showingDeletelocationDialog = false; // dialog is closed
                    }});
        deleteLocationDialog= alert.create();
        deleteLocationDialog.show();
        showingDeletelocationDialog = true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("SHOWING_ADD_FOLDER_DIALOG", showingAddFolderDialog);
        outState.putBoolean("SHOWING_DELETE_FOLDER_DIALOG", showingDeleteFolderDialog);
        outState.putBoolean("SHOWING_DELETE_LOCATION_DIALOG", showingDeletelocationDialog);

        outState.putString("SELECTED_FOLDER", selected_folder);
    }

}