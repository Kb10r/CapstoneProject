package com.example.hikingtracker;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.hikingtracker.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * This MapsActivity holds a GoogleMap that pulls its data from the Google MapsAPI
 * Also the screenshot functionality is here
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private ActivityMapsBinding binding;
    private ArrayList<LatLng> listOfLoc;
    private LatLng lastLatLng, firstLatLng;
    private Context context;
    private boolean firstRun;

    /**
     * runs when the activity is loaded
     * Binds the map to the view, gets extras from the passed in intent Bundle
     * Starts the SupportMapFragment Async loading the map
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setting the view and the binding for use by the Map fragment
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;

        //get Location data and firstRun from MainActivity
        Bundle extras = getIntent().getExtras();
        listOfLoc = extras.getParcelableArrayList("latLngArr");
        firstRun = extras.getBoolean("firstRun");


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Sets GoogleMap data
     * Adds the route to the map
     * Adds Markers to the map
     * OnMapLoaded to create the screenshot
     * @param googleMap the map to change around
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //finds first and last locations
        lastLatLng = listOfLoc.get(listOfLoc.size() - 1);
        firstLatLng = listOfLoc.get(0);

        //init for the map
        gMap = googleMap;
        gMap.setBuildingsEnabled(false);
        gMap.setIndoorEnabled(false);
        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        //adds the decorations to the map
        addRouteToMap(gMap);
        addMarkersToMap(gMap);

        //moves the camera to the starting point and zooms in
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLatLng, 17));

        /*
         * This callback is a little weird, before using this all screenshots (dynamically) were black
         * screens because the map wasn't loaded yet
         */
        gMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                gMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(@Nullable Bitmap bitmap) {
                        Log.d("HikingDev", "firstRun is: " + firstRun);
                        if (firstRun) {
                            Log.d("HikingDev", "So I am taking a screenshot!");
                            saveScreenshot(bitmap);
                        } else {
                            Log.d("HikingDev", "So I am NOT taking a screenshot!");
                        }
                    }
                });
            }
        });
    }

    /**
     * Adds the starting and ending markers to the map
     * @param gMap passed in
     */
    public void addMarkersToMap(GoogleMap gMap) {
        gMap.addMarker( new MarkerOptions().position(firstLatLng).title("Starting point"));
        gMap.addMarker( new MarkerOptions().position(lastLatLng).title("Ending point"));
    }

    /**
     * Adds the route to the map as a Polyline
     * @param gMap passed in
     */
    public void addRouteToMap(GoogleMap gMap) {
        Resources res = getResources();
        //Iterates along all locations and connects them with a PolyLine
        gMap.addPolyline(
                new PolylineOptions().addAll(listOfLoc)
                        .color(res.getColor(R.color.brightYellow))
                        .jointType(JointType.ROUND)
                        .width(20)
        );
    }

    /**
     * This is a development method
     * Prints the URI's of all the "HikingTracker" pictures in the internal storage
     * I use this to compare against the SharedPrefs
     * @param context
     */
    public static void printImageFileNames(Context context) {
        //Items passed to the Cursor to find the images
        ContentResolver contentResolver = context.getContentResolver();
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
        String selection = MediaStore.Images.Media.DISPLAY_NAME + " LIKE 'HikingTracker_%'";

        //This is like setting up a SQL query
        Cursor cursor = contentResolver.query(
                imageUri,
                projection,
                selection,
                null,
                null
        );
        if (cursor != null) {
            try {
                int displayNameColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);

                while (cursor.moveToNext()) {
                    String displayName = cursor.getString(displayNameColumnIndex);
                    Log.d("HikingDev", "Image File Name: " + displayName);
                }
            } finally {
                cursor.close();
            }
        }
    }

    /**
     * If Build.VERSION_CODES > Q (Android 29?)
     * We use the newer MediaStore API
     * in the future I will add a function for the lower Android versions that uses older file manipulation
     * to save the bitmap
     * @param bitmap to save
     */
    public void saveScreenshot(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            /*
            Log.d("HikingDev", Build.VERSION.SDK_INT + "");
            Log.d("HikingDev", "Q OR HIGHER");
            */
            saveBitmapToMediaStoreQ(bitmap);
            printImageFileNames(this);
        } else {
            //Log.d("HikingDev", "LOWER THAN Q");
            // For devices running below Android Q, you have to use use the traditional file saving approach
            // saveBitmapToFile(bitmap);
        }
    }

    /**
     * Takes in a bitmap and saves it as a png to the filesystem
     * Also adds the uri to the SharedPreferences so we can pull it out later
     * @param bitmap image passed in
     */
    private void saveBitmapToMediaStoreQ(Bitmap bitmap) {

        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        //named thus so we can find it later and associate it with the SharedPreferences
        String imageName = "HikingTracker_" + new SimpleDateFormat(("yyyyMMdd_HHmmss"), Locale.getDefault()).format(new Date());
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        try {
            //Compresses the bitmap to a ong and saves it to the filesystem
            OutputStream outputStream = resolver.openOutputStream(imageUri);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();

            //Adds the URI to the ListOfHikes
            SharedPreferences preferences = getSharedPreferences("ListOfHikes", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("uri_" + imageName, imageUri.toString());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}