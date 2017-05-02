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
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
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
            Toast.makeText(ShowMyTours.this, "You have no regions yet. Download some on the Discover Page", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            for(int i = 0 ; i< tours.size() ; i++){
                showAdapter.addTour(tours.get(i).getID(),tours.get(i).getName(),tours.get(i).getDesc());
            }
        }

        //Start the intent process to create a MyTours activity
        showAdapter.setViewbuttonclickedlistener(new ShowToursAdapter.OnViewbuttonclickedlistener() {
            @Override
            public void viewbuttonclicked(String tourid) {
                startMyTours(tourid);
            }
        });

        //This is the Delete button click listener from the ShowToursAdapter class
        showAdapter.setDeletebuttonclickedlistener(new ShowToursAdapter.OnDeletebuttonclickedlistener() {
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
        });
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