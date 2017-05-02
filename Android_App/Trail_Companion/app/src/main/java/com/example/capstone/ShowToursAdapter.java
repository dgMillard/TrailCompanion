package com.example.capstone;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Charles Henninger on 4/22/2017.
 */
/***********************************************
 * This is an Recycler adapter class made to manage ShowMyTour's recycler view. This adapter will
 * bind and display tour items to the recycler view and handle the function of both delete and view buttons.
 */

public class ShowToursAdapter extends RecyclerView.Adapter<ShowToursAdapter.TourItemHolder> {

    private ArrayList<String> titles;
    private ArrayList<String> descriptions;
    private ArrayList<String> ids;
    private ShowToursAdapter.OnDeletebuttonclickedlistener clicklistener1;
    private ShowToursAdapter.OnViewbuttonclickedlistener clicklistener2;
    Context mcontext;

    public ShowToursAdapter(Context context){
        titles = new ArrayList<>();
        descriptions = new ArrayList<>();
        ids = new ArrayList<>();
        mcontext = context;
    }

    public void addTour(String id, String title, String description) {
        ids.add(id);
        titles.add(title);
        descriptions.add(description);
        notifyDataSetChanged();
    }

    private int adapterPositionToArrayIndex(int adapterPosition) {
        int i = ids.size() - adapterPosition - 1;
        if(i<0){
            return 0;
        }else{
            return i;
        }
    }

    @Override
    public int getItemCount() {
        return titles.size();
    }

    @Override
    public TourItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.tours_list_item, parent, false);
        TourItemHolder viewHolder = new TourItemHolder(view);
        return viewHolder;
    }


    class TourItemHolder extends RecyclerView.ViewHolder {
        private TextView titleview;
        private TextView descview;
        public Button viewbutton;
        public Button deletebutton;

        public TourItemHolder(View itemView) {
            super(itemView);
            titleview = (TextView)itemView.findViewById(R.id.showtour_name);
            descview = (TextView)itemView.findViewById(R.id.showtour_desc);
            viewbutton = (Button) itemView.findViewById(R.id.tour_view_button);
            deletebutton = (Button) itemView.findViewById(R.id.tour_list_del);
        }

        //This is called from the ShowMyTours deletebuttonclickadapter. this deletes tours on the recycler view
        public void removeFromList() {
            int position = getAdapterPosition();
            titles.remove(adapterPositionToArrayIndex(position));
            descriptions.remove(adapterPositionToArrayIndex(position));
            ids.remove(adapterPositionToArrayIndex(position));
            notifyItemRemoved(position);
        }

        public void bind(String title, String desc) {
            titleview.setText(title);
            descview.setText(desc);
        }
    }

    public interface OnDeletebuttonclickedlistener {
        void deletebuttonclicked(String tourID, RecyclerView.ViewHolder viewHolder);
    }


    public void setDeletebuttonclickedlistener(ShowToursAdapter.OnDeletebuttonclickedlistener l) {
        clicklistener1 = l;
    }

    public interface OnViewbuttonclickedlistener {
        void viewbuttonclicked(String id);
    }

    public void setViewbuttonclickedlistener(ShowToursAdapter.OnViewbuttonclickedlistener l) {
        clicklistener2 = l;
    }

    //Click handlers for the two buttons
    @Override
    public void onBindViewHolder(final TourItemHolder holder, final int position) {
        holder.bind(titles.get(titles.size() - position - 1), descriptions.get(descriptions.size() - position - 1));
        holder.viewbutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(clicklistener2 != null){
                    clicklistener2.viewbuttonclicked(ids.get(adapterPositionToArrayIndex(position)));
                }
            }
        });
        holder.deletebutton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(clicklistener1 != null){
                    clicklistener1.deletebuttonclicked(ids.get(adapterPositionToArrayIndex(position)),holder);
                }
            }
        });
    }
}
