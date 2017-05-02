package com.example.capstone;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
        * Created by Charles Henninger on 4/22/2017.
        */
/***********************************************
 * This activity displays an offline Mapbox map, determined by the tour ID that is passed
 * to it through an intent. This class also creates waypoints on the map and displays the user locations
 * and any assets that come with the tour waypoints.
 */

public class MyTours extends AppCompatActivity {

    private static final String TAG = "MyTours Activity";

    private MapView mapView;
    private MapboxMap map;
    private OfflineManager offlineManager;
    private LocationEngine locationEngine;

    //Listing tours variables (recycleview and its adapter)
    private RecyclerView waypointRecView;
    private MyToursAdapter waypointAdapter;

    //database helper instance
    private WaypointDB waypointdb;

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Context mcontext = this;
        //Get tour ID from intent
        final String tourid = (String)getIntent().getExtras().get("id");

        setContentView(R.layout.activity_my_tours);

        //create DB instance
        waypointdb = new WaypointDB(this);

        //assign recycleview and adapter variables
        waypointRecView = (RecyclerView) findViewById(R.id.waypoint_recycler);
        waypointRecView.setLayoutManager(new LinearLayoutManager(this));
        waypointRecView.setHasFixedSize(true);
        waypointAdapter = new MyToursAdapter();
        waypointRecView.setAdapter(waypointAdapter);

        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = (MapView) findViewById(R.id._mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                // Set up the OfflineManager
                map = mapboxMap;
                offlineManager = OfflineManager.getInstance(MyTours.this);
                offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
                    @Override
                    public void onList(final OfflineRegion[] offlineRegions) {

                        // Add all of the region names to a list
                        ArrayList<String> offlineRegionsNames = new ArrayList<>();
                        for (OfflineRegion offlineRegion : offlineRegions) {
                            offlineRegionsNames.add(getRegionName(offlineRegion));
                        }
                        Integer index = null;
                        for (int i = 0 ; i < offlineRegionsNames.size() ; i++) {
                            if (tourid.equals(offlineRegionsNames.get(i))) {
                                index = i;
                            }
                        }

                        // Get the region bounds and zoom
                        LatLngBounds bounds = ((OfflineTilePyramidRegionDefinition)
                                offlineRegions[index].getDefinition()).getBounds();
                        double regionZoom = ((OfflineTilePyramidRegionDefinition)
                                offlineRegions[index].getDefinition()).getMinZoom();

                        // Create new camera position
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(bounds.getCenter())
                                .zoom(regionZoom)
                                .build();

                        // Move camera to new position
                        locationEngine = LocationSource.getLocationEngine(mcontext);
                        locationEngine.activate();
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        Log.i("TOUR ID:",tourid);
                        List<Waypoint> waypoints = waypointdb.getTourWaypoints(tourid);
                        if(waypoints != null){
                            for(int i = 0 ; i < waypoints.size() ; i++){
                                waypointAdapter.addWaypoint(waypoints.get(i).getID(),waypoints.get(i).getTitle(),waypoints.get(i).getDesc());
                                map.addMarker(new MarkerViewOptions()
                                        .position(new LatLng(waypoints.get(i).getLat(), waypoints.get(i).getLng()))
                                        .title(waypoints.get(i).getTitle()));
                                map.setMyLocationEnabled(true);
                            }
                        }
                    }
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error: " + error);
                    }
                });
            }
        });
    }

    private String getRegionName(OfflineRegion offlineRegion) {
        // Get the retion name from the offline region metadata
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

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }
}

