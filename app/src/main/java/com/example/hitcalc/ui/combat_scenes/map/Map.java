package com.example.hitcalc.ui.combat_scenes.map;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Map {
    private   Hex [][] mHexMap; //Array of hexes
    private String mAttackerCiv, mDefenderCiv; //Selected by user civilizations as attacker's and a defender's one
    private Integer mTerrainModifier = 0;

    // ------------- Used Formation & Warriors ---------------
    private HashMap<FormationActivated, ArrayList<WarriorInShock>> mFormations; // permanent table
    private HashMap<FormationActivated, WarriorInShock> mTmpFormationWarrior; //temp table

    // JSON constants
    private static final String HEX_MAP = "hex_map";
    private static final String HEX_ROW = "hex_row";
    private static final String MAP_ATTACKER_CIVIL = "map_attacker_civil";
    private static final String MAP_DEFENDER_CIVIL = "map_defender_civil";
    private static final String TERRAIN_MODIFIER = "map_terrain_modifier";

    //Array of used hex types - front, flank, rear and defender
    private static String [][] mHexTypes =
            {{"Front","Front","Front"},
             {"Flank","Defender","Defender","Flank"},
             {"Rear","Rear","Rear"}};

    //Cell Ids array
    private static int [][] mHexViewIds =
            {{R.id.hex00,R.id.hex01,R.id.hex02},
                    {R.id.hex10,R.id.hex11,R.id.hex12,R.id.hex13},
                    {R.id.hex20,R.id.hex21,R.id.hex22}};


    public Map(){
        prepareMap();
    }

    //create a new hex mapping
    protected void prepareMap() {
        prepareMap(null);
    }

    //copy existing hex mapping
    protected void prepareMap(Hex [][] hexMap){
        //Create an array of strings
        mHexMap = new Hex[mHexTypes.length][];

        for(int i = 0; i < mHexTypes.length ;i++ ){
            //create an array of rows with different number of elements
            mHexMap[i] = new Hex[mHexTypes[i].length];

            for(int j = 0; j < mHexTypes[i].length ;j++ ){
                if(hexMap != null) {
                    //copy a hex object
                    mHexMap[i][j] = new Hex(hexMap[i][j]);
                }
                else{
                    //make a hex object
                    mHexMap[i][j] = new Hex(mHexTypes[i][j], mHexViewIds[i][j],i,j);
                }
            }
        }
    }

    //JSON Constructor
    public Map(JSONObject jo) throws JSONException {
        //Get all formations and put them into army array
        JSONArray jrowMapArray = jo.getJSONArray(HEX_MAP);

        mHexMap = new Hex[jrowMapArray.length()][];
        for (int y = 0; y < jrowMapArray.length(); y++) {
            JSONArray jitemsArray = jrowMapArray.getJSONArray(y);

            mHexMap[y] = new Hex[jitemsArray.length()];
            for (int x = 0; x < jitemsArray.length(); x++) {
                mHexMap[y][x] = new Hex(jitemsArray.getJSONObject(x));
            }
        }

        if(!jo.isNull(MAP_ATTACKER_CIVIL)) {
            //If not null, populate the warrior object
            mAttackerCiv = jo.getString(MAP_ATTACKER_CIVIL);
        }

        if(!jo.isNull(MAP_DEFENDER_CIVIL)) {
            //If not null, populate the warrior object
            mDefenderCiv = jo.getString(MAP_DEFENDER_CIVIL);
        }

        mTerrainModifier = jo.getInt(TERRAIN_MODIFIER);
    }

    public static String[][] getHexTypes() {
        return mHexTypes;
    }



    public static int[][] getHexViewIds() {
        return mHexViewIds;
    }

    //JSON support
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();

        if(mHexMap != null) {
            // Make an array in JSON format
            JSONArray jHexVerticalArray = new JSONArray();
            for (int i = 0; i < mHexMap.length; i++) {

                JSONArray jHexHorizontalArray = new JSONArray();
                //create an array of rows with different number of elements
                for (int j = 0; j < mHexMap[i].length; j++) {
                    //make a JSON array of all horizontal hexes
                    jHexHorizontalArray.put(mHexMap[i][j].convertToJSON());
                }
                //make a JSON array of all vertical hex rows
                jHexVerticalArray.put(jHexHorizontalArray);
            }
            jo.put(HEX_MAP, jHexVerticalArray);
        }

        if(mAttackerCiv != null && mDefenderCiv != null){
            jo.put(MAP_ATTACKER_CIVIL, mAttackerCiv);
            jo.put(MAP_DEFENDER_CIVIL, mDefenderCiv);
        }

        jo.put(TERRAIN_MODIFIER, mTerrainModifier);

        return jo;
    }

    //Put warrior on the given hex
    public void placeWarriorOnHex(WarriorInShock warrior, Integer hexX, Integer hexY){
        mHexMap[hexY][hexX].placeWarrior(warrior);
    }

    /*
    *
    * */
    public void removeWarriorFromHex(Integer hexX, Integer hexY){
        if(mHexMap[hexY][hexX] != null){
            mHexMap[hexY][hexX].removeWarrior();
        }
    }

    //retrieve warrior from the given hex
    public WarriorInShock retrieveWarriorFromHex(Integer hexX, Integer hexY){
        if(mHexMap[hexY][hexX] != null){
            //return a unit from the hex
            return mHexMap[hexY][hexX].getWarrior();
        }

        //if no unit is on the hex return null
        return null;
    }

    //Provide to the map view a value selected by user out of the main activity
    public void terrainModifier(Integer value){
        mTerrainModifier = value;
    }

    public Integer terrainModifier(){
        return mTerrainModifier;
    }

    //put a new warrior into mapping <formation, warriors>
    public void placeWarrior(FormationActivated formation, WarriorInShock warrior){
        //clean up any not processed temporal data first
        mTmpFormationWarrior = new HashMap<FormationActivated, WarriorInShock>();

        //place a warrior into it
        mTmpFormationWarrior.put(formation, warrior);
    }

    public HashMap<FormationActivated, WarriorInShock> placeWarrior(){
        return mTmpFormationWarrior;
    }


    //put a new warrior into mapping <formation, warriors>
    public void warrior(FormationActivated formation, WarriorInShock warrior){
        ArrayList<WarriorInShock> warriors;

        if(mFormations == null){
            mFormations = new HashMap<FormationActivated, ArrayList<WarriorInShock>>();
        }

        if(mFormations.get(formation) != null){
            warriors = mFormations.get(formation);
        }else{
            warriors = new ArrayList<WarriorInShock>();
        }
        warriors.add(warrior);

        mFormations.put(formation, warriors);
    }

    //get a mapping <formation, warriors>
    public HashMap<FormationActivated, ArrayList<WarriorInShock>> formations(){
        return mFormations;
    }

    //put warriors into activated list & clean up them afterwards
    public void erase() {
        if (mFormations != null) {
            //put warriors into activated list
            for (FormationActivated formation : mFormations.keySet()) {
                for (WarriorInShock warrior : mFormations.get(formation)) {
                    //activate the warrior
                    if(warrior.type().equals("Harizma") != true) {
                        //skip harizma from the activation
                        formation.activate(warrior);
                    }

                    //clean up any configuration made
                    warrior.leader(null);
                    warrior.harizma(null);
                    warrior.stokedWarrior(null);
                }
            }

            //clean up activated warriors from the input list
            mFormations = null;
        }
    }

    //remove a warrior along with any attached leaders and stoked warrior
    public void erase(WarriorInShock warrior){
        if(mFormations != null){
            for(Formation formation : mFormations.keySet()){
                if(mFormations.get(formation).contains(warrior)){

                    //remove warrior
                    mFormations.get(formation).remove(warrior);

                    //remove leader
                    if(warrior.leader() != null){
                        mFormations.get(formation).remove(warrior.leader());
                    }

                    //remove stocked warrior
                    if(warrior.stokedWarrior() != null) {
                        mFormations.get(formation).remove(warrior.stokedWarrior());
                    }
                }
            }
        }
    }
}
