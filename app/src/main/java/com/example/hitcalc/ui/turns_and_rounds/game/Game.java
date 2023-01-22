package com.example.hitcalc.ui.turns_and_rounds.game;

import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.ui.combat_scenes.army.Army;
import com.example.hitcalc.ui.combat_scenes.map.MapView;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.PassTurn;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.SeizureFailure;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.SeizurePoint;
import com.example.hitcalc.ui.turns_and_rounds.game.turn_actions.TurnAbstractAction;
import com.opencsv.exceptions.CsvException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/*
* Game
*   -> Rounds[0..n]
*       -> Turns[0..k]
*   -> Players[0,1]
*   -> active player
*
* */
public class Game {
    private String mActivePlayer; //a player that has an initiative in the given turn
    private ArrayList<Player> mPlayers; //list of players
    private ArrayList<Round> mRounds; //list of turns

    private MapView mMapView; //current map layout being displayed to the user

    // ------------------------- JSON -----------------------
    private static final String ACTIVE_PLAYER = "active_player";
    private static final String PLAYERS = "players";
    private static final String ROUNDS = "ROUNDS";
    // ------------------------- JSON -----------------------

    /**************** JSON Constructor *************************/
    public Game(JSONObject jo, GameStorage gameStorage) throws JSONException {
        JSONArray jArray;
        HashMap<Integer, Player> playersMap = null;
        UnitWeaknessTable table = getUnitWeaknessTable(gameStorage);

        if(jo.has(ACTIVE_PLAYER)) {
            mActivePlayer = jo.getString(ACTIVE_PLAYER);
        }

        //Get all formations and put them into army array
        if(jo.has(PLAYERS)) {
            jArray = jo.getJSONArray(PLAYERS);
            mPlayers = new ArrayList<Player>();
            playersMap = new HashMap<Integer, Player>();

            //Populate attributes for army and civil lists
            for (int i = 0; i < jArray.length(); i++) {
                Player player = new Player(jArray.getJSONObject(i), table);
                mPlayers.add(player);

                playersMap.put(player.id(), player);
            }
        }

        //Get all formations and put them into army array
        if(jo.has(ROUNDS)) {
            jArray = jo.getJSONArray(ROUNDS);
            mRounds = new ArrayList<Round>();

            //Populate attributes for army and civil lists
            for (int i = 0; i < jArray.length(); i++) {
                mRounds.add(new Round(jArray.getJSONObject(i), playersMap, table));
            }
        }
    }
    /**************** JSON Constructor *************************/


    public Game(GameStorage gameStorage, String firstPlayer){
        //get list of players
        HashMap<String, Army> armyMap = new HashMap<String, Army>();
        ArrayList<String> players = gameStorage.scenario().getCivilizations();

        //create players of the game
        mPlayers = new ArrayList<Player>();
        for (String player : players) {
            //create a copy of given army instance
            ArmyInCombat army = new ArmyInCombat(gameStorage.scenario().getArmy(player), getUnitWeaknessTable(gameStorage));

            //create a list of players along with their initial setting
            mPlayers.add(new Player(player, army)); //<- seizure points to be determined out of the initial input data
        }

        //rebuild the army references in the scenario data structure
        //scenario(gameStorage);

        //instantiate turn & rounds
        mRounds = new ArrayList<Round>();
        Round currentRound = new Round(1, mPlayers, firstPlayer, null);
        mRounds.add(currentRound);

        mActivePlayer = firstPlayer;
    }

    //create army activated array and store it into scenario object
    public void scenario(GameStorage gameStorage){
        //get list of players
        HashMap<String, Army> armyMap = new HashMap<String, Army>();

        for(Player player: mPlayers) {
            //create a copy of given army instance
            ArmyActivated army = new ArmyActivated(player.army());

            //prepare new army references to update it in the scenario data structure
            armyMap.put(player.title(), army);
        }

        //rebuild the army references in the scenario data structure
        gameStorage.scenario().update(armyMap);
    }


    //used to rebuild scenario mapping
    public HashMap<String, Army> getArmyMap(){
        if(mPlayers != null){
            HashMap<String, Army> armyMap = new HashMap<String, Army>();
            for (Player player : mPlayers) {
                //prepare new army references to update it in the scenario data structure
                armyMap.put(player.title(),player.army());
            }
            return armyMap;
        }

        return null;
    }

    /*
    * Derive UnitWeaknessTable out of the gameStorage
    * */
    private UnitWeaknessTable getUnitWeaknessTable(GameStorage gameStorage){
        //get list of players
        UnitWeaknessTable unitWeaknessTable = null;

        try {
            unitWeaknessTable = new UnitWeaknessTable(gameStorage.getUnitWeaknessTable());
        } catch (CsvException | IOException e) {
            e.printStackTrace();
        }

        return unitWeaknessTable;
    }

    /*
    * Return current army in combat
    * */
    public ArmyInCombat getActivePlayersArmy(){
        return activePlayer().army();
    }

    /*
     * Return current army in combat
     * */
    public ArmyInCombat getPassivePlayersArmy(){
        return passivePlayer().army();
    }

    /*
    * Provide config data to the current turn
    * */
    public void configCurrentTurn(FormationActivated activatedFormation, ArrayList<TurnAbstractAction> actions){
        Turn currentTurn = currentRound().currentTurn();

        if(currentRound().currentTurn() != null){
            currentTurn.configTurn(activatedFormation, actions);
        }
    }

    //retrieve active player
    public Player activePlayer(){
        for(Player player: mPlayers){
            if(player.title().equals(mActivePlayer)){
                return player;
            }
        }
        return null;
    }

    //retrieve passive player
    public Player passivePlayer(){
        for(Player player: mPlayers){
            if(!player.title().equals(mActivePlayer)){
                return player;
            }
        }
        return null;
    }

    //return active round
    public Round currentRound() {
        if(mRounds != null) {
            return mRounds.get(mRounds.size() - 1);
        }
        return null;
    }

    /*
    * Terminate current turn and probably trigger switch of active player and start of a new round
    */
    public void finishTurn(){
        Round currentRound = currentRound();
        Turn currentTurn = currentRound.currentTurn();
        Turn previousTurn = currentRound.getPreviousTurn();

        //---------------------- Clean up spent actions -------------------------------------
        //Get a list of applied turn actions of active player
        ArrayList<TurnAbstractAction> appliedActions = currentTurn.getAppliedActions();

        //delete given items out of the available actions list
        if(appliedActions != null) {
            currentRound.spendTurnActions(appliedActions);
        }

        //--------------------- Update warrior threshold -------------------------------------
        //reset activation state & activation thresholds
        for(Player player: mPlayers){
            player.army().resetActivationThresholds();
        }

        //------------------------check if user switches -------------------------------------

        //check if active user shall be switched
        if(previousTurn != null && (currentTurn.activePlayer().equals(previousTurn.activePlayer()))){
            //if both turns were done by the same user
            mActivePlayer = passivePlayer().title();

        }else if(currentTurn.getAppliedActions().size() == 1){
            //check if seizure, seizure failure or end turn happened
            TurnAbstractAction action = currentTurn.getAppliedActions().get(0);
            if(action.getClass() == PassTurn.class ||
                    action.getClass() == SeizurePoint.class ||
                    action.getClass() == SeizureFailure.class){
                mActivePlayer = passivePlayer().title();
            }
        }

        //------------------------instantiate a new turn -------------------------------------

        if(checkRoundCompleted() == false) {
            //clean up killed formations
            getActivePlayersArmy().cleanUpRootedFormations();
            getPassivePlayersArmy().cleanUpRootedFormations();

            // Instantiate a new turn only in case the round remains unchanged
            currentRound.addTurn(mActivePlayer);
        }


        //-------- perform closing action at the round end (root, retreat & checks) -----------
    }

    //verify if the round is completed
    public Boolean checkRoundCompleted(){
        Round currentRound = currentRound();
        Turn currentTurn = currentRound.currentTurn();
        Turn previousTurn = currentRound.getPreviousTurn();

        //if there is no action done, than this is a newly instantiated turn for the current round.
        if(currentTurn.getAppliedActions() == null){
            return false;
        }

        //check if both turns were done by certain players
        if(previousTurn != null && !(currentTurn.activePlayer().equals(previousTurn.activePlayer()))){
            //if previous round was finished by another user
            if ((currentTurn.getAppliedActions().size() == 1 && previousTurn.getAppliedActions().size() == 1)){
                //if both actions done are "pass turn".
                if(currentTurn.getAppliedActions().get(0).getClass() == PassTurn.class &&
                            previousTurn.getAppliedActions().get(0).getClass() == PassTurn.class) {
                    //check if actions in both rounds were the same type EndTurn
                    TurnAbstractAction currentAction = currentTurn.getAppliedActions().get(0);
                    TurnAbstractAction previousAction = previousTurn.getAppliedActions().get(0);
                    if (currentAction.getClass() == PassTurn.class &&
                            previousAction.getClass() == PassTurn.class) {

                        return true;
                    }
                }
            }
        }
        return false;
    }

    //Instantiate new round
    public void newRound(){
        //reset activation state of each available formation and give a rest to the not-activated warriors
        for(Player player: mPlayers){
            player.army().resetActivationState();
        }
        //calculate new round id
        int id = mRounds.size() + 1;
        //derive previous round
        Round prevRound = null;
        if(id > 1) {
            prevRound = mRounds.get(mRounds.size() - 1);
        }
        //instantiate a new round item
        Round round = new Round(id, mPlayers, mActivePlayer, prevRound);
        mRounds.add(round);
    }

    //set map config
    public void mapView(MapView mapView){
        mMapView = mapView;
    }

    public MapView mapView(){
        return mMapView;
    }

    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        JSONArray jArray;
        HashMap<String, Player> playersMap = null;

        if(mActivePlayer != null) {
            jo.put(ACTIVE_PLAYER, mActivePlayer);
        }

        //Put turn array into JSON
        if(mPlayers != null){
            // Make an array in JSON format
            jArray = new JSONArray();
            playersMap = new HashMap<String, Player>();

            for (Player player : mPlayers) {
                jArray.put(player.convertToJSON());
                playersMap.put(player.title(), player);
            }

            jo.put(PLAYERS, jArray);
        }

        // Convert to JSON a given HashMap of available players actions
        if(mRounds != null && mRounds.size() > 0){
            jArray = new JSONArray();

            for(Round round : mRounds) {
                jArray.put(round.convertToJSON(playersMap));
            }

            jo.put(ROUNDS, jArray);
        }

        return jo;
    }
}