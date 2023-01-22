package com.example.hitcalc.ui.combat_scenes.army;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
 * Army -> Formation 0
 *      -> Formation 1
 *      -> Formation 2
 *          ...
 *      -> Formation m
 * */
public class Army {
    protected ArrayList<Formation> mArmy; //set of Formations which the army consists of
    protected String mCivilizationTitle; //A civilisation name the army belongs to

    // JSON constants
    protected static final String ARMY = "army";
    protected static final String CIVILIZATION_TITLE = "civilization_title";

    public Army(String civil){
        mArmy = new ArrayList<Formation>();
        mCivilizationTitle = civil;
    }

    //JSON Constructor
    public Army(JSONObject jo) throws JSONException {
        if(jo.has(CIVILIZATION_TITLE)) {
            mCivilizationTitle = jo.getString(CIVILIZATION_TITLE);
        }

        parseJSONArmy(jo);
    }

    //used as separated method to make overriding possible
    protected void parseJSONArmy(JSONObject jo) throws JSONException {
        //Get all formations and put them into army array
        JSONArray jArmyArray = jo.getJSONArray(ARMY);
        mArmy = new ArrayList<Formation>();

        for (int i = 0; i < jArmyArray.length(); i++) {
            mArmy.add(new Formation(jArmyArray.getJSONObject(i)));
        }
    }

    /*
    * Create a clone of existing army instance
    *
     */
    public Army() {
    }


    public void addFormation(Formation formation){
        mArmy.add(formation);
    }

    public ArrayList<Formation> getFormations(){
        return mArmy;
    }

    public String getCivilizationTitle(){
        return mCivilizationTitle;
    }

    /*
    *       Return formation by given warrior
    */
    public Formation getFormationByWarrior(WarriorInShock warrior){
        for(Formation formation:mArmy){
            if(formation.checkForWarriorPresence(warrior)){
                return formation;
            }
        }

        return null;
    }

    //return formation by given formation title
    public Formation getFormationByTitle(String title){
        for(Formation formation:mArmy){
            if((formation.getTitle() != null && formation.getTitle().equals(title)) ||
                    (formation.getLeader() != null && formation.getLeader().equals(title))){
                return formation;
            }
        }
        return null;
    }

    /*
    * Return a list of Formation leaders or Titles if there is no leader, otherwise null
    * */
    public ArrayList<String> getFormationLeaders(){
        ArrayList<String> leaders = new ArrayList<String>();
        for(Formation formation : mArmy){
            if(formation.getLeader() != null) {
                //first search for leaders
                leaders.add(formation.getLeader());
            }else if(formation.getTitle() != null){
                //if no leader is available search for title for combined formations
                leaders.add(formation.getTitle());
            }
        }
        return leaders;
    }

    /*
     * Return a list of Formation leaders or Titles if there is no leader, otherwise null
     * */
    public ArrayList<String> getFormationNames(){
        ArrayList<String> leaders = new ArrayList<String>();
        for(Formation formation : mArmy){
            if(formation.getTitle() != null){
                //if no leader is available search for title for combined formations
                leaders.add(formation.getTitle());
            }
            else if(formation.getLeader() != null) {
                //first search for leaders
                leaders.add(formation.getLeader());
            }
        }
        return leaders;
    }

    //get formation object using the warrior object
    public Formation deriveFormationByWarrior(WarriorItem warrior){
        return deriveFormation(mArmy, warrior);
    }

    //derive formation using army and warrior
    private Formation deriveFormation(ArrayList<Formation> formations, WarriorItem warrior){
        if(formations != null){
            for(Formation formation : formations){
                if(formation.warriors() != null){
                    if(formation.warriors().contains(warrior)){
                        return formation;
                    }
                }

                if(formation.subFormations() != null){
                    ArrayList<Formation> subFormations = new ArrayList<Formation>();
                    for(Formation subFormation : formation.subFormations().values()){
                        subFormations.add(subFormation);
                    }
                    Formation result = deriveFormation(subFormations, warrior);
                    if(result != null){
                        return result;
                    }
                }
            }
        }

        return null;
    }

    // ------------------------- JSON Conversion ---------------------------
    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {

        JSONObject jo = new JSONObject();
        // Make an array in JSON format
        JSONArray jArmyArray = new JSONArray();

        // And load it with the notes
        for (Formation formation : mArmy)
            jArmyArray.put(formation.convertToJSON());

        jo.put(ARMY, jArmyArray); //Put array of warriors to JSON format
        jo.put(CIVILIZATION_TITLE, mCivilizationTitle);
        return jo;
    }
}
