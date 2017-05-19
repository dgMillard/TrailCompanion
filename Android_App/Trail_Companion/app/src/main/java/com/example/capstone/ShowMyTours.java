package com.example.capstone;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Created by Charles Henninger on 4/22/2017.
 */
/***********************************************
 * This class represents the initial MyTours page, where all tours downloaded by the user are listed
 * and can be either deleted or viewed. viewing a tour will create a MyTours class activity
 */

public class ShowMyTours extends Drawer {

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";
    private static final String TAG = "ShowMyTours";

    // DELETE AFTER EXPO
    private boolean isEndNotified;
    int count;

    //adapter variables
    private RecyclerView toursView;
    private ShowToursAdapter showAdapter;

    // Offline objects
    private OfflineManager offlineManager;
    private ProgressBar progressBar;

    //database helper instance
    private ToursDB tourdb;
    private WaypointDB waypointdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //turn off auto rotation
        //setAutoOrientationEnabled(this, false);

        super.onCreate(savedInstanceState);
        //rebuild drawer activity
        setContentView(R.layout.activity_show_my_tours);

        //pass mapbox token
        Mapbox.getInstance(this, getString(R.string.access_token));

        //set up the adapter/recycler view variables
        toursView = (RecyclerView) findViewById(R.id.showtours_recycler);
        toursView.setLayoutManager(new LinearLayoutManager(this));
        toursView.setHasFixedSize(true);
        showAdapter = new ShowToursAdapter(this);
        toursView.setAdapter(showAdapter);

        // Query the sqlite DB for offline maps
        tourdb  = new ToursDB(this);
        waypointdb = new WaypointDB(this);

        List<Tour> tours = tourdb.getAllTours();

        // Set up the offlineManager
        offlineManager = OfflineManager.getInstance(this);

        // Assign progressBar for later use
        progressBar = (ProgressBar) findViewById(R.id.Delete_progress_bar);



        if(tours.size()==0){
            count = 0;
            downloadmap(37.819917,-122.371473,37.725884,-122.527857,"San Francisco", 12, 17);
            downloadmap(45.543672,-122.654586,45.22914,-122.659333,"Portland Nightlife", 12, 17);
            downloadmap(44.569603,-123.264204,44.561009,-122.285685, "Oregon State Campus",13,18);
        }else {
            for (int i = 0; i < tours.size(); i++) {
                showAdapter.addTour(tours.get(i).getID(), tours.get(i).getName(), tours.get(i).getDesc());
            }
        }


        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   PUT THIS BACK IN AFTER EXPO
        /*if(tours.size()==0){
            Toast.makeText(ShowMyTours.this, "You have no regions yet. Download some on the Discover Page", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            for(int i = 0 ; i< tours.size() ; i++){
                showAdapter.addTour(tours.get(i).getID(),tours.get(i).getName(),tours.get(i).getDesc());
            }
        }*/

        //Start the intent process to create a MyTours activity
        showAdapter.setViewbuttonclickedlistener(new ShowToursAdapter.OnViewbuttonclickedlistener() {
            @Override
            public void viewbuttonclicked(String tourid) {
                startMyTours(tourid);
            }
        });

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!   PUT THIS BACK IN AFTER EXPO

        //This is the Delete button click listener from the ShowToursAdapter class
        /*showAdapter.setDeletebuttonclickedlistener(new ShowToursAdapter.OnDeletebuttonclickedlistener() {
            @Override
            public void deletebuttonclicked(final String tourID, final RecyclerView.ViewHolder viewHolder) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShowMyTours.this);
                builder.setTitle("Confirmation")
                        .setMessage("Delete this map from you device?")
                        .setCancelable(false)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                ((ShowToursAdapter.TourItemHolder) viewHolder).removeFromList();
                                tourdb.deleteTour(tourID);
                                waypointdb.deleteTourWaypoints(tourID);
                                File deletefile = new File(Environment.getExternalStorageDirectory()+ File.separator+"Trail Companion"+File.separator+tourID);

                                if(deletefile.exists()){
                                    Log.i("DELETING ",deletefile.getAbsolutePath()+"   | "+deletefile.delete());
                                }

                                offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
                                    @Override
                                    public void onList(final OfflineRegion[] offlineRegions) {
                                        int regionSelected = 0;
                                        int index = 0;
                                        for (OfflineRegion offlineRegion : offlineRegions) {
                                            if (tourID.equals(getRegionName(offlineRegion))) {
                                                index = regionSelected;
                                            }
                                        }

                                        offlineRegions[index].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
                                            @Override
                                            public void onDelete() {
                                                Toast.makeText(ShowMyTours.this, "Region deleted", Toast.LENGTH_LONG).show();
                                            }

                                            @Override
                                            public void onError(String error) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                progressBar.setIndeterminate(false);
                                                Log.e(TAG, "Error: " + error);
                                            }
                                        });
                                    }
                                    @Override
                                    public void onError(String error) {
                                        Log.e(TAG, "Error: " + error);
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        // The dialog will automatically close
                                    }
                                }).create();
                builder.show();
            }
        });*/
    }

    // Get the retion name from the offline region metadata
    private String getRegionName(OfflineRegion offlineRegion) {
        String regionName;
        try {
            byte[] metadata = offlineRegion.getMetadata();
            String json = new String(metadata, JSON_CHARSET);
            JSONObject jsonObject = new JSONObject(json);
            regionName = jsonObject.getString(JSON_FIELD_REGION_NAME);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to decode metadata: " + exception.getMessage());
            regionName = "Region " + offlineRegion.getID();
        }
        return regionName;
    }

    //Creates an intent to the MyTours class
    void startMyTours(String tourID){
        Intent intent = new Intent(ShowMyTours.this, MyTours.class);
        Tour tour = tourdb.getTour(tourID);
        if(tour != null){
            intent.putExtra("id",tourID);
            startActivity(intent);
        }
    }


    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! DELETE AFTER EXPO
    public void downloadmap(final double NElat, final double NElong, final double SWlat, final double SWlong, final String id, int min, int max){

        //pass in mapbox access token
        Mapbox.getInstance(this, getString(R.string.access_token));

        // Set up the OfflineManager
        offlineManager = OfflineManager.getInstance(ShowMyTours.this);

        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(NElat, NElong)) // Northeast
                .include(new LatLng(SWlat, SWlong)) // Southwest
                .build();

        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                "mapbox://styles/mapbox/outdoors-v9",
                latLngBounds,
                min,
                max,
                ShowMyTours.this.getResources().getDisplayMetrics().density);

        // Set the mapbox metadata
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_REGION_NAME, id);
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception exception) {
            Log.e(TAG, "Failed to encode metadata: " + exception.getMessage());
            metadata = null;
        }

        // Create the region asynchronously
        offlineManager.createOfflineRegion(
                definition,
                metadata,
                new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion offlineRegion) {
                        offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
                        // Display the download progress bar
                        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
                        startProgress();
                        // Monitor the download progress using setObserver
                        offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                            @Override
                            public void onStatusChanged(OfflineRegionStatus status) {
                                // Calculate the download percentage and update the progress bar
                                double percentage = status.getRequiredResourceCount() >= 0
                                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                                        0.0;
                                if (status.isComplete()) {
                                    // Download complete
                                    endProgress("Region downloaded successfully.");
                                } else if (status.isRequiredResourceCountPrecise()) {
                                    // Switch to determinate state
                                    setPercentage((int) Math.round(percentage));
                                }
                            }

                            @Override
                            public void onError(OfflineRegionError error) {
                                // If an error occurs, print to logcat
                                Log.e(TAG, "onError reason: " + error.getReason());
                                Log.e(TAG, "onError message: " + error.getMessage());
                            }

                            @Override
                            public void mapboxTileCountLimitExceeded(long limit) {
                                // Notify if offline region exceeds maximum tile count
                                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error: " + error);
                    }
                });
    }


    // Progress bar methods
    private void startProgress() {
        // Start and show the progress bar
        isEndNotified = false;
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setPercentage(final int percentage) {
        progressBar.setIndeterminate(false);
        progressBar.setProgress(percentage);
    }

    private void endProgress(final String message) {
        // Don't notify more than once
        if (isEndNotified) {
            return;
        }
        // Stop and hide the progress bar
        //isEndNotified = true;
        //progressBar.setIndeterminate(false);
        //progressBar.setVisibility(View.GONE);
        //Toast.makeText(ShowMyTours.this, message, Toast.LENGTH_LONG).show();


        tourdb.addTour(new Tour("San Francisco","San Francisco","A small tour of the Golden Gate Park area of San Francisco, showing off some cool things to do and see."));
        tourdb.addTour(new Tour("Portland Nightlife","Portland Nightlife","Showing off some of the cool places to grab a drink in downtown Portland, specifically around Burnside."));
        tourdb.addTour(new Tour("Oregon State Campus","Oregon State Campus","A quick tour of some locations around Oregon State."));

        switch(count){
            case 0:
                count++;
                showAdapter.addTour("San Francisco","San Francisco","A small tour of the Golden Gate Park area of San Francisco, showing off some cool things to do and see.");
                waypointdb.addWaypoint(new Waypoint("sf4","San Francisco","Fort Point","Fort Point has stood guard at the narrows of the Golden Gate for over 150 years. The Fort has been called 'the pride of the Pacific', 'the Gibraltar of the West Coast', and 'one of the most perfect models of masonry in America'. When construction began during the height of the California Gold Rush, Fort Point was planned as the most formidable deterrence America could offer to a naval attack on California. Although its guns never fired a shot in anger, the \"Fort at Fort Point\" as it was originally named has witnessed Civil War, obsolescence, earthquake, bridge construction, reuse for World War II, and preservation as a National Historic Site.","fort_point.png",37.809068, -122.477045));
                waypointdb.addWaypoint(new Waypoint("sf3","San Francisco","Get Lunch Here!","Best burritos of your life! go here.","resturant.jpg",37.778682, -122.478933));
                waypointdb.addWaypoint(new Waypoint("sf2","San Francisco","Walt Disney Family Museum","The Walt Disney Family Museum is an American museum that features the life and legacy of Walt Disney. The museum is located in The Presidio of San Francisco, part of the Golden Gate National Recreation Area in San Francisco.","text",37.799167, -122.458506));
                waypointdb.addWaypoint(new Waypoint("sf1","San Francisco","Golden Gate Park Conservatory","As you venture into one of the first buildings situated in Golden Gate Park, you will encounter the oldest remaining municipal wooden conservatory in the United States. As the first public structures of its kind in the country, the Conservatory of Flowers serves as a safe haven for thought and imagination as visitors browse about some of the most exotic-looking blooms, sometimes presenting the beauty of colorful rarities. Highly praised in the world of history, architecture, engineering, and nature, the Conservatory of Flowers has been placed on the National Register of Historic Places, and is considered an intensely valued landmark in San Francisco.","golden_gate_park.jpg",37.769048,-122.473269));
                break;
            case 1:
                count++;
                showAdapter.addTour("Portland Nightlife","Portland Nightlife","Showing off some of the cool places to grab a drink in downtown Portland, specifically around Burnside.");
                waypointdb.addWaypoint(new Waypoint("pn3","Portland Nightlife","Jimmy Mak's","This is (or was) THE place to see some live jazz at any day of the week in Portland. Talented artists and great location. Check out the mezzanine!","jazz.jpg",45.524529, -122.681268));
                waypointdb.addWaypoint(new Waypoint("pn2","Portland Nightlife","Ground Kontrol","This is a really nice bar arcade. Full bar and retro video games make for a very good time.","bar.jpg",45.523889, -122.675502));
                waypointdb.addWaypoint(new Waypoint("pn1","Portland Nightlife","Sizzle Pie","Great place to start the night out. Draft beer and pizza paired with a great atmosphere.","pizza.jpg",45.522914, -122.659333));
                break;
            case 2:
                count++;
                showAdapter.addTour("Oregon State Campus","Oregon State Campus","A quick tour of some locations around Oregon State.");
                waypointdb.addWaypoint(new Waypoint("os4","Oregon State Campus","Valley Library","A very large portion of this application was written inside this building.","library.jpg",44.565623, -123.276015));
                waypointdb.addWaypoint(new Waypoint("os3","Oregon State Campus","MU","The MU always smells like Panda Express. No one talks about it, but we all know it's true.","mu.jpg",44.565266, -123.278914));
                waypointdb.addWaypoint(new Waypoint("os2","Oregon State Campus","Robotics Expo Area","Go check out the awesome robots over here during expo! One of the highlights for sure.","robots.jpg",44.567255, -123.277925));
                waypointdb.addWaypoint(new Waypoint("os1","Oregon State Campus","Kelly Engineering","Hey, That's where you are right now! That's some crazy stuff right there.","kelly.jpg",44.566861, -123.278461));
                progressBar.setIndeterminate(false);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ShowMyTours.this, message, Toast.LENGTH_LONG).show();
                break;
            default:break;
        }
    }
    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ^ DELETE AFTER EXPO

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}