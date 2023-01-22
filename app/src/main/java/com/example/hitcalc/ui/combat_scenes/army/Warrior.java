package com.example.hitcalc.ui.combat_scenes.army;

import org.json.JSONException;
import org.json.JSONObject;

public class Warrior extends Unit {
    // Title,Type,Quality,Size,Movement,Property,Army,TwoHex
    protected String mType = ""; // Unit type
    protected boolean mIsTwoHex = false;

    // JSON constants
    private static final String TYPE = "type";
    private static final String IS_TWO_HEX = "is_two_hex";


    public Warrior(String title, String type, boolean isTwoHex){
        super(title);

        mType = type;
        if(isTwoHex){
            mIsTwoHex = isTwoHex;
        }
    }

    //JSON Constructor
    public Warrior(JSONObject jo) throws JSONException {
        super(jo);

        mType = jo.getString(TYPE);
        mIsTwoHex = jo.getBoolean(IS_TWO_HEX);

    }

    //Default constructor to copy objects
    public Warrior() {
        super();
    }

    public String type(){
        return mType;
    }

    public boolean twoHexSize(){
        return mIsTwoHex;
    }

    // ------------------------- JSON Conversion ---------------------------
    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        //Need to call all previous methods in consequentially
        JSONObject jo = new JSONObject();
        jo = super.convertToJSON();

        jo.put(TYPE, mType);
        jo.put(IS_TWO_HEX, mIsTwoHex);
        return jo;
    }
}
