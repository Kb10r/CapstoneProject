package com.example.hikingtracker;

import androidx.fragment.app.FragmentActivity;

import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.hikingtracker.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap gMap;
    private ActivityMapsBinding binding;
    private ArrayList<LatLng> listOfLoc;
    private LatLng lastLatLng, firstLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle extras = getIntent().getExtras();

        //uncomment this when getting actual data and delete the switch below and the 3 load functions
        //listOfLoc = extras.getParcelableArrayList("latLngArr");

        //This is only for the Capstone Showcase, 3 pretend routes
        switch (extras.getString("routeSelected")) {
            case "route1":
                listOfLoc = loadRoute1();
                break;
            case "route2":
                listOfLoc = loadRoute2();
                break;
            case "route3":
                listOfLoc = loadRoute3();
                break;
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
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
    }

    /**
     * Adds the starting and ending markers to the map
     * @param gMap passed in
     */
    public void addMarkersToMap(GoogleMap gMap) {
        gMap.addMarker( new MarkerOptions().position(firstLatLng).title("Starting point")).showInfoWindow();
        gMap.addMarker( new MarkerOptions().position(lastLatLng).title("Ending point")).showInfoWindow();
    }

    /**
     * Adds the route to the map as a Polyline
     * @param gMap passed in
     */
    public void addRouteToMap(GoogleMap gMap) {
        Resources res = getResources();
        gMap.addPolyline(
                new PolylineOptions().addAll(listOfLoc)
                        .color(res.getColor(R.color.brightYellow))
                        .jointType(JointType.ROUND)
                        .width(20)
        );
    }

    public ArrayList<LatLng> loadRoute1() {//Cactus to Gym
        ArrayList<LatLng> l = new ArrayList<LatLng>();
        l.add(new LatLng(33.51151, -112.12335));
        l.add(new LatLng(33.51320, -112.12334));
        l.add(new LatLng(33.51318, -112.12569));
        l.add(new LatLng(33.51270, -112.12581));
        l.add(new LatLng(33.51270, -112.12685));
        l.add(new LatLng(33.51271, -112.12691));
        l.add(new LatLng(33.51283, -112.12710));
        l.add(new LatLng(33.51274, -112.12747));
        l.add(new LatLng(33.51272, -112.13070));
        l.add(new LatLng(33.51278, -112.13087));
        l.add(new LatLng(33.51277, -112.13196));

        return l;
    }
    public ArrayList<LatLng> loadRoute2() {//Tech Building to Grove
        ArrayList<LatLng> l = new ArrayList<LatLng>();;
        l.add(new LatLng(33.51063, -112.12727));
        l.add(new LatLng(33.51101, -112.12693));
        l.add(new LatLng(33.51473, -112.12694));
        l.add(new LatLng(33.51472, -112.13089));
        l.add(new LatLng(33.51474, -112.13140));
        l.add(new LatLng(33.51546, -112.13141));

        return l;

    }

    public ArrayList<LatLng> loadRoute3() {//Other?!?
        ArrayList<LatLng> l = new ArrayList<LatLng>();
        l.add(new LatLng(33.51061, -112.12996));
        l.add(new LatLng(33.51271, -112.13));
        l.add(new LatLng(33.51276, -112.13028));
        l.add(new LatLng(33.51297, -112.13042));
        l.add(new LatLng(33.51339, -112.13042));
        l.add(new LatLng(33.51364, -112.13023));
        l.add(new LatLng(33.51375, -112.12991));
        l.add(new LatLng(33.51367, -112.12937));
        l.add(new LatLng(33.51334, -112.12943));
        l.add(new LatLng(33.51333, -112.12958));
        l.add(new LatLng(33.51276, -112.12958));
        l.add(new LatLng(33.51274, -112.12901));
        l.add(new LatLng(33.51214, -112.12899 ));
        l.add(new LatLng(33.51211, -112.12790 ));
        l.add(new LatLng(33.51154, -112.12790 ));
        l.add(new LatLng(33.51143, -112.12798 ));
        l.add(new LatLng(33.51145, -112.12901 ));
        l.add(new LatLng(33.51062, -112.12899 ));
        return l;

    }
}