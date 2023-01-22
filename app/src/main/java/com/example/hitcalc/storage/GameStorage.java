package com.example.hitcalc.storage;

import android.content.Context;
import android.util.Log;

import com.example.hitcalc.ui.combat_scenes.army.Scenario;
import com.example.hitcalc.ui.combat_scenes.navigation.ScenarioNavigation;
import com.example.hitcalc.ui.combat_scenes.map.Map;
import com.example.hitcalc.ui.turns_and_rounds.game.Game;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/*
* Save game data into file and recover it on demand
* */
public class GameStorage {
    private Game mGame; //keep stored the info about current game
    private Scenario mScenario;
    private String mAttackingCivil;
    private String mFormationTitle; //  attacking formation title
    private ScenarioNavigation mScenarioNavigation;
    //keep the backup of the configuration in the file
    private String mFileName;
    private Random mRandomDiceRoll = new Random();

    private LoadTable mShockResultAttackerTable,
            mShockResultDefenderTable,
            mFrontAttackTable,
            mFlankRearAttackTable,
            mUnitActionsTable,
            mUnitWeaknessTable;

    public GameStorage(Scenario scenario, String fileName) throws JSONException {
        mScenario = scenario;
        mFileName = fileName;
    }

    public GameStorage(Scenario scenario,
                       String attackingCivil,
                       String formationTitle,
                       ScenarioNavigation scenarioNavigation) throws JSONException {

        mScenario = scenario;
        mAttackingCivil = attackingCivil;
        mFormationTitle = formationTitle;
        mScenarioNavigation = scenarioNavigation;
    }

    //Create a copy of the own class
    public GameStorage(GameStorage instance) throws JSONException {
        mScenario = instance.scenario();
        mAttackingCivil = instance.getAttackingCivil();
        mFormationTitle = instance.getFormationTitle();
        mScenarioNavigation = instance.getScenarioNavigation();
        mFileName = instance.getFileName();

        mFrontAttackTable = instance.getFrontAttackTable();
        mFlankRearAttackTable = instance.getFlankRearAttackTable();
        mShockResultAttackerTable = instance.getShockResultAttackerTable();
        mShockResultDefenderTable = instance.getShockResultDefenderTable();

        //Weakness & Activations
        mUnitActionsTable = instance.getUnitActionsTable();
        mUnitWeaknessTable = instance.getUnitWeaknessTable();
    }


    //JSON Constructor
    public GameStorage(JSONObject jo) throws JSONException {
       decodeJSON(jo);
    }

    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {

        JSONObject jo = new JSONObject();
        // Make an array in JSON format
        jo.put("SCENARIO_JSON", mScenario.convertToJSON().toString());
        jo.put("BACKUP_FILE_NAME", mFileName);

        if(mGame != null) {
            jo.put("GAME", mGame.convertToJSON());
        }

        return jo;
    }

    //JSON Constructor
    private void decodeJSON(JSONObject jo) throws JSONException {
        //recover backup file name
        if (jo.has("BACKUP_FILE_NAME")) {
            mFileName = jo.getString("BACKUP_FILE_NAME");
        }

        if (jo.has("SCENARIO_JSON")) {
            String scenario = jo.getString("SCENARIO_JSON");

            try {
                //Get scenario object from JSON
                JSONObject jsonObject = new JSONObject(scenario);
                mScenario = new Scenario(jsonObject);

            } catch (JSONException e) {
                Log.d("GameInstance", "PARSING-EXCEPTION:", e);
            }
        }

        if (jo.has("GAME")) {
            mGame = new Game(jo.getJSONObject("GAME"), this);
        }
    }

    public Scenario scenario() {
        if(mScenario != null) {
            return mScenario;
        }

        return null;
    }

    public ScenarioNavigation getScenarioNavigation() {
        if(mScenarioNavigation != null) {
            return mScenarioNavigation;
        }

        return null;
    }

    public String getAttackingCivil() {
        if(mAttackingCivil != null) {
            return mAttackingCivil;
        }

        return null;
    }

    public String getFormationTitle() {
        if(mFormationTitle != null) {
            return mFormationTitle;
        }

        return null;
    }

    public String getFileName() {
        if(mFileName != null) {
            return mFileName;
        }

        return null;
    }

    public void setFormationTitle(String formationTitle) {
        mFormationTitle = formationTitle;
    }

     /*
     * return all available files in the root application directory
     * */
    public File[] getFiles(Context context) {
        //path = Environment.getDataDirectory().toString();
        String path = context.getFilesDir().toString();
        //path = Environment.getRootDirectory().toString();
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();

        //Need to implement name sorting
        //Arrays.sort(files, Collections.reverseOrder());
        Arrays.sort(files);

        Log.d("Files", "Size: " + files.length);

        return files;
    }

    /*
     * Save configuration of the instance into file
     * */
    public void save(Context context, String prefix)
            throws IOException, JSONException {
        // Now write it to the private disk space of our app
        Writer writer = null;

        try {
            if (mFileName != null && prefix != null) {
                OutputStream out = context.openFileOutput(prefix +"_"+ mFileName,
                        context.MODE_PRIVATE);

                writer = new OutputStreamWriter(out);
                writer.write(this.convertToJSON().toString());
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /*
     * Save configuration of the instance into file
     * */
    public void save(Context context)
            throws IOException, JSONException {
        // Now write it to the private disk space of our app
        Writer writer = null;

        try {
            if (mFileName != null) {
                OutputStream out = context.openFileOutput(mFileName,
                        context.MODE_PRIVATE);

                writer = new OutputStreamWriter(out);
                writer.write(this.convertToJSON().toString());
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /*
    * Load saved game instance
    * */
    public void load(Context context, String fileName) throws IOException, JSONException {
        //Save old file name to reuse it after data reload
        String oldFileName = mFileName;

        InputStream in = context.openFileInput(fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder jsonString = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {
            jsonString.append(line);
        }

        JSONObject objectJSON = (JSONObject) new
                JSONTokener(jsonString.toString()).nextValue();

        decodeJSON(objectJSON);

        //Restore the locally persisted file name
        mFileName = oldFileName;
    }

    public LoadTable getFrontAttackTable() {
        return mFrontAttackTable;
    }

    public LoadTable getFlankRearAttackTable() {
        return mFlankRearAttackTable;
    }

    public LoadTable getShockResultAttackerTable() {
        return mShockResultAttackerTable;
    }

    public LoadTable getShockResultDefenderTable() {
        return mShockResultDefenderTable;
    }

    public LoadTable getUnitActionsTable() {
        return mUnitActionsTable;
    }

    public LoadTable getUnitWeaknessTable() {
        return mUnitWeaknessTable;
    }

    public void setCombatTables(HashMap<String, InputStream> tables) throws IOException, CsvException {
        mFrontAttackTable = new LoadTable(tables.get("front_attacks"));
        mFlankRearAttackTable = new LoadTable(tables.get("flank_rear_attacks"));
        mShockResultAttackerTable = new LoadTable(tables.get("attacker_shock_results"));
        mShockResultDefenderTable = new LoadTable(tables.get("defender_shock_results"));

        //Weakness & Activations
        mUnitActionsTable = new LoadTable(tables.get("unit_actions"));
        mUnitWeaknessTable = new LoadTable(tables.get("unit_weakness"));
    }

    public Game game() {
        return mGame;
    }

    public void setGame(Game game) {
        mGame = game;
    }

    public Random diceRoll() {
        return mRandomDiceRoll;
    }
}
