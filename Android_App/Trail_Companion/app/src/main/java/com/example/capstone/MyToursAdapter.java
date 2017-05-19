package com.example.capstone;

import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Charles Henninger on 4/22/2017.
 */
/***********************************************
 * This is the adapter for the Recycle viewer in the MyTours activity which will handle listing all
 * of the waypoint class items related to the current tour being viewed
 */

public class MyToursAdapter extends RecyclerView.Adapter<MyToursAdapter.WaypointItemHolder> {

    private ArrayList<String> titles;
    private ArrayList<String> descriptions;
    private ArrayList<String> ids;

    public MyToursAdapter(){
        titles = new ArrayList<>();
        descriptions = new ArrayList<>();
        ids = new ArrayList<>();
    }
    //add more params and lists to create more complex recycle views
    public void addWaypoint(String id, String title, String description) {
        titles.add(title);
        descriptions.add(description);
        ids.add(id);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    @Override
    public WaypointItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.waypoint_list_item, parent, false);
        WaypointItemHolder viewHolder = new WaypointItemHolder(view);
        return viewHolder;
    }

    class WaypointItemHolder extends RecyclerView.ViewHolder {
        private TextView waypointTitleView;
        private TextView waypointDescView;
        public ImageButton viewPicButton;
        public ImageButton viewVidButton;


        public WaypointItemHolder(View itemView) {
            super(itemView);
            waypointTitleView = (TextView)itemView.findViewById(R.id.waypoint_name);
            waypointDescView = (TextView)itemView.findViewById(R.id.waypoint_desc);
            viewPicButton = (ImageButton) itemView.findViewById(R.id.pics_button);
            viewVidButton = (ImageButton) itemView.findViewById(R.id.video_button);
        }

        public void bind(String title, String desc) {
            waypointTitleView.setText(title);
            waypointDescView.setText(desc);
        }
    }

    @Override
    public void onBindViewHolder(WaypointItemHolder holder, final int position) {
        holder.bind(titles.get(titles.size() - position - 1), descriptions.get(descriptions.size() - position - 1));
    }

}
