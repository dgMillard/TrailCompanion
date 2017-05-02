package com.example.capstone;

/**
 * Created by Charles Henninger on 4/27/2017.
 */
/***********************************************
 * This is the Waypoint class which will contain all relevant info on a waypoint's location and assets.
 * each waypoint will have an id,title,it's related tour id, desc,asses,and lat/lng coordinates
 */

public class Waypoint {

    String _id;
    String _tour;
    String _title;
    String _desc;
    String _asset;
    Double _lat;
    Double _lng;

    // constructors
    public Waypoint(){}

    public Waypoint(String id, String tourID, String title, String desc, String asset, Double lat, Double lng){
        this._id = id;
        this._tour = tourID;
        this._lat = lat;
        this._lng = lng;
        this._desc = desc;
        this._title = title;
        this._asset = asset;
    }

    // functions for getting variables
    public String getID(){
        return this._id;
    }

    public String getTitle(){return this._title;}

    public String getDesc(){
        return this._desc;
    }

    public String getAsset(){
        return this._asset;
    }

    public String getTour(){
        return this._tour;
    }

    public Double getLat(){
        return this._lat;
    }

    public Double getLng(){
        return this._lng;
    }

}