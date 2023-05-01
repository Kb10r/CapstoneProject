package com.example.hikingtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

public class MainActivity extends AppCompatActivity {
    /**
     * These All come from activity_main.xml
     */
    TextView tv_latitude, tv_longitude, tv_counter, tv_altitude;
    Button b_getLocation, b_stopLocation, b_toMap;
    RadioGroup radioGroup;

    /**
     * class variables
     */
    int counter;
    double startingAlt;
    private static final int FINE_LOCATION_CODE = 100;
    private List<Location> trackedLocations;
    private ArrayList<LatLng> trackedLatLngs;

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
        radioGroup = findViewById(R.id.rg_chooseRoute);

        //We need to be sure that the application has access to the location permissions
        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_CODE);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        trackedLocations = new ArrayList<Location>();
        trackedLatLngs = new ArrayList<LatLng>();
        startingAlt = -1;
        counter = 0;

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


        /*
          Start Button
          You need to call getLocation before startLocationUpdates because that will crash if it
          is the first location you get.
         */
        b_getLocation.setOnClickListener(v -> {
            getLocation();
            startLocationUpdates();
        });

        /*
          Stop Button
          Stops the location updates
         */
        b_stopLocation.setOnClickListener(v -> {
            //Toast.makeText(MainActivity.this, "This should stop the counting", Toast.LENGTH_SHORT).show();
            fusedLocationClient.removeLocationUpdates(locationCallback);
        });


        /*
          Map Button
          Currently this finds which radio button is checked
         */
        b_toMap.setOnClickListener(v -> {
            //finds the checked button of the three
            RadioButton checkedButton = findViewById(radioGroup.getCheckedRadioButtonId());

            Intent intent = new Intent(v.getContext(), MapsActivity.class);
            intent.putExtra("routeSelected", checkedButton.getTag().toString());
            v.getContext().startActivity(intent);

            /*
              Uncomment this for using actual data and delete above code
            int numLoc = trackedLocations.size();

            if (numLoc == 0) {
                b_getLocation.callOnClick();
            } else {
                Location lastLocation = trackedLocations.get(numLoc - 1);

                for (Location loc : trackedLocations) {
                    trackedLatLngs.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
                }
                Intent intent = new Intent(v.getContext(), MapsActivity.class);
                intent.putParcelableArrayListExtra("latLngArr",trackedLatLngs);

                v.getContext().startActivity(intent);
            }
            */
        });
    }

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

    public void onLocationChanged(Location location) {
        trackedLocations.add(location);
        showLocationOnScreen(location);
    }

    public void showLocationOnScreen(Location location) {
        counter++;
        tv_counter.setText("Counter: " + counter);

        Double lat, lon, alt;
        lat = location.getLatitude();
        lon = location.getLongitude();
        alt = (-startingAlt + location.getAltitude()) * 3.28;//translates from meters to feet

        tv_latitude.setText(lat + "");
        tv_longitude.setText(lon + "");
        tv_altitude.setText("Change in Altitude in ft: " + alt);
    }

    public void checkPermission(String permission, int requestCode){
        if(ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, requestCode);
        } else {
            Toast.makeText(MainActivity.this, "This Permission: " + permission + " is already granted!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int resultCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(resultCode, permissions, grantResults);
        String text = "";

        switch (resultCode) {
            case FINE_LOCATION_CODE:
                text = "Fine Location Permission";
                break;
        }

        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, text + " Granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, text + " Denied", Toast.LENGTH_SHORT).show();
        }
    }

}