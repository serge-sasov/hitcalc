package com.example.hitcalc.ui.turns_and_rounds.game.turn_actions;

import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.Player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/*
 * Determine available actions for the given round:
 * - action point
 * - seizure point
 * - seizure failure
 * - pass turn
 * */
public class AvailableActions {
    private ArrayList<ActionPoint> mActionPoints;
    private HashMap<String, ArrayList<TurnAbstractAction>> mLeaderSeizurePoints;
    private PassTurn mPassTurn;
    private SkipSeizure mSkipSeizure;

    //-------------------- JSON -------------------------------------
    private static final String ACTION_POINTS = "action_points";
    private static final String LEADER_SEIZURE_POINTS = "leader_seizure_points";
    private static final String PLAYER_OBJECT_ID = "player_object_id";
    //-------------------- JSON -------------------------------------

    /**************** JSON Constructor *************************/
    public AvailableActions(JSONObject jo, HashMap<Integer, Player> players) throws JSONException {
        Player player = null;
        JSONArray jArray;
        //derive player object reference
        if(jo.has(PLAYER_OBJECT_ID)) {
            int player_id = jo.getInt(PLAYER_OBJECT_ID);
            player = players.get(player_id);

            mPassTurn = new PassTurn(player);
            mSkipSeizure = new SkipSeizure(player);
        }

        //Get all formations and put them into army array
        if(jo.has(ACTION_POINTS)) {
            jArray = jo.getJSONArray(ACTION_POINTS);
            mActionPoints = new ArrayList<ActionPoint>();

            //Populate attributes for army and civil lists
            for (int i = 0; i < jArray.length(); i++) {
                mActionPoints.add(new ActionPoint(jArray.getJSONObject(i), players));
            }
        }

        //retrieve leader seizure points
        if(jo.has(LEADER_SEIZURE_POINTS)) {
            jArray = jo.getJSONArray(LEADER_SEIZURE_POINTS);
            mLeaderSeizurePoints = new HashMap<String, ArrayList<TurnAbstractAction>>();

            //derive the formation title and list of seizure action
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject entry = jArray.getJSONObject(i);
                String title = entry.getString("TITLE");
                JSONArray action_list = entry.getJSONArray("SEIZURE_LIST");

                ArrayList<TurnAbstractAction> actions = new ArrayList<TurnAbstractAction>();
                for (int j = 0; j < action_list.length(); j++) {
                    actions.add(new SeizurePoint(action_list.getJSONObject(j), players));
                }
                //add a seizure failure to the list
                actions.add(new SeizureFailure(player));

                mLeaderSeizurePoints.put(title, actions);
            }
        }
    }
    /**************** JSON Constructor *************************/
    /*
    @input
        player - given player
        inheritedSeizurePoints - inherited seizure points are derived from the previous turn
        roundId - unique round id to recognise how to get SeizurePoint, i.e. to calculate or to inherit
     */
    public AvailableActions(Player player,
                            int roundId,
                            HashMap<String,ArrayList<TurnAbstractAction>> inheritingSeizurePoints){
        mActionPoints = new ArrayList<ActionPoint>();

        //Add a PassTurn
        mPassTurn = new PassTurn(player);

        //Add skip attempt action
        mSkipSeizure = new SkipSeizure(player);

        //Add Action Points
        for(int i = 0; i < calculateAvailableActionPoints(player.army()); i++){
            mActionPoints.add(new ActionPoint(player));
        }

        mLeaderSeizurePoints = new HashMap<String, ArrayList<TurnAbstractAction>>();

        if(roundId == 1) {
            //Calculate initial seizure points on the first round using the formation configs
            for(Formation formation : player.army().getFormations()) {
                if (formation.getSeizurePoints() != null && formation.getSeizurePoints() > 0) {
                    ArrayList<TurnAbstractAction> seizureArray = new ArrayList<TurnAbstractAction>();
                    for (int i = 0; i < formation.getSeizurePoints(); i++) {
                        seizureArray.add(new SeizurePoint(player));
                    }
                    seizureArray.add(new SeizureFailure(player));
                    mLeaderSeizurePoints.put(formation.getLeaderOrFormationTitle(), seizureArray);
                }
            }
        }else{
            //inherit yet not consumed seizure points from previous round
            mLeaderSeizurePoints = inheritingSeizurePoints;
        }
    }

    //Get action points for the round (calculated as 50% of available formations rounded down)
    public Integer calculateAvailableActionPoints(ArmyInCombat army){
        //get the amount of available formations and divide it via 2
        int actionPoints = (int) army.getFormations().size() / 2;

        //return result
        return actionPoints;
    }

    public ArrayList<ActionPoint> getActionPoints() {
        return mActionPoints;
    }

    public HashMap<String, ArrayList<TurnAbstractAction>> getLeaderSeizurePoints() {
        return mLeaderSeizurePoints;
    }

    public PassTurn getPassTurn() {
        return mPassTurn;
    }

    /*
     * Return all available actions for given formation for the first turn
     * */
    public ArrayList<TurnAbstractAction> getFirstTurnActions(String formationTitle){
        ArrayList<TurnAbstractAction> actions = new ArrayList<TurnAbstractAction>();

        for(ActionPoint actionPoint: mActionPoints) {
            actions.add(actionPoint);
        }

        //show up the available seizure points if exists
        ArrayList<TurnAbstractAction> seizurePoints = mLeaderSeizurePoints.get(formationTitle);
        //Consider failure attempt by determining the amount of available seizure points
        if(seizurePoints != null && seizurePoints.size() > 1) {
            for (TurnAbstractAction seizurePoint : seizurePoints) {
                actions.add(seizurePoint);
            }
        }

        actions.add(mPassTurn);

        return actions;
    }
    /*
     * Return available actions for given formation for the second turn (without action points)
     * */
    public ArrayList<TurnAbstractAction> getSecondTurnActions(String formationTitle){
        ArrayList<TurnAbstractAction> actions = new ArrayList<TurnAbstractAction>();
        if(mLeaderSeizurePoints != null) {
            ArrayList<TurnAbstractAction> seizurePoints = mLeaderSeizurePoints.get(formationTitle);
            //Consider failure attempt by determining the amount of available seizure points
            if(seizurePoints != null && seizurePoints.size() > 1) {
                for (TurnAbstractAction seizurePoint : seizurePoints) {
                    actions.add(seizurePoint);
                }
            }
        }

        actions.add(mSkipSeizure);

        return actions;
    }

    //Remove provided items out of list of available actions
    public void remove(ArrayList<TurnAbstractAction> actions){
        for(TurnAbstractAction action: actions){
            //Remove used action point(s)
            if(action.getClass().equals(ActionPoint.class)){
                mActionPoints.remove(action);
            }

            //Remove used seizure point
            if(action.getClass().equals(SeizurePoint.class)){
                for(String leaderName : mLeaderSeizurePoints.keySet()){
                    if(mLeaderSeizurePoints.get(leaderName).contains(action)) {
                        mLeaderSeizurePoints.get(leaderName).remove(action);
                    }
                }
            }

            //Remove available seizure point in case seizure attempt failure
            if(action.getClass().equals(SeizureFailure.class)){
                for(String leaderName : mLeaderSeizurePoints.keySet()){
                    if(mLeaderSeizurePoints.get(leaderName).contains(action)) {
                        //remove the first element out of the array
                        mLeaderSeizurePoints.get(leaderName).remove(0);
                    }
                }
            }
        }
    }

    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON(Player player) throws JSONException {
        JSONObject jo = new JSONObject();

        JSONArray jActionPointsArray = new JSONArray();

        jo.put(PLAYER_OBJECT_ID, player.hashCode());

        // And load it with the notes
        for (ActionPoint action : mActionPoints) {
            jActionPointsArray.put(action.convertToJSON()); //put list of armies
        }
        jo.put(ACTION_POINTS,jActionPointsArray);

        if(mLeaderSeizurePoints != null) {
            JSONArray jLeaderSeizurePointsArray = new JSONArray();
            //put a hash map into JSON
            for (String title : mLeaderSeizurePoints.keySet()) {
                JSONObject joFormationSeizurePoints = new JSONObject();
                joFormationSeizurePoints.put("TITLE", title);

                JSONArray jArray = new JSONArray();
                for (TurnAbstractAction action : mLeaderSeizurePoints.get(title)) {
                    //store only the Seizure Point without Seizure Failure
                    if(action.getClass() == SeizurePoint.class) {
                        jArray.put(action.convertToJSON());
                    }
                }

                joFormationSeizurePoints.put("SEIZURE_LIST", jArray);
                jLeaderSeizurePointsArray.put(joFormationSeizurePoints);
            }

            jo.put(LEADER_SEIZURE_POINTS, jLeaderSeizurePointsArray);
        }
        return jo;
    }
}
