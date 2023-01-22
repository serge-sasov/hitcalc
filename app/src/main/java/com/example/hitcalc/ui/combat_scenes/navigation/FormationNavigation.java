package com.example.hitcalc.ui.combat_scenes.navigation;

import com.example.hitcalc.ui.combat_scenes.map.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FormationNavigation {
    private ArrayList<Map> mMaps; //list of battle scene map configurations
    private String mFormationTitle; //used formation for given list of battle scenes
    private Integer mFragmentIndex = 0; //a pointer to active fragment user navigated to (by default has zero value)

    private static final String MAPS = "nav_maps";
    private static final String FORMATION = "nav_formation";
    private static final String INDEX = "index_formation";


    public FormationNavigation(String formationTitle){
        mMaps = new ArrayList<Map>();
        mFormationTitle = formationTitle;
    }

    //JSON Constructor
    public FormationNavigation(JSONObject jo) throws JSONException {
        mFormationTitle = jo.getString(FORMATION);
        // Get an array in JSON format
        JSONArray jmapArray = jo.getJSONArray(MAPS);

        //Build a list of battle scene from JSON string
        mMaps = new ArrayList<Map>();
        for (int i = 0; i < jmapArray.length(); i++) {
            mMaps.add(new Map(jmapArray.getJSONObject(i)));
        }

        //Restore fragment index of the formation
        if(jo.has(INDEX)){
            mFragmentIndex = jo.getInt(INDEX);
        }
    }

    /*
     When user switches between different army and formations battle scene sets the current fragment
     index of the last formation need to be stored to be able to restore it when user navigates
     to the formation once again.
     */
    public void setFragmentIndex(Integer index){
        mFragmentIndex = index;
    }

    public ArrayList<Map> getMaps() {
        return mMaps;
    }

    public String getFormation() {
        return mFormationTitle;
    }

    //only this property shall be configurable once instantiated
    public void setMaps(ArrayList<Map> maps) {
        mMaps = maps;
    }

    public Integer getFragmentIndex(){
        return mFragmentIndex;
    }

    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {

        JSONObject jo = new JSONObject();
        // Make an array in JSON format
        JSONArray jMapArray = new JSONArray();


        // And load it with the notes
        for (Map map : mMaps) {
            if (map != null) {
                jMapArray.put(map.convertToJSON());
            }
        }

        jo.put(MAPS, jMapArray); //Put array of battle scenes
        jo.put(FORMATION, mFormationTitle); //put formation the battle scenes belongs to
        jo.put(INDEX, mFragmentIndex);
        return jo;
    }
}
