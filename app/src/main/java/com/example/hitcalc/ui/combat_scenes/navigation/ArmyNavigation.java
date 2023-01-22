package com.example.hitcalc.ui.combat_scenes.navigation;

import com.example.hitcalc.ui.combat_scenes.map.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ArmyNavigation {
    private HashMap<String, FormationNavigation> mFormNavList; //list of battle scene map configurations
    private ArrayList<String> mFormationTitleList; //List of all formations consisting the given army
    private String mSelectedFormationTitle; //keeps a selected formation of the army if switches to another army view

    private static final String NAVIGATIONS = "formation_navs";
    private static final String TITLES = "formation_titles";
    private static final String FORMATION = "selected_formation_title";

    //Create an array of available formationTitles and populate it as new battle scene appears
    public ArmyNavigation(ArrayList<String> formationTitleList){
        mFormationTitleList = formationTitleList;
        mFormNavList = new HashMap<String, FormationNavigation>();

        //Add formations list to each army
        for(String title : mFormationTitleList){
            mFormNavList.put(title, new FormationNavigation(title));
        }
    }

    //JSON Constructor
    public ArmyNavigation(JSONObject jo) throws JSONException {

        //Get all formations and put them into army array
        JSONArray jFormNavArray = jo.getJSONArray(NAVIGATIONS); // <- may be a null object
        JSONArray jFormTitleArray = jo.getJSONArray(TITLES);

        mFormNavList = new HashMap<String, FormationNavigation>();
        mFormationTitleList = new ArrayList<String>();
        if (jo.has(FORMATION)) {
            mSelectedFormationTitle = jo.getString(FORMATION);
        }

        //Populate attributes for army and civil lists
        for (int i = 0; i < jFormTitleArray.length(); i++) {
            if(!jFormNavArray.isNull(i)) {
                //Create the object only for those formations, which have configured their own battle scenes
                mFormNavList.put(jFormTitleArray.getString(i), new FormationNavigation(jFormNavArray.getJSONObject(i)));
            }
            mFormationTitleList.add(jFormTitleArray.getString(i));
        }
    }

    //get access to the methods of the child object
    public FormationNavigation getFormationNavigation(String formationTitle){
        return mFormNavList.get(formationTitle);
    }

    public ArrayList<Map> getMapsOfFormationNavigation(String formationTitle){
        FormationNavigation formationNavigation = mFormNavList.get(formationTitle);
        if(formationNavigation != null){
            return formationNavigation.getMaps();
        }
        return null;
    }

    //Get Fragment Index of given Formation
    public Integer getFormationFragmentIndex(String formationTitle) {
        FormationNavigation formationNavigation = mFormNavList.get(formationTitle);
        return formationNavigation.getFragmentIndex();
    }

    //Store information about the selected formation of the given army
    public void setSelectedFormationTitle(String selectedFormationTitle) {
        mSelectedFormationTitle = selectedFormationTitle;
    }

    public String getSelectedFormationTitle() {
        if(mSelectedFormationTitle == null){
            //If no formation is selected choose the first out of the list
            return mFormationTitleList.get(0);
        }
        return mSelectedFormationTitle;
    }

    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        // Make an array in JSON format
        JSONArray jFormNavArray = new JSONArray();
        JSONArray jFormTitleArray = new JSONArray();

        // put into arrays formation titles and battle scene configurations
        for (String formationTitle: mFormationTitleList){
            FormationNavigation formationNavigation = mFormNavList.get(formationTitle);
            if (formationNavigation != null) {
                jFormNavArray.put(formationNavigation.convertToJSON());
            }
            jFormTitleArray.put(formationTitle);
        }

        jo.put(NAVIGATIONS, jFormNavArray); //Put array of battle scenes
        jo.put(TITLES, jFormTitleArray); //put formation the battle scenes belongs to

        if(mSelectedFormationTitle != null){
            jo.put(FORMATION, mSelectedFormationTitle);
        }

        return jo;
    }
}
