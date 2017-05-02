package com.example.capstone;

/**
 * Created by Charles Henninger on 4/02/2017.
 */
/***********************************************
 * This is the tour class which will contain all relevant information about a tour. the information
 * in this class will be accessed via the metafile.json that is available after downloading a tour
 * */

public class Tour {

    String _id;
    String _name;
    String _desc;

    // constructors
    public Tour(){}


    public Tour(String id, String name, String desc){
        this._id = id;
        this._name = name;
        this._desc = desc;
    }

    // functions for getting variables
    public String getID(){
        return this._id;
    }

    public String getName(){
        return this._name;
    }

    public String getDesc(){
        return this._desc;
    }

    // function for setting variables
    public void setID(String id){
        this._id = id;
    }

    public void setName(String name){
        this._name = name;
    }

    public void setDesc(String desc){
        this._desc = desc;
    }
}