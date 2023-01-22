package com.example.hitcalc.ui.turns_and_rounds.game;

import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.ActionPoint;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.PassTurn;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.SeizureFailure;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.SeizurePoint;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.SkipSeizure;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.TurnAbstractAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/*
* Handles all data and state models for turn handling as
* - spend AP
* - pass a turn
*  - seizure a turn
*  - give a rest
*  - failure seizure
*/
public class Turn {
    private ArrayList<TurnAbstractAction> mActions; //applied action list
    private String mActivePlayer; //current active player
    private FormationActivated mFormation; //activated formation title
    private int mTurnNumber;

    //-------------------- JSON -------------------------------------
    private static final String ACTIONS = "actions";
    private static final String ACTIVE_PLAYER = "active_player";
    private static final String TURN_NUMBER = "turn_number";
    private static final String FORMATION = "formation";
    //-------------------- JSON -------------------------------------

    /**************** JSON Constructor *************************/
    public Turn(JSONObject jo, HashMap<Integer, Player> players, UnitWeaknessTable table) throws JSONException {
        JSONArray jArray;

        if(jo.has(ACTIVE_PLAYER)) {
            mActivePlayer = jo.getString(ACTIVE_PLAYER);
        }

        if(jo.has(TURN_NUMBER)) {
            mTurnNumber = jo.getInt(TURN_NUMBER);
        }

        if(jo.has(FORMATION)) {
            for(Player player: players.values()){
                if(player.title().equals(mActivePlayer)){
                    mFormation = new FormationActivated(jo.getJSONObject(FORMATION), player.army(), table);
                }
            }
        }

        if(jo.has(ACTIONS)) {
            jArray = jo.getJSONArray(ACTIONS);
            mActions = new ArrayList<TurnAbstractAction>();

            //Populate attributes for army and civil lists
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject object = jArray.getJSONObject(i).getJSONObject("ACTION");
                switch(jArray.getJSONObject(i).getString("TITLE")){
                    case "Action Point":
                        mActions.add(new ActionPoint(object, players));
                        break;

                    case "Pass Turn":
                        mActions.add(new PassTurn(object, players));
                        break;

                    case "Seizure Failure":
                        mActions.add(new SeizureFailure(object, players));
                        break;

                    case "Seizure Point":
                        mActions.add(new SeizurePoint(object, players));
                        break;

                    case "Skip Seizure":
                        mActions.add(new SkipSeizure(object, players));
                        break;

                    default:
                        break;
                }
            }
        }
    }
    /**************** JSON Constructor *************************/

    //instantiate a new turn
    public Turn(Integer number, String activePlayer){
        mTurnNumber = number;
        mActivePlayer = activePlayer;
    }

    //
    public String activePlayer() {
        return mActivePlayer;
    }

    //configure current turn configuration
    public void configTurn(FormationActivated activatedFormation, ArrayList<TurnAbstractAction> actions){
        mActions = actions;
        mFormation = activatedFormation;
    }

    public Set<String> getSubFormationTitles() {
        if(mFormation.subFormations() != null && mFormation.subFormations().size() > 0) {
            return mFormation.subFormations().keySet();
        }
        return null;
    }

    public String formationTitle() {
        return mFormation.getLeaderOrFormationTitle();
    }

    public ArrayList<TurnAbstractAction> getAppliedActions() {
        return mActions;
    }

    public FormationInCombat getFormation() {
        return mFormation;
    }

    //get turn round id
    public int id() {
        return mTurnNumber;
    }


    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        // Make an array in JSON format
        jo.put(ACTIVE_PLAYER, mActivePlayer);
        jo.put(TURN_NUMBER, mTurnNumber);

        JSONArray jArray = null;

        if(mFormation != null) {
            jo.put(FORMATION, mFormation.convertToJSON());
        }

        //Convert actions into JSON
        if(mActions != null) {
            JSONArray jActionArray = new JSONArray();
            //For each given action create an array of title & json-object
            for (TurnAbstractAction action : mActions) {
                JSONObject actionJSON = new JSONObject();
                actionJSON.put("TITLE", action.title());
                actionJSON.put("ACTION", action.convertToJSON());
                jActionArray.put(actionJSON);
            }

            jo.put(ACTIONS, jActionArray);
        }

        return jo;
    }
}