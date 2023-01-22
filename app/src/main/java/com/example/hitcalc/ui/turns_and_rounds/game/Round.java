package com.example.hitcalc.ui.turns_and_rounds.game;


import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.ActionPoint;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.AvailableActions;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.TurnAbstractAction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/*
* Round workflow specification
* */
public class Round {
    private ArrayList<Turn> mTurns = new ArrayList<Turn>();
    private int mRoundNumber;
    //Available actions
    private HashMap<String, AvailableActions> mAvailablePlayersActions; // mapping between a player and his available actions

    // ------------------------- JSON -----------------------
    private static final String TURNS = "turns";
    private static final String ROUND_NUMBER = "round_number";
    private static final String AVAILABLE_PLAYERS_ACTIONS = "available_players_actions";
    // ------------------------- JSON -----------------------

    /**************** JSON Constructor *************************/
    public Round(JSONObject jo, HashMap<Integer, Player> players, UnitWeaknessTable table) throws JSONException {
        Player player = null;
        JSONArray jArray;

        //derive player object reference
        if(jo.has(ROUND_NUMBER)) {
            mRoundNumber = jo.getInt(ROUND_NUMBER);
        }

        //Get all formations and put them into army array
        if(jo.has(TURNS)) {
            jArray = jo.getJSONArray(TURNS);
            mTurns = new ArrayList<Turn>();

            //Populate attributes for army and civil lists
            for (int i = 0; i < jArray.length(); i++) {
                mTurns.add(new Turn(jArray.getJSONObject(i), players, table));
            }
        }

        //retrieve leader seizure points
        if(jo.has(AVAILABLE_PLAYERS_ACTIONS)) {
            jArray = jo.getJSONArray(AVAILABLE_PLAYERS_ACTIONS);
            mAvailablePlayersActions = new HashMap<String, AvailableActions>();

            //derive the formation title and available actions
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject entryArray = jArray.getJSONObject(i);
                String title = entryArray.getString("TITLE");
                JSONObject action_list = entryArray.getJSONObject("AVAILABLE_ACTIONS");

                mAvailablePlayersActions.put(title, new AvailableActions(action_list, players));
            }
        }
    }
    /**************** JSON Constructor *************************/

    public Round(int number, ArrayList<Player> players, String activePlayer, Round prevRound){
        //store Round Number
        mRoundNumber = number;

        //Retrieve prev turn
        int turnAmount = mTurns.size();
        Turn prevTurn = null;
        if(turnAmount > 1){
            prevTurn = mTurns.get(turnAmount - 2);
        }

        //instantiate the first turn in the round
        addTurn(activePlayer);

        //Determine available actions for the given round
        mAvailablePlayersActions = new HashMap<String, AvailableActions>();

        for(Player player: players){
            if(mRoundNumber == 1) {
                //first round, need to calculate seizure points
                mAvailablePlayersActions.put(player.title(), new AvailableActions(player, mRoundNumber, null));
            }else{
                //derive inheriting seizure points
                HashMap<String, ArrayList<TurnAbstractAction>> inheritingSeizurePoints = prevRound.getAvailableSeizurePoints(player.title());
                mAvailablePlayersActions.put(player.title(), new AvailableActions(player, mRoundNumber, inheritingSeizurePoints));
            }
        }
    }

    //create a new turn for given active player
    public void addTurn(String activePlayer){
        int id;
        if(mTurns.size() == 0){
            id = 1;
        }else{
            id = mTurns.size() + 1;
        }

        //instantiate the first turn in the round
        Turn turn = new Turn(id, activePlayer);
        //Instantiate a new turn
        mTurns.add(turn);
    }

    //provide active turn object for the given round
    public Turn currentTurn(){
        if(mTurns != null){
            return mTurns.get(mTurns.size() - 1);
        }
        return null;
    }

    //provide active turn object for the given round
    public Turn getPreviousTurn(){
        if(mTurns != null && mTurns.size() >= 2){
            return mTurns.get(mTurns.size() - 2);
        }
        return null;
    }

    //Get active player
    private String getActivePlayer(){
        return mTurns.get(mTurns.size()-1).activePlayer();
    }


    //returns a list of available turn actions according to the chosen formation and current turn of the active player
    public ArrayList<TurnAbstractAction> getAvailableActionsForTheTurn(String formationTitle){
        String activePlayer = getActivePlayer();
        AvailableActions availableActions = mAvailablePlayersActions.get(activePlayer);
        ArrayList<TurnAbstractAction> actions = null;

        int turnAmount = mTurns.size();

        if(turnAmount > 1){
            //get current & previous turns
            Turn currentTurn = mTurns.get(turnAmount - 1);
            Turn previousTurn = mTurns.get(turnAmount - 2);

            if(currentTurn.activePlayer().equals(previousTurn.activePlayer())){
               //if previous turn was done by the same user, than this is a second turn attempt, i.e. turn seizure, turn seizure failure or turn pass (cannot use action points)
                actions = availableActions.getSecondTurnActions(formationTitle);
            }else{
                //if the playes does not match than this is a new turn of the next user, i.e. action points & pass action or use turn seizure / turn seizure failure
                actions = availableActions.getFirstTurnActions(formationTitle);
            }
        }
        else{
            //if this is the first turn in the round
            actions = availableActions.getFirstTurnActions(formationTitle);
        }

        return actions;
    }

    //retrieve a set of available action point for given player
    public ArrayList<ActionPoint> getAvailableActionPoints(String player){
        return mAvailablePlayersActions.get(player).getActionPoints();
    }

    //returns a list of available actions left for the round for a given formation title
    public ArrayList<TurnAbstractAction> getAvailableActionsForRound(String playertitle, String formationTitle){
        AvailableActions availableActions = mAvailablePlayersActions.get(playertitle);

        return availableActions.getFirstTurnActions(formationTitle);
    }

    //derive available seizure points
    public HashMap<String, ArrayList<TurnAbstractAction>> getAvailableSeizurePoints(String playertitle){
        AvailableActions availableActions = mAvailablePlayersActions.get(playertitle);
        return availableActions.getLeaderSeizurePoints();
    }

    /*
    * Remove user turn actions from available list.
    * */
    public void spendTurnActions(ArrayList<TurnAbstractAction> actions){
        mAvailablePlayersActions.get(getActivePlayer()).remove(actions);
    }

    //Check for round end
    public boolean checkForRoundEnd(){
        return false;
    }
    //Configure tiredness

    //get round id
    public int id() {
        return mRoundNumber;
    }

    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON(HashMap<String, Player> players) throws JSONException {
        JSONObject jo = new JSONObject();
        JSONArray jArray;
        // Make an array in JSON format
        jo.put(ROUND_NUMBER, mRoundNumber);

        //Put turn array into JSON
        if(mTurns != null & mTurns.size() > 0){
            // Make an array in JSON format
            jArray = new JSONArray();

            for (Turn turn : mTurns) {
                jArray.put(turn.convertToJSON());
            }

            jo.put(TURNS, jArray);
        }

        // Convert to JSON a given HashMap of available players actions
        if(mAvailablePlayersActions != null && mAvailablePlayersActions.size() > 0){
            jArray = new JSONArray();

            for(String title : mAvailablePlayersActions.keySet()) {
                JSONObject actionsJSON = new JSONObject();
                actionsJSON.put("TITLE", title);
                actionsJSON.put("AVAILABLE_ACTIONS", mAvailablePlayersActions.get(title).convertToJSON(players.get(title)));
                jArray.put(actionsJSON);
            }

            jo.put(AVAILABLE_PLAYERS_ACTIONS, jArray);
        }

        return jo;
    }
}