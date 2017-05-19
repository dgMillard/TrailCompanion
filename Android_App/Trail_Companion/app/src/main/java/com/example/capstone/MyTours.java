package com.example.capstone;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.VideoView;

import static java.lang.Math.abs;

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
    private boolean zoomOut =  false;

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
        final String tourid = (String) getIntent().getExtras().get("id");

        setContentView(R.layout.activity_my_tours);

        //create DB instance
        waypointdb = new WaypointDB(this);

        //assign recycleview and adapter variables
        waypointRecView = (RecyclerView) findViewById(R.id.waypoint_recycler);
        waypointRecView.setLayoutManager(new LinearLayoutManager(this));
        waypointRecView.setHasFixedSize(true);
        waypointAdapter = new MyToursAdapter(this);
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
                        for (int i = 0; i < offlineRegionsNames.size(); i++) {
                            if (tourid.equals(offlineRegionsNames.get(i))) {
                                index = i;
                            }
                        }

                        // Get the region bounds and zoom
                        LatLngBounds bounds = ((OfflineTilePyramidRegionDefinition)
                                offlineRegions[index].getDefinition()).getBounds();
                        double regionZoom = ((OfflineTilePyramidRegionDefinition)
                                offlineRegions[index].getDefinition()).getMinZoom();

                        // Move camera to new position
                        locationEngine = LocationSource.getLocationEngine(mcontext);
                        locationEngine.activate();
                        Log.i("TOUR ID:", tourid);

                        List<Waypoint> waypoints = waypointdb.getTourWaypoints(tourid);
                        if (waypoints != null) {
                            // Create new camera position
                            LatLng start = new LatLng(waypoints.get(0).getLat(),waypoints.get(0).getLng());
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(start)
                                    .zoom(regionZoom)
                                    .build();
                            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            for (int i = 0; i < waypoints.size(); i++) {
                                waypointAdapter.addWaypoint(waypoints.get(i).getID(), waypoints.get(i).getTitle(), waypoints.get(i).getDesc(), waypoints.get(i).getAsset());
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

        waypointAdapter.setImageclickedlistener(new MyToursAdapter.OnImageclickedlistener() {
            @Override
            public void imageclicked(ImageView picview, Context mcontext) {
                if(zoomOut){
                    Resources r = getResources();
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, r.getDisplayMetrics());
                    picview.setLayoutParams(new LinearLayout.LayoutParams((int)px,(int)px));
                    zoomOut=false;
                }
                else{
                    picview.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
                    zoomOut=true;
                }
            }
        });

        waypointAdapter.setVidclickedlistener(new MyToursAdapter.OnVidclickedlistener() {
            @Override
            public void vidclicked(String id, Context mcontext){
                /*View close = findViewById(R.id.dismiss);
                Resources r = getResources();
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, r.getDisplayMetrics());
                mapView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,(int)px));

                LinearLayout vidlayout = (LinearLayout) findViewById(R.id.videoLayout);
                vidlayout.setVisibility(View.VISIBLE);
                RecyclerView rview = (RecyclerView) findViewById(R.id.waypoint_recycler);
                rview.setVisibility(View.GONE);
                VideoView vidplayer = (VideoView) findViewById(R.id.videoview);

                Log.i("PATH",""+getCacheDir().getAbsolutePath());
                Uri videoUri;
                if(id.equals("sf2")){
                    videoUri = Uri.parse("android:resource://com.example.capstone/"+R.raw.disney);
                }else if(id.equals("pn3")){
                    videoUri = Uri.fromFile(new File("android:resource://com.example.capstone/"+R.raw.jimmy_maks));
                }else{
                    videoUri = Uri.parse("android:resource://com.example.capstone/"+R.raw.fleck);
                }

                vidplayer.setVideoURI(videoUri);
                vidplayer.setMediaController(new MediaController(mcontext));
                vidplayer.requestFocus();
                vidplayer.start();

                close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });*/
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

