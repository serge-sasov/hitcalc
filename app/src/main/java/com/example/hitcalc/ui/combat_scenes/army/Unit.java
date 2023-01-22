package com.example.hitcalc.ui.combat_scenes.army;

import org.json.JSONException;
import org.json.JSONObject;

/*
* Root class for Units
* */
public class Unit{
    protected String mTitle = "";
    //unique unit id to distinguish a unit from the similar name but other type, like Macedon LC and Macedon LN
    protected String mUnitId = "";

    // JSON constants
    private static final String TITLE = "title";
    private static final String UNIT_ID = "unit_id";


    public Unit(String title){
        mTitle = title;
    }

    //JSON Constructor
    public Unit(JSONObject jo) throws JSONException {
        mTitle = jo.getString(TITLE);
        //If there is no value just leave it out
        if(jo.getString(UNIT_ID)!= null) {
            mUnitId = jo.getString(UNIT_ID);
        }
    }

    //Default constructor to copy objects
    public Unit() {
    }

    public String title(){
        return mTitle;
    }

    public String getUnitId(){
        return mUnitId;
    }

    // ------------------------- JSON Conversion ---------------------------
    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put(TITLE, mTitle);
        jo.put(UNIT_ID, mUnitId); //Comment out once implemented
        return jo;
    }
}
