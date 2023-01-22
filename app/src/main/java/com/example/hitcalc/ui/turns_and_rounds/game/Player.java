package com.example.hitcalc.ui.turns_and_rounds.game;

import com.example.hitcalc.ui.combat_scenes.army.Army;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.TurnAbstractAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
* Player data structure
* */
public class Player {
    private String mPlayerTitle;
    private ArmyInCombat mArmy;
    private Integer mObjectId = null;

    //-------------------- JSON -------------------------------------
    private static final String PLAYER_TITLE = "player_title";
    private static final String ARMY = "ARMY";
    private static final String PLAYER_OBJECT_ID = "player_object_id";
    //-------------------- JSON -------------------------------------

    /**************** JSON Constructor *************************/
    public Player(JSONObject jo, UnitWeaknessTable table) throws JSONException {
        if(jo.has(PLAYER_TITLE)) {
            mPlayerTitle = jo.getString(PLAYER_TITLE);
        }

        if(jo.has(ARMY)) {
            mArmy = new ArmyInCombat(jo.getJSONObject(ARMY), table);
        }

        if(jo.has(PLAYER_OBJECT_ID)) {
            mObjectId = jo.getInt(PLAYER_OBJECT_ID);
        }
    }
    /**************** JSON Constructor *************************/

    public Player(String title, ArmyInCombat army){
        mPlayerTitle = title;
        mArmy = army;
    }

    //show given player title
    public String title(){
        return mPlayerTitle;
    }

    //get object od of the given player instance
    public Integer id() {
        return mObjectId;
    }

    public ArmyInCombat army(){
        return mArmy;
    }

    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        // Make an array in JSON format
        jo.put(PLAYER_TITLE, mPlayerTitle);
        jo.put(PLAYER_OBJECT_ID, this.hashCode());
        jo.put(ARMY, mArmy.convertToJSON());

        return jo;
    }
}
