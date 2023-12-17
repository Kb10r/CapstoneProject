package com.example.hikingtracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

/**
 * This Activity Shows a list of all the Hike's Maps
 * In future releases their will be cards with the separate names for each hike and their corresponding data
 */
public class ShowAllHikesActivity extends AppCompatActivity {
    public SharedPreferences preferences;

    LinearLayout hikesLayout;

    /**
     * when the Activity is created
     * We get the SharedPreferences
     * Then the Map key,value pairs and send them to the layout
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showallhikes);

        // Obtain an instance of SharedPreferences
        preferences = getSharedPreferences("ListOfHikes", Context.MODE_PRIVATE);
        hikesLayout = findViewById(R.id.hikesLayout);
        Map<String, ?> allHikes = preferences.getAll();

        printPrefList();
        addHikesToLayout(allHikes);

    }

    /**
     * This function goes through all the uri's stored in allHikes and adds the images to the layout
     * @param allHikes hike map uri's to add
     */
    private void addHikesToLayout(Map<String, ?> allHikes) {
        SharedPreferences preferences = getSharedPreferences("ListOfHikes", Context.MODE_PRIVATE);

        for (Map.Entry<String, ?> entry : allHikes.entrySet()) {
            if (entry.getValue() instanceof String) {
                String uriString = (String) entry.getValue();
                Uri uri = Uri.parse(uriString);

                ImageView imageView = new ImageView(this);
                imageView.setImageURI(uri);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                /**
                 * TODO: Create an onClick for the image to go to its own details page
                 */

                hikesLayout.addView(imageView);
            }
        }
    }

    /**
     * For development, allows me to see what key values are sent to this page
     */
    private void printPrefList() {
        for (String key: preferences.getAll().keySet()) {
            Object value = preferences.getAll().get(key);
            Log.d("PrefList", "Key: " + key + ", Value: " + value);
        }
    }

}
