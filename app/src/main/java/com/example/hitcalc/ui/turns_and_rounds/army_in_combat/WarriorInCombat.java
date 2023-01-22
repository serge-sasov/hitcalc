package com.example.hitcalc.ui.turns_and_rounds.army_in_combat;

import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect.AbstractWeaknessEffect;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect.ExtraActionPoints;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.DistanceAttackDefence;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.ForcedRetreat;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.HitAndRun;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.RampageDefence;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.RetreatBeforeShock;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.RootAction;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.ShockCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.UnitAbstractAction;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class WarriorInCombat extends WarriorInShock {
    private Integer mWeakness = 0; //define weakness stage of the unit 0 - no weakness, 5 - max value
    private Integer mWeaknessLimit;
    private Boolean mSpent = false; //a flag showing whether a warrior during a round was affected by any action that has influenced it weakness state.
    private Boolean mActivated = false; //a flag showing whether a warrior was activated during a round.

    private Integer mActivationThreshold = 1; //default value for activation via ActivationPoints
    private UnitWeaknessTable mUnitWeaknessTable;

    // ------------------------- JSON -----------------------
    private static final String WEAKNESS = "weakness";
    private static final String WEAKNESS_LIMIT = "weakness_limit";
    private static final String ACTIVATION_THRESHOLD = "activation_threshold";
    private static final String SPENT = "spent";
    private static final String ACTIVATED = "activated";
    // ------------------------- JSON -----------------------

    // Step 1 - This interface defines the type of messages I want to communicate to my owner
    public interface OnDataChangeListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onWeaknessChanged(Integer weakness); //coordinates X & Y
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    // The listener must implement the events interface and passes messages up to the parent.
    private ArrayList<OnDataChangeListener> mListeners = null;


    // Step 3: Assign the listener implementing events interface that will receive the events
    public void setOnDataChangedListener(WarriorInCombat.OnDataChangeListener listener) {
        if(mListeners == null){
            mListeners = new  ArrayList<OnDataChangeListener>();
        }

        mListeners.add(listener);
    }

    //Constructor to clone the WarriorItem into WarriorInShock
    public WarriorInCombat(WarriorItem warrior, UnitWeaknessTable unitWeaknessTable) {
        super(warrior);

        mUnitWeaknessTable = unitWeaknessTable;
        if(!warrior.type().equals("Leader") && !warrior.type().equals("Dummy") && !warrior.type().equals("Harizma")) {
            //Consider two hex units size
            Integer twoHexMultiplier = 1;
            if (mIsTwoHex) {
                twoHexMultiplier = 2;
            }
            //get the threshold value after the unit roots
            mWeaknessLimit = unitWeaknessTable.getWeaknessLimitValue(mType) * twoHexMultiplier;
        }
    }

    // ------------------ JSON Constructor -----------------------------
    public WarriorInCombat(JSONObject jo, UnitWeaknessTable table) throws JSONException {
        super(jo);

        mUnitWeaknessTable = table;

        if(jo.has(WEAKNESS)) {
            mWeakness = jo.getInt(WEAKNESS);
        }
        if(jo.has(WEAKNESS_LIMIT)) {
            mWeaknessLimit = jo.getInt(WEAKNESS_LIMIT);
        }
        if(jo.has(ACTIVATION_THRESHOLD)) {
            mActivationThreshold = jo.getInt(ACTIVATION_THRESHOLD);
        }

        if(jo.has(ACTIVATED)) {
            mActivated = jo.getBoolean(ACTIVATED);
        }

        if(jo.has(SPENT)) {
            mSpent = jo.getBoolean(SPENT);
        }
    }

    public Integer getWeakness(){
        return mWeakness;
    }

    public void setWeakness(Integer weakness){
        mWeakness = weakness;

        //Step 4. Notify all listeners if weakness changes
        notifyWeaknessListeners();
    }

    //Increase Weakness on delta value
    public void addWeakness(Integer delta){
        mWeakness += delta;

        //Step 4. Notify all listeners if weakness changes
        notifyWeaknessListeners();
    }

    //Decrease Weakness on delta value
    public void substructWeakness(Integer delta){
        mWeakness -= delta;

        //Step 4. Notify all listeners if weakness changes
        notifyWeaknessListeners();
    }
    // ------------------ JSON Constructor -----------------------------

    //Decrease Weakness on delta value without triggering listener,
    // that in turn will update the view element the value displayed in
    public void rollbackWeakness(Integer delta){
        mWeakness -= delta;
        notifyWeaknessListeners();
    }

    //Decrease weakness on 1 to allow warrior to have a rest
    public void rest(){
        //give a rest for 1 point for one size units and 2 for double size
        if(mIsTwoHex == true){
            mWeakness = mWeakness - 2;
        }else {
            mWeakness--;
        }

        //If too much subtracted, then simply set to zero
        if(mWeakness < 0) {
            mWeakness = 0;
            //Step 4. Notify all listeners if weakness changes
            //notifyWeaknessListeners();
        }
    }

    //Inform weakness listeners about the event happend
    protected void notifyWeaknessListeners(){
        //Notify all listeners if weakness changes
        if(mListeners != null){
            for(OnDataChangeListener listener: mListeners) {
                listener.onWeaknessChanged(mWeakness);
            }
        }
    }

    public Integer getWeaknessLimit() {
        return mWeaknessLimit;
    }

    //return number of action points to be spent to activate given warrior
    private Integer calculateActivationThreshold(){
        Integer activationPointsThreshold = 1;

        ArrayList<AbstractWeaknessEffect> weaknessEffects = mUnitWeaknessTable.getWeaknessEffects(this);
        if(weaknessEffects != null) {
            for (AbstractWeaknessEffect effect : weaknessEffects) {
                if (effect.getClass().equals(ExtraActionPoints.class)) {
                    return activationPointsThreshold += effect.getExtraValue();
                }
            }
        }
        //By default the activation costs 1 AP
        return activationPointsThreshold;
    }

    /*
     *Return a calculated final effect that results of all possible events as checks or already reached effects of root & retreat events.
     * As input are provided a value of randomly derived dice roll.
     * As output are provided null, retreat or root
     * */

    public String calculateRoundCompletionEffect(Random randomD10){
        ArrayList<AbstractWeaknessEffect> weaknessEffects = mUnitWeaknessTable.getWeaknessEffects(this);

        String effect = null;
        if(weaknessEffects != null){
            for(AbstractWeaknessEffect item: weaknessEffects){

                String result = item.getEffect(randomD10, mTroopQuality);

                //if the effect results in root, than immediately stop further processing nd give derived result further.
                if(result != null && result.equals("Root")){
                    return result;
                }else if(result != null){
                    effect = result;
                }
            }
        }

        return effect;
    }

    public void updateActivationThreshold(){
        mActivationThreshold = calculateActivationThreshold();
    }

    public Integer getActivationThreshold() {
        return mActivationThreshold;
    }

    //Derive a of possible actions
    public ArrayList<UnitAbstractAction> actionList(LoadTable table){
        WarriorInCombat warrior = this;
        ArrayList<UnitAbstractAction> actionList = new ArrayList<UnitAbstractAction>();

        /*
            actionList.add(new ShockCombat(table, warrior));
            if(warrior.isTwoHexSize() == true){
                //create a second action
                actionList.add(new ShockCombat(table, warrior));
            }
             */
        try {
            actionList.add(new HitAndRun(table, warrior));

            actionList.add(new DistanceAttackDefence(table, warrior));
            if(warrior.twoHexSize() == true) {
                //create a second action
                actionList.add(new DistanceAttackDefence(table, warrior));
            }
            actionList.add(new RetreatBeforeShock(table, warrior));
            actionList.add(new ForcedRetreat(table, warrior));
            //actionList.add(new Rampage(table, warrior));
            actionList.add(new RampageDefence(table, warrior));
            if(warrior.twoHexSize() == true) {
                actionList.add(new RampageDefence(table, warrior));
            }

        } catch (IOException|CsvException e) {
            e.printStackTrace();
        }

        return actionList;
    }

    //get action for shock combat -> used for phalanx combat calculation
    public ArrayList<UnitAbstractAction> shockAction(LoadTable table){
        WarriorInCombat warrior = this;
        ArrayList<UnitAbstractAction> actionList = new ArrayList<UnitAbstractAction>();

        try{
            actionList.add(new ShockCombat(table, warrior));
            //actionList.add(new ShockCombat(table, warrior));
        } catch (IOException|CsvException e) {
            e.printStackTrace();
        }

        return actionList;
    }

    //get action for shock combat -> used for phalanx combat calculation
    public ArrayList<UnitAbstractAction> rootAction(FormationInCombat formation){
        WarriorInCombat warrior = this;
        ArrayList<UnitAbstractAction> actionList = new ArrayList<UnitAbstractAction>();

        try{
            actionList.add(new RootAction(formation, warrior));
        } catch (IOException|CsvException e) {
            e.printStackTrace();
        }

        return actionList;
    }

    //calculate a score value for killed warrior unit
    public int score(){
        int score = mTroopQuality;

        if(mType.equals("SK") || mType.equals("CH") || mType.equals("EL")) {
            score = 2;
        }

        if(mIsTwoHex == true){
            score = score * 2;
        }

        return score;
    }

    //set the unit state to activated if the warrior formation was activated the turn
    public void activate(){
        mActivated = true;
    }

    //deactivate the warrior at the end of each round
    public void deactivate(){
        mActivated = false;
    }

    //set a spent flag to the user which has make any action except of moving the round
    public void spend(){
        mSpent = true;
    }

    //give a rest for all spent units at the end of the round
    public void recover(){

        mSpent = false;
    }



    /*          Converter to JSON object         */
    public JSONObject convertToJSON() throws JSONException {
        //Need to call all previous methods in consequentially
        JSONObject jo = new JSONObject();
        jo = super.convertToJSON();

        jo.put(WEAKNESS, mWeakness);
        jo.put(WEAKNESS_LIMIT, mWeaknessLimit);
        jo.put(ACTIVATION_THRESHOLD, mActivationThreshold);
        jo.put(ACTIVATED, mActivated);
        jo.put(SPENT, mSpent);

        return jo;
    }
}
