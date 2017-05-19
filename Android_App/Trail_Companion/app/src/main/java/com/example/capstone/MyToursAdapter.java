package com.example.capstone;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
    private MyToursAdapter.OnImageclickedlistener clicklistener;
    private MyToursAdapter.OnVidclickedlistener clicklistener1;
    Context mcontext;

    //just for exp
    private ArrayList<String> assets;

    public MyToursAdapter(Context thiscontext){
        titles = new ArrayList<>();
        descriptions = new ArrayList<>();
        ids = new ArrayList<>();
        mcontext = thiscontext;

        //just for expo
        assets=new ArrayList<>();
    }
    //add more params and lists to create more complex recycle views
    public void addWaypoint(String id, String title, String description,String expoasset) {
        titles.add(title);
        descriptions.add(description);
        ids.add(id);

        //just for expo
        assets.add(expoasset);
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
        public ImageView imageview;



        public WaypointItemHolder(View itemView) {
            super(itemView);
            waypointTitleView = (TextView)itemView.findViewById(R.id.waypoint_name);
            waypointDescView = (TextView)itemView.findViewById(R.id.waypoint_desc);
            imageview = (ImageView) itemView.findViewById(R.id.waypoint_pic);
            viewPicButton = (ImageButton) itemView.findViewById(R.id.pics_button);
            viewVidButton = (ImageButton) itemView.findViewById(R.id.video_button);
        }

        public void bind(String title, String desc) {
            waypointTitleView.setText(title);
            waypointDescView.setText(desc);
        }

        //JUST FOR EXPO
        public void buttonbind(String id){
            switch(id){
                case "sf1":
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageResource(R.drawable.golden_gate_park);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "sf2":
                    viewVidButton.setVisibility(View.VISIBLE);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "sf3":
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageResource(R.drawable.resturant);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "sf4":
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageResource(R.drawable.fort_point);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "pn1":
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageResource(R.drawable.pizza);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "pn2":
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageResource(R.drawable.bar);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "pn3":
                    viewVidButton.setVisibility(View.VISIBLE);
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageResource(R.drawable.jazz);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "os1":
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageResource(R.drawable.kelly);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "os2":
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageResource(R.drawable.robots);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "os3":
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageResource(R.drawable.mu);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "os4":
                    imageview.setVisibility(View.VISIBLE);
                    imageview.setImageResource(R.drawable.library);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
                case "os5":
                    viewVidButton.setVisibility(View.VISIBLE);
                    waypointDescView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    public interface OnImageclickedlistener {
        void imageclicked(ImageView picview, Context mcontext);
    }

    public void setImageclickedlistener(OnImageclickedlistener l) {
        clicklistener = l;
    }


    public interface OnVidclickedlistener {
        void vidclicked(String id, Context mcontext);
    }

    public void setVidclickedlistener(OnVidclickedlistener l) {
        clicklistener1 = l;
    }

    @Override
    public void onBindViewHolder(final WaypointItemHolder holder, final int position) {
        holder.bind(titles.get(titles.size() - position - 1), descriptions.get(descriptions.size() - position - 1));
        holder.buttonbind(ids.get(titles.size() - position - 1));

        holder.viewVidButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(clicklistener1 != null){
                    clicklistener1.vidclicked(ids.get(titles.size() - position - 1),mcontext);
                }
            }
        });

        holder.imageview.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(clicklistener != null){
                    clicklistener.imageclicked(holder.imageview,mcontext);
                }
            }
        });
    }

}
