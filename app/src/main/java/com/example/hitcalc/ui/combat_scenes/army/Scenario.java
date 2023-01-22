package com.example.hitcalc.ui.combat_scenes.army;

import com.example.hitcalc.ui.combat_scenes.army.parser.ArmyParser;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
* Scenario -> Army 1
* Scenario -> Army 2
* */
public class Scenario {
    private HashMap<String, Army> mArmyMap;
    private ArrayList<String> mCivilList;

    // JSON constants
    private static final String ARMY_MAP = "army_map";
    private static final String CIVIL_LIST = "civil_list";

    //JSON Constructor
    public Scenario(JSONObject jo) throws JSONException {

        //Get all formations and put them into army array
        JSONArray jArmyMapArray = jo.getJSONArray(ARMY_MAP);
        JSONArray jCivilArray = jo.getJSONArray(CIVIL_LIST);

        mArmyMap = new HashMap<String, Army>();
        mCivilList = new ArrayList<String>();

        //Populate attributes for army and civil lists
        for (int i = 0; i < jCivilArray.length(); i++) {
            mArmyMap.put(jCivilArray.getString(i),new Army(jArmyMapArray.getJSONObject(i)));
            mCivilList.add(jCivilArray.getString(i));
        }
    }

    public Scenario(LoadTable scenario, LoadTable unitTable) throws IOException, CsvException {
        //Table format: civilization,leader,unit,unit,unit...
        List<String []> table = scenario.getTable();

        //split into 2 army arrays
        String civil, leader;
        String [] row;
        ArrayList<String> formationArray = new ArrayList<String>();
        mArmyMap = new  HashMap<String, Army>();
        mCivilList = new ArrayList<String>();

        for(int i=1; i < table.size(); i++){
            row = table.get(i); //formation row of particular army
            civil = row[0]; //get civilisation title
            leader = row[1]; //get leader

            //Create army object for each civilisation
            if(mArmyMap.get(civil) == null) {
                mCivilList.add(civil);
                //Create army object for each civilisation
                mArmyMap.put(civil, new Army(civil));
            }

            //Delete all elements of previous formation
            formationArray.clear();

            //Build new formation array without the first element
            for(int j = 1; j < row.length; j++){
                if(row[j].length()>0){
                    formationArray.add(row[j]);
                }
            }

            //Create a formation object
            Formation formationObject = new Formation(unitTable, leader, GetStringArray(formationArray)); //need to convert ArrayList<String> -> String[]

            //Add formation to its army
            mArmyMap.get(civil).addFormation(formationObject);
        }
    }

    //Make Scenario out of parsed scenario data
    public Scenario(ArmyParser armyParser){
        mArmyMap = new  HashMap<String, Army>();
        mCivilList = armyParser.getCivilList();

        for (String civil : mCivilList) {
            //Create army object for each civilisation
            if (mArmyMap.get(civil) == null) {
                mArmyMap.put(civil, new Army(civil));
            }
        }

        //a list of pre-parsed formations
        //HashMap<String, Formation> formationMap = armyParser.getScenarioFormations();
        //Set<String> FormationTitles = formationMap.keySet();

        //get Formations to given Civil
        for (String civil : mCivilList) {
            ArrayList<Formation> formationList = armyParser.getFormationTitlesOfGivenCivil(civil);
            //populate each army with its formations
            for (Formation formation : formationList) {
                //Add the formation only if it is of a standalone type
                if (formation.isStandalone()) {
                    //add formation to the given civilization army
                    mArmyMap.get(civil).addFormation(formation);
                }

            }
        }
     }


    // Function to convert ArrayList<String> to String[]
    public static String[] GetStringArray(ArrayList<String> arr)
    {

        // declaration and initialise String Array
        String str[] = new String[arr.size()];

        // ArrayList to Array Conversion
        for (int j = 0; j < arr.size(); j++) {

            // Assign each value to String array
            str[j] = arr.get(j);
        }

        return str;
    }

    public ArrayList<String> getCivilizations(){
        return mCivilList;
    }

    public HashMap<String, Army> getArmies(){
        return mArmyMap;
    }

    public Army getArmy(String civil){
        return mArmyMap.get(civil);
    }

    //rebuild reference to the army mapping to be able to update the info regarding the removed formations & warriors
    public void update(HashMap<String, Army> armyMap){
        mArmyMap = armyMap;
    }

    // ------------------------- JSON Conversion ---------------------------
    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {

        JSONObject jo = new JSONObject();
        // Make an array in JSON format
        JSONArray jCivilArray = new JSONArray();
        JSONArray jArmyMapArray = new JSONArray();
        // And load it with the notes
        for (String civil : mCivilList) {
            jCivilArray.put(civil); //Put list of civilisations
            jArmyMapArray.put(mArmyMap.get(civil).convertToJSON()); //put list of armies
        }

        jo.put(ARMY_MAP, jArmyMapArray); //Put array of warriors to JSON format
        jo.put(CIVIL_LIST, jCivilArray);
        return jo;
    }
}
