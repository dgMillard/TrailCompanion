package com.example.capstone;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.util.Log.i;

//*** Created by Charles Henninger on 4/25/2017.
/***********************************************
 * This is the downloads activity that will use OKHttp to get
 * a JSON object containing available tour downloads from the server, and display the information
 * via it's adapter
 */

public class Downloads extends Drawer {

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";
    private static final String TAG = "Downloads";
    private OkHttpClient mOkHttpClient;

    //downloading related variables
    private boolean isEndNotified;
    private ProgressBar progressBar;
    private OfflineManager offlineManager;

    private DownloadManager downloadManager;
    private long downloadReference;

    //Listing tours variables (recycleview and its adapter)
    private RecyclerView downloadsRecView;
    private DownloadsAdapter loadAdapter;

    //database helper instance
    private ToursDB tourdb;
    private WaypointDB waypointdb;

    //App and tours file variables
    private File appDir;
    private File toursDir;
    private String appPathway;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //turn off auto rotation (NOT WORKING YET)
        //setAutoOrientationEnabled(this, false);

        super.onCreate(savedInstanceState);

        //Get or create file to keep tour zip files in. file is placed in the application pathway
        appDir = this.getDir("Tours",MODE_PRIVATE);
        appPathway = appDir.getAbsoluteFile()+File.separator;

        //rebuild drawer activity
        setContentView(R.layout.activity_downloads);

        //assign recycleview and adapter variables
        downloadsRecView = (RecyclerView) findViewById(R.id.load_recycler);
        downloadsRecView.setLayoutManager(new LinearLayoutManager(this));
        downloadsRecView.setHasFixedSize(true);
        loadAdapter = new DownloadsAdapter(this);
        downloadsRecView.setAdapter(loadAdapter);

        //create DB instance
        waypointdb = new WaypointDB(this);
        tourdb  = new ToursDB(this);

        // action for download recieved
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadReference);
                    Cursor cursor = downloadManager.query(query);
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndex)) {
                            String tourID = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                            unZip(tourID);
                            addTourDATA(tourID);
                        }
                    }
                }
            }
        };
        registerReceiver(receiver, filter);

        //The following are hard coded "tours" for testing purposes
        mOkHttpClient = new OkHttpClient();
        HttpUrl reqUrl = HttpUrl.parse(getString(R.string.downloads_list_url));
        Request request = new Request.Builder()
                .url(reqUrl)
                .build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String r = response.body().string();
                try {
                    JSONObject j = new JSONObject(r);
                    JSONArray jTours = j.getJSONArray("tourListings");
                    for(int i = 0; i < jTours.length(); i++){
                        loadAdapter.addTour(jTours.getJSONObject(i).getString("tour_uid"),jTours.getJSONObject(i).getString("tour_name"),jTours.getJSONObject(i).getString("tour_desc"));
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }

            }
        });

        loadAdapter.setDownloadbuttonclickedlistener(new DownloadsAdapter.OnDownloadbuttonclickedlistener() {
            @Override
            public void downloadbuttonclicked(String tourid,Context mcontext) {
                Tour t = tourdb.getTour(tourid);
                if(t == null){
                    toursDir = new File(appPathway+tourid);
                    i("FILE CREATED","Directory path: "+toursDir.getAbsolutePath());

                    downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                    Uri Download_Uri = Uri.parse(getString(R.string.downloads_url)+tourid);
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
                    DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                    request.setTitle(tourid);
                    //File exPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,tourid+".zip");
                    i("DOWNLOADING FILE PATH",Environment.getExternalStorageDirectory().getAbsolutePath());
                    downloadReference = downloadManager.enqueue(request);
                }else {
                    Toast.makeText(Downloads.this, "Tour already downloaded", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void unZip(String ID){
        File zip = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+File.separator+ ID+".zip");
        File outfile = new File(Environment.getExternalStorageDirectory()+File.separator+"Trail Companion"+File.separator+ID);
        try {
            ZipFile zipFile = new ZipFile(zip.getAbsolutePath());
            zipFile.extractAll(outfile.getAbsolutePath());
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }

    private void addTourDATA(String tourID) {
        String jsonStr = null;
        try {
            File jsonFile = new File(Environment.getExternalStorageDirectory()+File.separator+"Trail Companion"+File.separator+tourID+File.separator+"metadata.json");
            FileInputStream stream = new FileInputStream(jsonFile);
            try {
                FileChannel fileChannel = stream.getChannel();
                MappedByteBuffer bb = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
                jsonStr = Charset.defaultCharset().decode(bb).toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }

            String tourName, organization, tourDesc,waypointName, waypointDesc, waypointAsset, waypointIndex;
            Double NElat, NElng, SWlat, SWlng, waypointLat,waypointLng;
            int waypointCount;

            JSONObject mapJson = new JSONObject(jsonStr);
            tourName = mapJson.getString("tour_name");
            tourDesc = mapJson.getString("tour_desc");
            waypointCount = mapJson.getInt("waypoint_count");
            organization = mapJson.getString("organization");
            JSONObject mapInfo = mapJson.getJSONObject("mapInfo");
            JSONObject NWcorner = mapInfo.getJSONObject("topLeft");
            NElng = Double.parseDouble(NWcorner.getString("y"));
            SWlat = Double.parseDouble(NWcorner.getString("x"));
            JSONObject SEcorner = mapInfo.getJSONObject("bottomRight");
            NElat = Double.parseDouble(SEcorner.getString("x"));
            SWlng = Double.parseDouble(SEcorner.getString("y"));
            JSONArray waypoints  = mapJson.getJSONArray("waypoints");

            Log.i("WAYPOINT COUNT", "" + waypointCount);
            downloadmap(NElat,NElng,SWlat,SWlng,tourID);
            tourdb.addTour(new Tour(tourID,tourName,tourDesc));
            for(int i = 0 ; i < waypointCount ; i++){
                waypointName = waypoints.getJSONObject(i).getString("name");
                waypointDesc = waypoints.getJSONObject(i).getString("desc");
                waypointLat = Double.parseDouble(waypoints.getJSONObject(i).getString("xLoc"));
                waypointLng = Double.parseDouble(waypoints.getJSONObject(i).getString("yLoc"));
                waypointAsset = waypoints.getJSONObject(i).getString("asset");
                waypointIndex = waypoints.getJSONObject(i).getString("index");
                waypointdb.addWaypoint(new Waypoint(waypointIndex, tourID, waypointName, waypointDesc, waypointAsset, waypointLat, waypointLng));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadmap(final double NElat, final double NElong, final double SWlat, final double SWlong, final String id){

        //pass in mapbox access token
        Mapbox.getInstance(this, getString(R.string.access_token));

        // Set up the OfflineManager
        offlineManager = OfflineManager.getInstance(Downloads.this);

        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(NElat, NElong)) // Northeast
                .include(new LatLng(SWlat, SWlong)) // Southwest
                .build();

        // Define the offline region
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                "mapbox://styles/mapbox/outdoors-v9",
                latLngBounds,
                13,
                20,
                Downloads.this.getResources().getDisplayMetrics().density);

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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
        isEndNotified = true;
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);

        Toast.makeText(Downloads.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}