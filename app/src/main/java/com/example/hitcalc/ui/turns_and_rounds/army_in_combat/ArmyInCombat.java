package com.example.hitcalc.ui.turns_and_rounds.army_in_combat;

import com.example.hitcalc.ui.combat_scenes.army.Army;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;
import com.example.hitcalc.utility.LoadTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ArmyInCombat extends Army {
    private ArrayList<FormationInCombat> mRootedFormations;
    private UnitWeaknessTable mTmpTable; //tmp table

    // ------------------------- JSON -----------------------
    private static final String ROOTED_FORMATIONS = "rooted_formations";
    // ------------------------- JSON -----------------------

    /**************** JSON Constructor *************************/
    public ArmyInCombat(JSONObject jo, UnitWeaknessTable table) throws JSONException {
        super(jo);
        mTmpTable = table; //store temporary data

        //Get all formations and put them into army array
        parseJSONArmy(jo);

        if(jo.has(ROOTED_FORMATIONS)) {
            // Get an array in JSON format
            JSONArray jMembersArray = jo.getJSONArray(ROOTED_FORMATIONS);

            //Create new formation member array and populate it
            mRootedFormations = new ArrayList<FormationInCombat>();

            for (int i = 0; i < jMembersArray.length(); i++) {
                mRootedFormations.add(new FormationInCombat(jMembersArray.getJSONObject(i), table));
            }
        }

        mTmpTable = null; //clean up tmp table
    }

    //dummy constructor, is needed for child class ArmyActivated
    public ArmyInCombat(){}

    public ArmyInCombat(String civil){
        super(civil);
    }

    /**************** JSON Constructor *************************/

    public ArmyInCombat(Army army, UnitWeaknessTable unitWeaknessTable){
        super();
        /*
         * Create a clone of existing army instance
         * */
        mArmy = new ArrayList<Formation>();
        mCivilizationTitle = army.getCivilizationTitle();

        //build up a common formation list
        HashMap<String, FormationInCombat> allFormations = new HashMap<String, FormationInCombat>();

        //Step 1: Determine all parent formations and create a copy of them
        for(Formation formation: army.getFormations()){
            FormationInCombat formationInCombat;
                //Copy only parent formation
            if(formation.isStandalone() && formation.subFormations() == null) {
                formationInCombat = new FormationInCombat(formation, unitWeaknessTable);
                mArmy.add(formationInCombat);

                allFormations.put(formationInCombat.getLeaderOrFormationTitle(), formationInCombat);
            }else if(formation.isStandalone() && formation.subFormations() != null){
                //check if the formation is already in the list
                Collection<Formation> subFormations = formation.subFormations().values();
                if(subFormations != null && subFormations.size() > 0){
                    for(Formation subFormation : subFormations){
                        Boolean existingFormation = false;
                        for(String title : allFormations.keySet()){
                            if(subFormation.getLeaderOrFormationTitle().equals(title)){
                                existingFormation = true;
                            }
                        }

                        if(existingFormation == false){
                            //if the entire list  does not still contain any given sub-formation, then add it to the list
                            formationInCombat = new FormationInCombat(subFormation, unitWeaknessTable);
                            allFormations.put(subFormation.getLeaderOrFormationTitle(), formationInCombat);
                        }
                    }
                }
            }
        }

        //Step 2: Determine all combined formations having nested sub-formations and create a copy of them with reference to the write parent formation
        for(Formation formation: army.getFormations()){
            if(formation.isStandalone() && formation.subFormations() != null) {
                //For each found sub-formation create a copy
                //mArmy.add(new FormationInCombat(formation, this, unitWeaknessTable));
                mArmy.add(new FormationInCombat(formation, allFormations, unitWeaknessTable));
            }
        }
    }
    @Override
    //used as separated method to make overriding possible
    protected void parseJSONArmy(JSONObject jo) throws JSONException {
        //Get all formations and put them into army array
        JSONArray jArmyArray = jo.getJSONArray(ARMY);
        mArmy = new ArrayList<Formation>();

        //build a reference mapping to the existing formations

        HashMap<String, FormationInCombat> allFormations = new HashMap<String, FormationInCombat>();
        for (int i = 0; i < jArmyArray.length(); i++) {
            FormationInCombat formation = new FormationInCombat(jArmyArray.getJSONObject(i), mTmpTable);

            //for simple formation populate the list
            mArmy.add(formation);
            allFormations.put(formation.getLeaderOrFormationTitle(), formation);

            if(formation.subFormations() != null) {
                //add new items to the list if any
                for(Formation subFormation: formation.subFormations().values()){
                    Boolean isPresent = false;
                    for(String title : allFormations.keySet()){
                        if(subFormation.getLeaderOrFormationTitle().equals(title)){
                            isPresent = true;
                        }
                    }

                    if(isPresent == false){
                        allFormations.put(subFormation.getLeaderOrFormationTitle(), (FormationInCombat) subFormation);
                    }
                }
            }
        }

        //process todo list
        for(Formation formation : mArmy){
            //rebuild sub-formation reference
            ((FormationInCombat) formation).revise(allFormations);
        }
    }


    //update warriors activation thresholds
    public void resetActivationThresholds(){
        for(Formation formation: mArmy){
            //reset threshold
            ((FormationInCombat) formation).updateActivationThreshold();
        }
    }

    //reset to the initial value the warriors activation state
    public void resetActivationState(){
        for(Formation formation: mArmy){
            //reset activation state
            ((FormationInCombat) formation).deactivate(true);
        }
    }

    //Calculate applicable effect for the given army warriors
    public HashMap<String, AffectedFormationWarriors> calculateRoundCompletionEffect(Random randomD10){
        HashMap<String, AffectedFormationWarriors> affectedArmy = null;

        for(Formation formation:mArmy){
            AffectedFormationWarriors affectedFormationWarriors = ((FormationInCombat) formation).calculateRoundCompletionEffect(randomD10);
            if(affectedFormationWarriors != null){
                if(affectedArmy == null){
                    affectedArmy = new HashMap<String, AffectedFormationWarriors>();
                }

                affectedArmy.put(formation.getLeaderOrFormationTitle(), affectedFormationWarriors);
            }
        }

        cleanUpRootedFormations();

        return affectedArmy;
    }

    //Remove rooted formations from the list
    public void cleanUpRootedFormations(){
        HashMap<String, AffectedFormationWarriors> affectedArmy = null;

        for(Formation formation:mArmy){
            //if formation is rooted, remove it out of the available formations list
            if(((FormationInCombat) formation).isRooted() == true){
                if(mRootedFormations == null){
                    mRootedFormations = new ArrayList<FormationInCombat>();
                }

                mRootedFormations.add((FormationInCombat) formation);
            };
        }
        //remove destroyed formation from the army
        if(mRootedFormations != null) {
            for (FormationInCombat rootedFormation : mRootedFormations) {
                if (mArmy.contains(rootedFormation)) {
                    mArmy.remove(rootedFormation);
                    //remove the same formation out of the any found sub-formations
                    for(Formation formation : mArmy){
                        if(((FormationInCombat) formation).subFormations() != null){
                            for(String title: formation.subFormations().keySet()) {
                                if (formation.subFormations().get(title).equals(rootedFormation)) {
                                    formation.subFormations().remove(title);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //calculate a score value for killed warrior of the army
    public int score(){
        int value = 0;
        //list of already processed formations
        ArrayList<Formation> processedFormations = new ArrayList<Formation>();

        if(mRootedFormations != null){
            for(FormationInCombat formation: mRootedFormations){
                value = value + formation.score(processedFormations);
            }
        }


        if(mArmy != null){
            for(Formation formation: mArmy){
                /*
                    a sub-formation may be in two different parent formations, so the filter is to be applied to
                    get rid of already counted sub/root formations
                 */
                if(processedFormations.contains(formation) == false){
                    value = value + ((FormationInCombat) formation).score(processedFormations);
                    processedFormations.add(formation);
                }
            }
        }
        return value;
    }

    // ------------------------- JSON Conversion ---------------------------
    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        jo = super.convertToJSON();

        //Put array of rooted formations into JSON format
        if(mRootedFormations != null){
            // Make an array in JSON format
            JSONArray jArray = new JSONArray();

            for (FormationInCombat formation : mRootedFormations) {
                jArray.put(formation.convertToJSON());
            }

            jo.put(ROOTED_FORMATIONS, jArray);
        }

        return jo;
    }
}
