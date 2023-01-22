package com.example.hitcalc.ui.combat_scenes.map;

import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;

import org.json.JSONException;
import org.json.JSONObject;

public class Hex {
    private String mHexType;
    private Integer mHexViewId;
    //Probably need to be extended to ArrayList<WarriorItem>
    private WarriorInShock mWarrior; //Theoretically there can be more than just one warrior

    //Coordinates of used hex
    private Integer mCoordinateX;
    private Integer mCoordinateY;

    // JSON constants
    private static final String HEX_TYPE = "hex_type";
    private static final String HEX_VIEW_ID = "hex_view_id";
    private static final String HEX_X_COORDINATE = "hex_x_coordinate";
    private static final String HEX_Y_COORDINATE = "hex_y_coordinate";
    private static final String HEX_WARRIOR = "hex_warrior";

    public Hex(String type, Integer viewId, Integer x, Integer y){
        mHexType = type;
        mHexViewId = viewId;
        mCoordinateX = x;
        mCoordinateY = y;
    }

    //Clone Hex
    public Hex(Hex hex){
        mHexType = hex.getHexType();
        mHexViewId = hex.getHexViewId();
        mCoordinateX = hex.getCoordinateX();
        mCoordinateY = hex.getCoordinateY();
        //Copy the object instead of reference to it
        if(hex.getWarrior() != null) {
            mWarrior = new WarriorInShock(hex.getWarrior());
        }
    }

    //JSON Constructor
    public Hex(JSONObject jo) throws JSONException {
        mHexType = jo.getString(HEX_TYPE);
        mHexViewId = jo.getInt(HEX_VIEW_ID);
        mCoordinateX = jo.getInt(HEX_X_COORDINATE);
        mCoordinateY = jo.getInt(HEX_Y_COORDINATE);
        if(!jo.isNull(HEX_WARRIOR)) {
            //If not null, populate the warrior object
            mWarrior = new WarriorInShock(jo.getJSONObject(HEX_WARRIOR));
        }
        else{
            mWarrior = null;
        }
    }


    //JSON support
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        jo.put(HEX_TYPE, mHexType);
        jo.put(HEX_VIEW_ID, mHexViewId);
        jo.put(HEX_X_COORDINATE, mCoordinateX);
        jo.put(HEX_Y_COORDINATE, mCoordinateY);
        if(mWarrior != null) {
            jo.put(HEX_WARRIOR, mWarrior.convertToJSON());
        }
        return jo;
    }


    public void placeWarrior(WarriorInShock warrior) {
        mWarrior = warrior;
    }

    public void removeWarrior() {
        mWarrior = null;
    }

    public WarriorInShock getWarrior() {
        return mWarrior;
    }

    public Integer getHexViewId() {
        return mHexViewId;
    }

    public String getHexType() {
        return mHexType;
    }

    public Integer getCoordinateX() {
        return mCoordinateX;
    }

    public Integer getCoordinateY() {
        return mCoordinateY;
    }
}
