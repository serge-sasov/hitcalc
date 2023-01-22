package com.example.hitcalc.ui.turns_and_rounds.game.turn_actions;

import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.Player;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/*
* the class changes a player characteristics according to the
* action chosen. The actions are given via the child classes and defines
* how the data is to be treated. For this are given two basic methods ->
* apply() and rollback()
* */
public abstract class TurnAbstractAction {
    protected Player mPlayer;
    protected String mTitle;
    protected int mTitleId;
    protected String mColor;


    // ------------------------- JSON -----------------------
    private static final String PLAYER_OBJECT_ID = "player_object_id";
    public static final String [] supportableActions(){
        String[] validTitles = {"Action Point", "Pass Turn", "Seizure Failure", "Seizure Point", "Skip Seizure"};
        return validTitles;
    }
    // ------------------------- JSON -----------------------
    public TurnAbstractAction(JSONObject jo, HashMap<Integer, Player> players) throws JSONException {
        //derive a player id first and then merge it with the created Player object
        if(jo.has(PLAYER_OBJECT_ID)) {
            Integer player_id = jo.getInt(PLAYER_OBJECT_ID);
            mPlayer = players.get(player_id);
        }
    }
    /**************** JSON Constructor *************************/

    //Instantiate player variable
    public TurnAbstractAction(Player player){
        mPlayer = player;
    }



    //apply a given action
    public void apply(){

    };

    //rollback a given action
    public void rollback(){

    };

    public String title() {
        return mTitle;
    }

    //return a reference to the localization string resource id
    public int titleId() {
        return mTitleId;
    }

    public String color() {
        return mColor;
    }

    /*          Converter to JSON object         */
    public JSONObject convertToJSON() throws JSONException {
        //Need to call all previous methods in consequentially
        JSONObject jo = new JSONObject();

        jo.put(PLAYER_OBJECT_ID, mPlayer.hashCode());
        return jo;
    }
}
