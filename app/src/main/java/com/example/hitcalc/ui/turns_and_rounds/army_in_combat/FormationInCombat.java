package com.example.hitcalc.ui.turns_and_rounds.army_in_combat;

import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

/*
* The class describes the state of the formation being in combat that faces looses in units.
* If all units are lost the formation becomes lost.
* */
public class FormationInCombat extends Formation {
    public static Boolean ACTIVATED = true;
    public static Boolean DEACTIVATED = false;

    protected Boolean mSimilarity = false; //shows the identity of the unit type the formation consist of
    protected Boolean mElephants = false; //shows whether elephants are part of the formation
    protected Boolean mActivated = DEACTIVATED; //shows the activation status for the given round
    protected ArrayList<WarriorInCombat> mRootedWarriors;
    protected HashMap<String, FormationInCombat> mRootedSubFormations;

    // ------------------------- JSON -----------------------
    protected int mHash; //hash code of this object, used to establish a reference from the outer objects to this one.
    protected UnitWeaknessTable mTmpTable; //tmp table

    //----------- json-titles --------------
    protected static final String HASH = "hash";
    protected static final String SIMILARITY = "similarity";
    protected static final String HAS_ELEPHANTS = "has_elephants";
    protected static final String ACTIVATED_STR = "activated";
    protected static final String ROOTED_WARRIORS = "rooted_warriors";



    //Copy of the given formation to use it as a filter by the turn/combat calculator setup
    public FormationInCombat(FormationInCombat formation) {
        //copy basic simple data
        cloneBasicData(formation);
    }
    // ------------------------- JSON -----------------------

    // Step 1 - notify listeners about the warrior root
    public interface OnRootListener {
        // These methods are the different events and
        // need to pass relevant arguments related to the event triggered
        public void onRoot(WarriorInCombat warrior, Integer pageId); //coordinates X & Y

        public void onRecover(WarriorInCombat warrior, Integer pageId);
    }

    // Step 2 - This variable represents the listener passed in by the owning object
    private HashMap<FormationInCombat.OnRootListener, Integer>  mListeners = null;
    //private int mTabId;

    // Step 3: Assign the listener implementing events interface that will receive the events
    public void setOnRootListener(FormationInCombat.OnRootListener listener, int tabId) {

        //instantiate list of listeners
        if(mListeners == null){
            mListeners = new HashMap<FormationInCombat.OnRootListener, Integer>();
        }

        //if there is any warrior in the base formation
        if(mWarriors != null) {
            mListeners.put(listener, tabId);
        }

        if(mSubFormations != null){
            //is there is any number of sub-formations
            for(Formation formation: mSubFormations.values()){
                ((FormationInCombat) formation).setOnRootListener(listener, tabId);
            }
        }
    }

    /**************** JSON Constructor *************************/
    public FormationInCombat(JSONObject jo, UnitWeaknessTable table) throws JSONException {
        super(jo);
        mTmpTable = table; //store temporary data

        if(jo.has(SIMILARITY)) {
            mSimilarity = jo.getBoolean(SIMILARITY);
        }

        if(jo.has(HAS_ELEPHANTS)) {
            mElephants = jo.getBoolean(HAS_ELEPHANTS);
        }

        if(jo.has(ACTIVATED_STR)) {
            mActivated = jo.getBoolean(ACTIVATED_STR);
        }

        if(jo.has(ROOTED_WARRIORS)) {
            // Get an array in JSON format
            JSONArray jMembersArray = jo.getJSONArray(ROOTED_WARRIORS);

            //Create new formation member array and populate it
            mRootedWarriors = new ArrayList<WarriorInCombat>();

            for (int i = 0; i < jMembersArray.length(); i++) {
                mRootedWarriors.add(new WarriorInCombat(jMembersArray.getJSONObject(i), table));
            }
        }

        // parse warriors (tmp table is needed for the method)
        parseJSONWarriors(jo);

        // parse Sub-Formations (tmp table is needed for the method)
        parseJSONSubFormations(jo);

        mTmpTable = null; //clean up tmp table
    }

    @Override
    //used as separated method to make overriding possible
    protected void parseJSONWarriors(JSONObject jo) throws JSONException {
        if(jo.has(WARRIORS)) {
            if(jo.has(FORMATION_MEMBERS)) {
                // Get an array in JSON format
                JSONArray jMembersArray = jo.getJSONArray(FORMATION_MEMBERS);

                //Create new formation member array and populate it
                mFormationMembers = new String[jMembersArray.length()];

                for (int i = 0; i < jMembersArray.length(); i++) {
                    mFormationMembers[i] = jMembersArray.getString(i);
                }
            }

            JSONArray jWarriorsArray = jo.getJSONArray(WARRIORS);
            mWarriors = new ArrayList<WarriorItem>();

            for (int i = 0; i < jWarriorsArray.length(); i++) {
                mWarriors.add(new WarriorInCombat(jWarriorsArray.getJSONObject(i), mTmpTable));
            }
        }
    }

    @Override
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
                mSubFormations.put(jSubTitlesArray.getString(i), new FormationInCombat(jSubFormationsArray.getJSONObject(i), mTmpTable));
                mSubFormationTitles.add(jSubTitlesArray.getString(i));
            }
        }

        //get original hash code of the recovered object
        if(jo.has(HASH)){
            mHash = jo.getInt(HASH);
        }
    }

    /**************** JSON Constructor *************************/

    /*
    To clone formation it need to have a reference to the army it is attached to, due to necessity to
    link each sub-formation to its parent-layer formation origin, i.e. base Nicanor formation and any
    referenced sub-formations.
    */
    /************************* Clone Formations ********************************/
    /*
     * Clone parent formation only
     */
    public FormationInCombat(Formation formation, UnitWeaknessTable unitWeaknessTable){
        super();
        /*
         * To clone formation it need to have a reference to the army it is attached to, due to necessity to
         * link each sub-formation to its parent-layer formation origin, i.e. base nicanor formation and any
         * referenced sub-formations.
         */
        cloneParentFormationData(formation, unitWeaknessTable);
    }

    /*
     * Clone Formation
     */
    public FormationInCombat(Formation oldParentFormation,  HashMap<String, FormationInCombat> allFormations, UnitWeaknessTable unitWeaknessTable){
        super();
        //Step 1 Clone parent formation data
        cloneParentFormationData(oldParentFormation, unitWeaknessTable);

        /*Step2: For each founded sub formation perform a binding of them to the cloned parent formation or if there is not any
         * clone the given one.
         */
        for(Formation oldSubFormation : oldParentFormation.subFormations().values()) {
            //build up a reference to its parent formation if any, otherwise create a new sub-formation copy
            FormationInCombat formation = allFormations.get(oldSubFormation.getLeaderOrFormationTitle());
            addSubFormation(formation, false);
        }
    }

    private void cloneParentFormationData(Formation formation, UnitWeaknessTable unitWeaknessTable){
        //Copy each warrior item
        if(formation.warriors() != null) {
            mWarriors = new ArrayList<WarriorItem>();
            for (WarriorItem warrior : formation.warriors()) {
                mWarriors.add(new WarriorInCombat(warrior, unitWeaknessTable));
            }
        }

        if(formation.getFormationMembers() != null && formation.getFormationMembers().length > 0) {
            //Copy each member item
            mFormationMembers = new String[formation.getFormationMembers().length];
            int i = 0;
            for (String member : formation.getFormationMembers()) {
                mFormationMembers[i] = member;
                i++;
            }
        }


        if(formation.subFormations() == null) {
            boolean mAssociationWithParentFormation = false;
        }

        //copy basic simple data
        cloneBasicData(formation);
    }

    private void cloneBasicData(Formation formation){
        mLeader = formation.getLeader();
        mFormationTitle = formation.getTitle();
        mStandalone = formation.isStandalone();
        mSeizurePoints = formation.getSeizurePoints();
        //Find out the similarity for warriors & formations
        mSimilarity = similar();
        mElephants = elephants();
    }

    //check for similarity of the formation members
    private Boolean similar(){
        if(mWarriors != null && mWarriors.size() > 0) {
            int index = 0;
            String type = null;
            for (WarriorItem warrior : warriors()) {
                String currentType = warrior.type();
                if (isSimple(warrior) == true) {
                    if (index == 0) {
                        //memorize the first warrior type
                        type = warrior.type();
                    } else if (!warrior.type().equals(type)) {
                        // if different type exit the loop with negative outcome
                        return false;
                    }
                    index++;
                }
            }
        }
        return true;
    }

    //Check whether there is any elephant in the formation
    private Boolean elephants(){
        if(mWarriors != null && mWarriors.size() > 0) {
            for (WarriorItem warrior : warriors()) {
                if (warrior.equals("EL")) {
                    return true;
                }
            }
        }
        return false;
    }

    //rebuild a reference to the equal sub-formations
    public void revise(HashMap<String, FormationInCombat> allFormations){
        if(mSubFormations != null && mSubFormationTitles.size() > 0) {
            HashMap<String, Formation> subFormations = new HashMap<String, Formation>();
            ArrayList<String> subFormationTitles = new ArrayList<String>();
            for (String title : mSubFormations.keySet()) {
                for (String refTitle : allFormations.keySet()) {
                    if (refTitle.equals(title)) {
                        subFormations.put(refTitle, allFormations.get(refTitle));
                        subFormationTitles.add(refTitle);
                    }
                }
            }

            mSubFormations = subFormations;
            mSubFormationTitles = subFormationTitles;
        }
    }

    public Boolean getSimilarity() {
        return mSimilarity;

    }

    public Boolean formationHasElephants() {
        return mElephants;
    }

    // Update the activation thresholds based on the made user actions
    public void updateActivationThreshold(){
        updateWarriorsActivationThreshold(warriors());

        if(subFormations() != null){
            Collection<Formation> formations = subFormations().values();
            for(Formation item: formations){
                updateWarriorsActivationThreshold(item.warriors());
            }
        }
    }

    private void updateWarriorsActivationThreshold(ArrayList<WarriorItem> warriors){
        for(WarriorItem warrior: warriors){
            if(isSimple(warrior) == true) {
                ((WarriorInCombat) warrior).updateActivationThreshold();
            }
        }
    }

    //set the flag to true as soon as the user activates the formation
    public void activate() {
        mActivated = ACTIVATED;
    }

    /*
     * Deactivate formation at the end of each round.
     * isSubFormation flag shows whether the call performs for root (true) or sub (false) formation
     */
    public void deactivate(Boolean isRootFormation) {
        if(mActivated == ACTIVATED) {
            mActivated = DEACTIVATED;
        }else if(isRootFormation == true){
            //give a rest to the warriors of not activated formation
            rest();
        }

        //Deactivate any activated sub-formations
        if(subFormations() != null){
            for(Formation formation: subFormations().values()){
                //give a rest to the sub-formations which cannot be activated in a standalone mode.
                if(formation.isStandalone() == false) {
                    ((FormationInCombat) formation).deactivate(true);
                }else{
                    ((FormationInCombat) formation).deactivate(false);
                }
            }
        }
    }

    public Boolean state(){
        return mActivated;
    }

    //check for similarity of the formation members
    private void rest(){
        //give a rest for root formations
        ArrayList<WarriorItem> warriors = warriors();
        if(warriors != null) {
            for (WarriorItem warrior : warriors) {
                if(isSimple(warrior) == true) {
                    //Give a rest for each warrior that was not activated in the given turn
                    ((WarriorInCombat) warrior).rest();
                }
            }
        }

        //give a rest for sub-formations
        if(subFormations() != null){
            for(Formation formation: subFormations().values()){
                //give a rest to the sub-formations which cannot be activated in a standalone mode.
                if(formation.isStandalone() != true) {
                    ((FormationInCombat) formation).rest();
                }
            }
        }
    }

    //check warrior type, if a simple warrior, then return true, otherwise false
    private Boolean isSimple(WarriorItem warrior){
        if(!warrior.type().equals("Leader") && !warrior.type().equals("Harizma") && !warrior.type().equals("Dummy")) {
            return true;
        }
        return false;
    }

    //Arrange warriors according to its current activation value threshold
    public HashMap<Integer, ArrayList<WarriorInCombat>> arrangeWarriorsPerActivationValue(){
        HashMap<Integer, ArrayList<WarriorInCombat>> mappingActivationPointsPerWarrior =
                new HashMap<Integer, ArrayList<WarriorInCombat>>();

        for (WarriorItem warrior : mWarriors) {
            if (warrior.simple() == true) {
                Integer activationValue = ((WarriorInCombat) warrior).getActivationThreshold();

                //check formation activation state
                if(state() == true){
                    //Increase by 1 activation value if the formation was activated before
                    activationValue = activationValue + 1;
                }

                ArrayList<WarriorInCombat> warriorsPerActivationValue = mappingActivationPointsPerWarrior.get(activationValue);
                if(warriorsPerActivationValue == null){
                    warriorsPerActivationValue = new ArrayList<WarriorInCombat>();
                }
                warriorsPerActivationValue.add((WarriorInCombat) warrior);

                //update warrior list
                mappingActivationPointsPerWarrior.put(activationValue, warriorsPerActivationValue);
            }
        }

        return mappingActivationPointsPerWarrior;
    }

    //Arrange warriors according to the action (retreat or root) to be done
    private HashMap<Integer, ArrayList<WarriorInCombat>> arrangeWarriorsPerAction(){
        HashMap<Integer, ArrayList<WarriorInCombat>> mappingActivationPointsPerWarrior =
                new HashMap<Integer, ArrayList<WarriorInCombat>>();

        for (WarriorItem warrior : mWarriors) {
            if (warrior.simple() == true) {
                //Evaluate criteria. Derive actions for given warriors

                //Case 1. check for Retreat
                //Case 1. Root


                Integer activationValue = ((WarriorInCombat) warrior).getActivationThreshold();

                ArrayList<WarriorInCombat> warriorsPerActivationValue = mappingActivationPointsPerWarrior.get(activationValue);
                if(warriorsPerActivationValue == null){
                    warriorsPerActivationValue = new ArrayList<WarriorInCombat>();
                }
                warriorsPerActivationValue.add((WarriorInCombat) warrior);

                //update warrior list
                mappingActivationPointsPerWarrior.put(activationValue, warriorsPerActivationValue);
            }
        }

        return mappingActivationPointsPerWarrior;
    }

    //Derive the effect to be applied on the warriors at the end of each round
    public AffectedFormationWarriors calculateRoundCompletionEffect(Random randomD10){
        AffectedFormationWarriors affectedFormationWarriors = null;
        ArrayList<WarriorInCombat> rootedWarriors = null;

        if(mWarriors != null){
            for(WarriorItem warrior:mWarriors){
                if(warrior.simple()){
                    //check the applicable effects for each warrior
                    String effect = ((WarriorInCombat) warrior).calculateRoundCompletionEffect(randomD10);

                    if(effect != null){
                        //if there is any effect to be applied to the warrior then add it to the affected warriors object
                        if(affectedFormationWarriors == null){
                            //if there is still no affected warriors list create it
                            affectedFormationWarriors = new AffectedFormationWarriors();
                        }

                        if(effect == "Root"){
                            if(rootedWarriors == null) {
                                //if there is still no rooted warriors list create it
                                rootedWarriors = new ArrayList<WarriorInCombat>();
                            }

                            //Move warrior from available to the list of rooted warriors
                            rootedWarriors.add((WarriorInCombat) warrior);
                        }

                        //Put found effect & warrior into common mapping of root list
                        affectedFormationWarriors.addWarrior(effect, (WarriorInCombat) warrior);
                    }
                }
            }
        }

        //clean up all routed warriors
        //Move warrior from available to the list of rooted warriors
        if(rootedWarriors != null){
            //The warrior was killed or rooted from the battle field
            if(mRootedWarriors == null){
                mRootedWarriors = new ArrayList<WarriorInCombat>();
            }

            for(WarriorInCombat warrior : rootedWarriors){
                mRootedWarriors.add((WarriorInCombat) warrior);
                mWarriors.remove(warrior);
            }
        }

        if(mSubFormations != null){
            //In case there are any sub-formations
            for(String title : mSubFormations.keySet()){
                Formation formation = mSubFormations.get(title);

                if(formation.isStandalone() != true){
                    //execute the killed warriors clean up only for not stand-alone formations
                    for(WarriorItem warrior: ((FormationInCombat) formation).warriors()) {
                        if (warrior.simple()) {
                            //check the applicable effects for each warrior
                            String effect = ((WarriorInCombat) warrior).calculateRoundCompletionEffect(randomD10);

                            if (effect != null) {
                                //if there is any effect to be applied to the warrior then add it to the affected warriors object
                                if (affectedFormationWarriors == null) {
                                    affectedFormationWarriors = new AffectedFormationWarriors();
                                }
                                //Put found effect & warrior into common mapping of sub-formation list
                                affectedFormationWarriors.addSubFormationWarrior(title, effect, (WarriorInCombat) warrior);
                            }
                        }
                    }
                }
            }
        }

        return affectedFormationWarriors;
    }

    //Remove manually a warrior from the alive warrior set to the rooted one
    public void killWarrior(WarriorInCombat warrior){
        //clean up all routed warriors
        //Move warrior from available to the list of rooted warriors
        if(warrior != null){
            //The warrior was killed or rooted from the battle field
            if(mRootedWarriors == null){
                mRootedWarriors = new ArrayList<WarriorInCombat>();
            }

            mRootedWarriors.add((WarriorInCombat) warrior);
            mWarriors.remove(warrior);

            //notify listeners when a warrior was rooted by manual move to the rooted warriors range
            if(mListeners != null) {
                for(FormationInCombat.OnRootListener listener: mListeners.keySet()){
                    Integer tabId = mListeners.get(listener);
                    listener.onRoot(warrior, tabId);
                }
            }
        }
    }

    //Remove manually a warrior from the alive warrior set to the rooted one
    public void recoverWarrior(WarriorInCombat warrior){
        //clean up all routed warriors
        //Move warrior from available to the list of rooted warriors
        if(warrior != null){
            //The warrior was killed or rooted from the battle field
            if(mWarriors == null){
                mWarriors = new ArrayList<WarriorItem>();
            }

            mWarriors.add((WarriorInCombat) warrior);
            mRootedWarriors.remove(warrior);

            //notify listeners when a warrior was rooted by manual move to the rooted warriors range
            if(mListeners != null) {
                for(FormationInCombat.OnRootListener listener: mListeners.keySet()){
                    Integer tabId = mListeners.get(listener);
                    listener.onRecover(warrior, tabId);
                }
            }
        }
    }

    public ArrayList<WarriorInCombat> getRootedWarriors() {
        return mRootedWarriors;
    }

    //Check if formation is completely killed, i.e. all warriors are rooted
    public Boolean isRooted(){
        if(mWarriors != null && mWarriors.size() > 0){
            for(WarriorItem warrior : mWarriors){
                if(warrior.simple()){
                    //stop processing as there is still one alive warrior
                    return false;
                }
            }
            //skip any none simple units out of the view
            return true;
        }

        if(mWarriors == null && mRootedWarriors != null){
            //apply only to the formation having the own warriors
            return true;
        }

        //check for sub-formations root
        if(mSubFormations != null){
            for(String title:mSubFormations.keySet()){
                FormationInCombat formation = (FormationInCombat) mSubFormations.get(title);
                if(formation.isRooted()){
                    if(mRootedSubFormations == null){
                        mRootedSubFormations = new HashMap<String, FormationInCombat>();
                    }
                    //if any sub-formation rooted -> put it into the list of rooted sub-formations
                    mRootedSubFormations.put(title, formation);
                }
            }

            //clean up rooted sub-formation
            if(mRootedSubFormations != null){
                for(String title:mRootedSubFormations.keySet()){
                    mSubFormations.remove(title);
                }
            }

            //check once again whether all sub-formations rooted
            if(mSubFormations == null || mSubFormations.size() == 0){
                return true;
            }
        }

        return false;
    }

    //calculate a score value for killed warrior units
    //@input -  processedFormations - list of already processed formations
    public int score(ArrayList<Formation> processedFormations){
        int score = 0;
        //calculate the score value for own formation
        if(mRootedWarriors != null){
            for(WarriorInCombat warrior: mRootedWarriors){
                score = score + warrior.score();
            }
        }

        //calculate the score value for each sub-formation
        if(mSubFormations != null){
            for(Formation formation:mSubFormations.values()){
                if(processedFormations.contains(formation) == false){
                    if(formation.isStandalone() == false) {
                        score = score + ((FormationInCombat) formation).score(processedFormations);
                    }
                    processedFormations.add(formation);
                }
            }
        }

        return score;
    }

    // ------------------------- JSON Conversion ---------------------------
    //
    public int hash(){
        return mHash;
    }

    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        jo = super.convertToJSON();

        if(mSimilarity != null) {
            jo.put(SIMILARITY, mSimilarity);
        }

        if(mElephants != null){
            jo.put(HAS_ELEPHANTS, mElephants);
        }

        if(mActivated != null){
            jo.put(ACTIVATED_STR, mActivated);
        }

        //Put array of rooted warriors into JSON format
        if(mRootedWarriors != null){
            // Make an array in JSON format
            JSONArray jRootedWarriorArray = new JSONArray();

            for (WarriorInCombat warrior : mRootedWarriors) {
                jRootedWarriorArray.put(warrior.convertToJSON());
            }

            jo.put(ROOTED_WARRIORS, jRootedWarriorArray);
        }

        //add hash reference to this object too
        jo.put(HASH, this.hashCode());

        return jo;
    }
}
