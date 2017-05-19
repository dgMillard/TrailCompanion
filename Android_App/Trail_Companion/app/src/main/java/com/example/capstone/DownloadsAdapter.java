package com.example.capstone;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Charles Henninger on 4/25/2017.
 */
/***********************************************
 * This is the adapter for the recycler view in the downloads activity that display information about
 * available tour downloads and handle the download buttons functionality
 */

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.TourItemHolder> {

    private ArrayList<String> titles;
    private ArrayList<String> descriptions;
    private ArrayList<String> ids;
    private OnDownloadbuttonclickedlistener clicklistener;
    Context mcontext;

    public DownloadsAdapter(Context thiscontext){
        titles = new ArrayList<String>();
        descriptions = new ArrayList<String>();
        ids = new ArrayList<String>();
        mcontext = thiscontext;
    }
    //add more params and lists to create more complex recycle views
    public void addTour(String id, String title, String description) {
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
    public TourItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.download_items, parent, false);
        TourItemHolder viewHolder = new TourItemHolder(view);
        return viewHolder;
    }

    class TourItemHolder extends RecyclerView.ViewHolder {
        private TextView titleview;
        private TextView descview;
        public ImageButton downloadbutton;

        public TourItemHolder(View itemView) {
            super(itemView);
            titleview = (TextView)itemView.findViewById(R.id.D_region);
            descview = (TextView)itemView.findViewById(R.id.D_desc);
            downloadbutton = (ImageButton) itemView.findViewById(R.id.load_button);
        }

        public void bind(String title, String desc) {
            titleview.setText(title);
            descview.setText(desc);
        }
    }
    public interface OnDownloadbuttonclickedlistener {
        void downloadbuttonclicked(String id, Context mcontext);
    }

    public void setDownloadbuttonclickedlistener(OnDownloadbuttonclickedlistener l) {
        clicklistener = l;
    }
    @Override
    public void onBindViewHolder(TourItemHolder holder, final int position) {
        holder.bind(titles.get(titles.size() - position - 1), descriptions.get(descriptions.size() - position - 1));

        holder.downloadbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(clicklistener != null){
                    clicklistener.downloadbuttonclicked(ids.get(ids.size() - position - 1),mcontext);
                }
            }
        });
    }

}
