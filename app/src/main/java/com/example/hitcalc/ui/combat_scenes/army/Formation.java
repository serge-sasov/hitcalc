package com.example.hitcalc.ui.combat_scenes.army;

import android.util.Log;

import com.example.hitcalc.utility.ParseTable;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * Formation
 *    -> Leader
 *    -> Formation Title
 *    -> Seizure Points
 *    -> Warriors[0..n]
 *    -> Sub-Formations[0..k]
 *    -> Sub-FormationsTitles[0..k]
 * */

public class Formation{
    protected ArrayList<WarriorItem> mWarriors;
    protected String [] mFormationMembers;
    protected String mLeader;
    protected String mFormationTitle;
    protected boolean mStandalone = false;
    protected HashMap<String, Formation> mSubFormations; //Formation Title/Formation
    protected ArrayList<String> mSubFormationTitles; //Formation Title/Formation
    //Possibility to be chosen with parent formation unit. If not, at the same time can be used unit from one selected formation.
    protected boolean mAssociationWithParentFormation = true; // <- applicable only for subFormations
    protected Integer mSeizurePoints; //amount of available seizure points

    // JSON constants
    protected static final String WARRIORS = "warriors";
    protected static final String FORMATION_MEMBERS = "formation_members";
    protected static final String FORMATION_TITLE = "formation_title";
    protected static final String LEADER = "leader";
    protected static final String SEIZURE_POINTS = "seizure_points";
    protected static final String ASSOCIATION = "association";
    protected static final String SUB_FORMATIONS = "sub_formations";
    protected static final String SUB_FORMATION_TITLES = "sub_formation_titles";
    protected static final String STANDALONE = "standalone";

    /*
    public Formation(ArrayList<WarriorItem> warriors, String leader, String formationTitle, boolean standalone){
        instantiateAttributes(warriors, leader, formationTitle, standalone);
    }
     */

    //Use that constructor to add seizure points
    public Formation(ArrayList<WarriorItem> warriors, String leader, String formationTitle, boolean standalone, Integer seizurePoints){
        if(seizurePoints != null) {
            mSeizurePoints = seizurePoints;
        }
        mWarriors = warriors;
        mLeader = leader;
        mFormationTitle = formationTitle;
        mStandalone = standalone;

        //populate formation members
        mFormationMembers = new String [mWarriors.size()];
        for(int i =0; i<mWarriors.size(); i++){
            mFormationMembers[i] = mWarriors.get(i).title();
        }
    }

    /************************* Clone Formations ********************************/
    /*
     * Clone parent formation only
    */
    public Formation(){
    }

    /*
     * Clone Formation
    */
    public Formation(Formation oldParentFormation, Army army){
    }

    /**************** JSON Constructor *************************/
    public Formation(JSONObject jo) throws JSONException {
        if(jo.has(LEADER)) {
            mLeader = jo.getString(LEADER);
        }

        if(jo.has(SEIZURE_POINTS)) {
            mSeizurePoints = jo.getInt(SEIZURE_POINTS);
        }


        if(jo.has(FORMATION_TITLE)){
            mFormationTitle = jo.getString(FORMATION_TITLE);
        }

        if(jo.has(FORMATION_MEMBERS)) {
            // Get an array in JSON format
            JSONArray jMembersArray = jo.getJSONArray(FORMATION_MEMBERS);

            //Create new formation member array and populate it
            mFormationMembers = new String[jMembersArray.length()];

            for (int i = 0; i < jMembersArray.length(); i++) {
                mFormationMembers[i] = jMembersArray.getString(i);
            }
        }

        if(jo.has(ASSOCIATION)){
            mAssociationWithParentFormation = jo.getBoolean(ASSOCIATION);
        }

        if(jo.has(STANDALONE)){
            mStandalone = jo.getBoolean(STANDALONE);
        }

        // parse out warriors & sub-formations
        parseJSONWarriors(jo);
        parseJSONSubFormations(jo);
    }

    //used as separated method to make overriding possible
    protected void parseJSONWarriors(JSONObject jo) throws JSONException {
        if(jo.has(WARRIORS)) {
            JSONArray jWarriorsArray = jo.getJSONArray(WARRIORS);
            mWarriors = new ArrayList<WarriorItem>();

            for (int i = 0; i < jWarriorsArray.length(); i++) {
                mWarriors.add(new WarriorItem(jWarriorsArray.getJSONObject(i)));
            }
        }
    }

    //used as separated method to make overriding possible
    protected void parseJSONSubFormations(JSONObject jo) throws JSONException {
        //Get all formations and put them into army array
        if(jo.has(SUB_FORMATIONS)) {
            JSONArray jSubFormationsArray = jo.getJSONArray(SUB_FORMATIONS);
            JSONArray jSubTitlesArray = jo.getJSONArray(SUB_FORMATION_TITLES);

            mSubFormations = new HashMap<String, Formation>();
            mSubFormationTitles = new ArrayList<String>();

            //Populate attributes for army and civil lists
            for (int i = 0; i < jSubTitlesArray.length(); i++) {
                mSubFormations.put(jSubTitlesArray.getString(i), new Formation(jSubFormationsArray.getJSONObject(i)));
                mSubFormationTitles.add(jSubTitlesArray.getString(i));
            }
        }
    }


    public Formation(LoadTable table, String leader,String [] formation) throws CsvException, IOException {
        String [] unit;

        mFormationMembers = formation;
        mLeader = leader;

        //Create warriors
        ParseTable config = new ParseTable(table);
        mWarriors = new ArrayList<WarriorItem>();

        //Build up an array of Warrior Items belonging to the formation
        for(int i =0; i < formation.length; i++){
            //Title is used as a key for search for Type,Quality,Size,Movement,Property,Army,TwoHex
            unit = config.getRowByRowTitle(formation[i]);

            //Create object if units actually exists
            if(unit != null) {
                String title, type;
                Integer tq, size, mov, legioId;
                String property, addProperty, civilization, color;
                boolean isTwoHex;

                //parse out the input string derived from unit table
                title = unit[0];
                type = unit[1];
                tq = getInegerValue(unit[2]);
                size = getInegerValue(unit[3]);
                mov = getInegerValue(unit[4]);
                property = unit[5];
                addProperty = unit[6];
                civilization = unit[7];
                isTwoHex = config.convertToBoolean(unit[8]);
                color = unit[9];
                legioId = getInegerValue(unit[10]);

                mWarriors.add(new WarriorItem(title, type, tq, size, mov, property, addProperty, civilization, isTwoHex, color, legioId));
            }
            else{
                //notify user that a unit was not found
                Log.w("Class.Formation", "Unit not found: " + formation[i]);

            }
        }
    }


    //Add sub formation with the association flag determining possibility to use units with parent formation
    public void addSubFormation(Formation formation, boolean association){
        if(mSubFormations == null){
            mSubFormations = new  HashMap<String, Formation>();
            mSubFormationTitles = new ArrayList<String>();
        }

        formation.setAsociationWithParentFormation(association);
        String title = formation.getTitle();

        mSubFormations.put(title, formation);
        mSubFormationTitles.add(title);
    }

    public HashMap<String, Formation> subFormations(){
        if(mSubFormations != null) {
            return mSubFormations;
        }
        return null;
    }


    public void setAsociationWithParentFormation(boolean association){
        mAssociationWithParentFormation = association;
    }


    public String getTitle(){
        return mFormationTitle;
    }

    //Return a leader name if available otherwise given title.
    public String getLeaderOrFormationTitle(){
        if(mFormationTitle != null){
            return mFormationTitle;
        }

        return mLeader;
    }

    //clear any subformation entities to avoid recursion call at JSON transformation
    private void clearSubFormations(){
        mSubFormations = null;
        mSubFormationTitles = null;
    }

    //Use function to transform null string into null or real velue if any provided
    protected Integer getInegerValue(String value){
        Integer result = null;

        if(value.length() > 0) {
            result = Integer.parseInt(value);
        }
        return result;
    }

    public String [] formationTitles(){
        return mFormationMembers;
    }

    public String getLeader(){
        return mLeader;
    }

    public ArrayList<WarriorItem> warriors(){
        return mWarriors;
    }

    public boolean isStandalone(){
        return mStandalone;
    }

    //Check if the given warrior is a member of the current formation
    public boolean checkForWarriorPresence(WarriorItem warrior){
        for(int index=0; index < mFormationMembers.length; index++){
            if(mFormationMembers[index].equals(warrior.title())){
                return true;
            }
        }

        return false;
    }

    //return a warrior object on provided warrior title
    public WarriorItem getWarriorItem(String title){
        for(int i =0; i < mFormationMembers.length; i++){
            if(title.equals(mFormationMembers[i])){
                return mWarriors.get(i);
            }
        }
        return null;
    }

    public String [] getFormationMembers() {
        return mFormationMembers;
    }

    public Integer getSeizurePoints(){
        return mSeizurePoints;
    }

    // ------------------------- JSON Conversion ---------------------------
    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        // Make an array in JSON format
        JSONArray jArray = null;

        if(mWarriors != null) {
            jArray = new JSONArray();
            // And load it with the notes
            for (WarriorItem warrior : mWarriors) {
                jArray.put(warrior.convertToJSON());
            }

            jo.put(WARRIORS, jArray); //Put array of warriors to JSON format
        }

        if(mFormationMembers != null){
            jArray = new JSONArray();
            for (String member : mFormationMembers) {
                jArray.put(member);
            }
            jo.put(FORMATION_MEMBERS, jArray); //Put array of formation members
        }

        if(mSeizurePoints != null) {
            jo.put(SEIZURE_POINTS, mSeizurePoints);
        }

        if(mLeader != null){
            jo.put(LEADER, mLeader);
        }


        jo.put(FORMATION_TITLE, mFormationTitle);
        jo.put(ASSOCIATION, mAssociationWithParentFormation);
        jo.put(STANDALONE, mStandalone);

        //---------------------- Sub Formation JSON parsing -----------------------------------------
        if(mSubFormations != null && mSubFormations.size() > 0){
            // Make an array in JSON format
            JSONArray jSubFormationsArray = new JSONArray();
            JSONArray jSubTitlesArray = new JSONArray();

            // And load it with the notes
            for (String title : mSubFormations.keySet()) {
                jSubTitlesArray.put(title); //Put list of civilisations
                jSubFormationsArray.put(mSubFormations.get(title).convertToJSON()); //put list of armies
            }

            jo.put(SUB_FORMATIONS, jSubFormationsArray);
            jo.put(SUB_FORMATION_TITLES, jSubTitlesArray);
        }
        //---------------------------------------------------------------
        return jo;
    }
}