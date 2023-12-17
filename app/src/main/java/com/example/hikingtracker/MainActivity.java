package com.example.hikingtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the Main Activity of my android app
 *
 * @author Connor Rolstad
 */
public class MainActivity extends AppCompatActivity {
    /*
     * These All come from activity_main.xml
     */
    TextView tv_latitude, tv_longitude, tv_counter, tv_altitude;
    Button b_getLocation, b_stopLocation, b_toMap;

    /*
     * class variables
     */
    int counter;
    double startingAlt;
    private static final int FINE_LOCATION_CODE = 100;
    private static final int READ_EXTERNAL_STORAGE = 101;
    private List<Location> trackedLocations;
    private ArrayList<LatLng> trackedLatLngs;
    public SharedPreferences preferences;
    boolean firstRun;

    /**
     * FusedLocationProviderClient is how android and google lets you
     * grab the location of the device based on the gps
     */
    FusedLocationProviderClient fusedLocationClient;
    /**
     * Used to continually update the location of the device
     */
    LocationCallback locationCallback;

    /**
     * Gets called at the start of the application's lifecycle
     * @param savedInstanceState passed in
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //links all the ui objects on the screen to the code
        tv_latitude = findViewById(R.id.tv_latitude);
        tv_longitude = findViewById(R.id.tv_longitude);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_counter = findViewById(R.id.tv_counter); tv_counter.setText("0");
        b_getLocation = findViewById(R.id.b_getLocation);
        b_stopLocation = findViewById(R.id.b_stopLocation);
        b_toMap = findViewById(R.id.b_toMap);

        //We need to be sure that the application has access to the location and storage permissions
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_CODE);
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE);

        //Instantiating some variables
        preferences = getSharedPreferences("ListOfHikes", Context.MODE_PRIVATE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        trackedLocations = new ArrayList<Location>();
        trackedLatLngs = new ArrayList<LatLng>();
        startingAlt = -1;
        counter = 0;
        firstRun = true;

        /*
          everytime there is a new location available from the gps
          the function calls onLocationChanged with that new location
         */
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                onLocationChanged(locationResult.getLastLocation());
            }
        };


        /* This is used in development because I need to clear the SharedPreferences at some-point
        They do get cleared on uninstall so it is a memory leak once you delete the picture but not a big one!
        Eventually there will be a handler that looks through the associated SharedPreferences
        and if they aren't connected to an image it will be deleted
        */
        //clearPrefs();
        Log.d("HikingDev", "PrefList At Start: " + preferences.getAll().toString());
    }

    /**
     * Start Button:
     * You need to call getLocation before startLocationUpdates because that will crash if it
     * is the first location you get.
     */
    public void onStartLocationButton(View view) {
        getLocation();
        startLocationUpdates();
    }

    /**
     * Stop Button:
     * Stops the location updates
     */
    public void onStopLocationButton(View view) {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
    /**
     * Map Button:
     * Sends collected data to the MapsActivity
     */
    public void onMapClick(View view) {
        int numLoc = trackedLocations.size();

        if (numLoc == 0) {
            b_getLocation.callOnClick();
        } else {
            for (Location loc : trackedLocations) {
                trackedLatLngs.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
            }
            Intent intent = new Intent(view.getContext(), MapsActivity.class);
            intent.putParcelableArrayListExtra("latLngArr",trackedLatLngs);
            intent.putExtra("firstRun", firstRun);
            firstRun = false;

            view.getContext().startActivity(intent);
        }
    }

    /**
     * This function Asks the fusedLocationClient to check what the current location is
     * Then onSuccess I get information like
     * Starting Altitude and add the location to the trackedLocations
     * Then I update the textViews
     */
    @SuppressLint("MissingPermission")
    public void getLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        startingAlt = location.getAltitude();
                        trackedLocations.add(location);
                        showLocationOnScreen(location);
                    }
                });
    }

    /**
     * Creates the LocationRequest that is built and sent to the fusedLocationClient
     * That starts the callbacks (in this case every 10 intervalMillis
     */
    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(10)//updates every second ish
                //.setMaxUpdates(5)
                .setPriority(100)//100 is high priority
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper());

    }

    /**
     * Sends the updated location to the tracked locations
     * And showLocationOnScreen functions
     * @param location where the user is
     */
    public void onLocationChanged(Location location) {
        trackedLocations.add(location);
        showLocationOnScreen(location);
    }

    /**
     * This is the function that actually separates data from the Location variable
     * and changes the TextViews
     * @param location where the user is
     */
    public void showLocationOnScreen(Location location) {
        counter++;
        tv_counter.setText("Counter: " + counter);

        Double lat, lon, alt;
        lat = location.getLatitude();
        lon = location.getLongitude();
        alt = (-startingAlt + location.getAltitude()) * 3.28;//translates from meters to feet

        String latStr = "Latitude: " + lat;
        String lonStr = "Longitude: " + lon;
        String altStr = "Change in Altitude in ft: " + alt;

        tv_latitude.setText(latStr);
        tv_longitude.setText(lonStr);
        tv_altitude.setText(altStr);
    }

    /**
     * Needed to see if we have necessary permissions from the user, will ask for said permissions if we don't have it
     * @param permission what to ask the user about
     * @param requestCode unique code for each permission
     */
    public void checkPermission(String permission, int requestCode){
        if(ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        } else {
            //Toast.makeText(MainActivity.this, "This Permission: " + permission + " is already granted!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * This sends the question to the user if we did or did not get permissions
     * @param resultCode The request code passed in
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either {@link android.content.pm.PackageManager#PERMISSION_GRANTED}
     *     or {@link android.content.pm.PackageManager#PERMISSION_DENIED}. Never null.
     *
     */
    @Override
    public void onRequestPermissionsResult(int resultCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(resultCode, permissions, grantResults);
        String text = "";

        switch (resultCode) {
            case FINE_LOCATION_CODE:
                text = "Fine Location Permission";
                break;
            case READ_EXTERNAL_STORAGE:
                text = "Read External Storage Permission";
                break;
        }

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, text + " Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, text + " Denied", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Button's onClick, routes us to the ShowAllHikes Activity
     * @param view
     */
    public void goToShowAllHikes(View view) {
        Intent intent = new Intent(this, ShowAllHikesActivity.class);
        startActivity(intent);
    }

    /**
     * Creates a SharedPreferences Editor
     * Clears the preferences
     * then Applies the changes to save them
     */
    public void clearPrefs() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }
}