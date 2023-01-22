package com.example.hitcalc.ui.combat_scenes.navigation;

import com.example.hitcalc.ui.combat_scenes.army.Scenario;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ScenarioNavigation {
    private HashMap<String, ArmyNavigation> mArmyNavList; //list of battle scene map configurations
    private ArrayList<String> mCivilizationList;

    private static final String NAVIGATIONS = "army_nav_list";
    private static final String TITLES = "civil_nav_list";

    public ScenarioNavigation(Scenario scenario){
        mArmyNavList = new HashMap<String, ArmyNavigation>();
        mCivilizationList = scenario.getCivilizations();;

        //<- put here creation of formation title list (implement it in the Army class)!!!!
        for(String civil : mCivilizationList){
            ArrayList<String> formationLeaders = scenario.getArmy(civil).getFormationNames();
            mArmyNavList.put(civil, new ArmyNavigation(formationLeaders));
        }
    }

    //JSON Constructor
    public ScenarioNavigation(JSONObject jo) throws JSONException {

        //Get all formations and put them into army array
        JSONArray jArmyNavArray = jo.getJSONArray(NAVIGATIONS); // <- may be a null object
        JSONArray jArmyTitleArray = jo.getJSONArray(TITLES);

        mArmyNavList = new HashMap<String, ArmyNavigation>();
        mCivilizationList = new ArrayList<String>();

        //Populate attributes for army and civil lists
        for (int i = 0; i < jArmyTitleArray.length(); i++) {
            if(!jArmyNavArray.isNull(i)) {
                //Create the object only for those formations, which have configured their own battle scenes
                mArmyNavList.put(jArmyTitleArray.getString(i), new ArmyNavigation(jArmyNavArray.getJSONObject(i)));
            }
            mCivilizationList.add(jArmyTitleArray.getString(i));
        }
    }

    //Return child object to access a required method to use
    public ArmyNavigation getArmyNavigation(String civil){
        return mArmyNavList.get(civil);
    }

    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        // Make an array in JSON format
        JSONArray jArmyNavArray = new JSONArray();
        JSONArray jCivilTitleArray = new JSONArray();

        // put into arrays formation titles and battle scene configurations
        for (String civilTitle: mCivilizationList){
            ArmyNavigation formationNavigation = mArmyNavList.get(civilTitle);
            if (formationNavigation != null) {
                jArmyNavArray.put(formationNavigation.convertToJSON());
            }
            jCivilTitleArray.put(civilTitle);
        }

        jo.put(NAVIGATIONS, jArmyNavArray); //Put array of battle scenes
        jo.put(TITLES, jCivilTitleArray); //put formation the battle scenes belongs to
        return jo;
    }
}
