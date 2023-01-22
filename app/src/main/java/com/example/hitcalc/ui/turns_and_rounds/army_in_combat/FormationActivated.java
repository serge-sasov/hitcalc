package com.example.hitcalc.ui.turns_and_rounds.army_in_combat;

import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

//the class is used for building a navigation view and representation of warriors still not processes while weakness/combat resolution.
public class FormationActivated extends FormationInCombat{
    private FormationInCombat mSourceFormation;
    private ArrayList<WarriorInShock> mActivatedWarriors;

    // ------------------------- JSON -----------------------
    private static final String SOURCE_FORMATION = "source_formation";
    private static final String ACTIVATED_WARRIORS = "activated_warriors";
    //----------------------------------------------

    public FormationActivated(FormationInCombat formation){
        super(formation);

        //persist reference to the source formation
        mSourceFormation = formation;

        //we have to handle two data sets root-layer and sub-formations warriors.
        if(formation.warriors() != null) {
            //instantiate a new array instance
            mWarriors = new ArrayList<WarriorItem>();
            mWarriors.addAll(formation.warriors());
        }

        //cast FormationInCombat -> FormationActivated
        if(formation.subFormations() != null) {
            mSubFormations = castSubFormations(formation);
        }
    }

    //cast sub formations from cast FormationInCombat to FormationActivated
    private HashMap<String, Formation> castSubFormations(FormationInCombat formation){
        HashMap<String, Formation> subFormations = null;

        if(formation.subFormations() != null){
            //instantiate a new sub-formation mapping
            subFormations = new HashMap<String, Formation>();
            for(String title: formation.subFormations().keySet()){
                subFormations.put(title, new FormationActivated((FormationInCombat) formation.subFormations().get(title)));
            }
        }

        return subFormations;
    }

    /**************** JSON Constructor *************************/
    public FormationActivated(JSONObject jo, ArmyInCombat army, UnitWeaknessTable table) throws JSONException {
        super(jo, table); //derive current structure of the warriors

        if(jo.has(SOURCE_FORMATION) && army != null) {
            int hash = jo.getInt(SOURCE_FORMATION);

            //cast FormationInCombat -> FormationActivated
            if(mSubFormations != null) {
                mSubFormations = castSubFormations(this);
            }

            //recover the original reference to the object using the persisted hash code
            for(Formation formation : army.getFormations()) {
                if(((FormationInCombat) formation).hash() == hash) {
                    mSourceFormation = (FormationInCombat) formation;
                }
            }
        }

        //need to rebuild an object reference of each warrior to its source object
        rebuildFormationReference();
    }

    //rebuild a reference to the source warrior object derived from JSON backup
    public void rebuildWarriorReference(ArrayList<WarriorItem> sourceWarriors){
        //need to rebuild an object reference of each warrior to its source object
        if(mWarriors != null && sourceWarriors != null) {
            ArrayList<WarriorItem> warriors = new ArrayList<WarriorItem>();
            for (WarriorItem warrior : mWarriors){
                for (WarriorItem sourceWarrior : sourceWarriors) {
                    if(warrior.title().equals(sourceWarrior.title())){
                        warriors.add(sourceWarrior);
                    }
                }
            }
            mWarriors = warriors;
        }
    }

    //rebuild a whole formation
    private void rebuildFormationReference(){
        rebuildWarriorReference(mSourceFormation.warriors());

        if(mSubFormations != null && mSourceFormation.subFormations() != null){
            for(String titleActivated: mSubFormations.keySet()){
                ArrayList<WarriorItem> sourceWarriors = mSourceFormation.subFormations().get(titleActivated).warriors();
                ((FormationActivated) mSubFormations.get(titleActivated)).rebuildWarriorReference(sourceWarriors);
            }
        }
    }

    /**************** JSON Constructor *************************/

    //replace the given warrior set throw the new list of activated units
    public void setMainWarriors(ArrayList<WarriorInCombat> warriors){
        ArrayList<WarriorItem> fullWarriors = mWarriors;
        mWarriors = new ArrayList<WarriorItem>();

        //copy harizma & leader to new array
        WarriorInCombat leader, harizma;
        for(WarriorItem warrior: fullWarriors){
            if(warrior.type().equals("Leader") || warrior.type().equals("Harizma")){
                mWarriors.add((WarriorInCombat) warrior);
            }
        }

        for(WarriorInCombat warrior:warriors){
            //cast to warrior item
            mWarriors.add((WarriorItem) warrior);
        }
    }

    //replace the given warrior set throw the new list of activated units
    public void setSubFormationWarriors(HashMap<String, ArrayList<WarriorInCombat>> warriors){
        HashMap<String, Formation> subFormations = new HashMap<String, Formation>();
        for(String title: mSubFormations.keySet()){
            if(warriors.get(title) != null){
                ((FormationActivated) mSubFormations.get(title)).setMainWarriors(warriors.get(title));
                subFormations.put(title, mSubFormations.get(title));
            }
        }
        //replace old structure with the relevant data
        mSubFormations = subFormations;
    }

    //return a reference to the source parent formation the given formation was made of.
    public FormationInCombat source(){
        return mSourceFormation;
    }

    //activate root & the given sub-formations in the parent source formation
    public void activate(){
        //activate root formations
        mSourceFormation.activate();

        //activate all given sub-formation
        if(mSubFormations != null && mSubFormations.size() > 0){
            for(String title : mSubFormations.keySet()){
                ((FormationInCombat) mSourceFormation.subFormations().get(title)).activate();
            }
        }
    }

    //put a warrior into activated warrior list
    public void activate(WarriorInShock warrior){
        if(mActivatedWarriors == null){
            mActivatedWarriors = new ArrayList<WarriorInShock>();
        }

        mActivatedWarriors.add(warrior);
    }

    //Remove manually a warrior from the alive warrior set to the rooted one
    @Override
    public void killWarrior(WarriorInCombat warrior){
        super.killWarrior(warrior);

        //remove a warrior in the source formation
        mSourceFormation.killWarrior(warrior);

    }

    //return a list of not activated warriors
    @Override
    public ArrayList<WarriorItem> warriors(){
        //build a copy of warrior list
        ArrayList<WarriorItem> warriors = new ArrayList<WarriorItem>();
        warriors.addAll(mWarriors);

        for(WarriorItem warrior : mWarriors){
            if(mActivatedWarriors != null && mActivatedWarriors.contains(warrior)){
                //remove activated unit from the list of available warriors
                warriors.remove(warrior);
            }
        }

        return warriors;
    }

    //show all available warriors in the activated formation
    public ArrayList<WarriorItem> allWarriors(){
        //build a copy of warrior list
        return mWarriors;
    }


    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        JSONObject jo = new JSONObject();
        //jo = super.convertToJSON();

        //Store a reference to the source object
        if(mSourceFormation != null) {

            int hash = mSourceFormation.hashCode();
            jo.put(SOURCE_FORMATION, hash);
        }

        if(mActivatedWarriors != null){
            JSONArray jRootedWarriorArray = new JSONArray();

            //put hash code of activated warriors
            for (WarriorInShock warrior : mActivatedWarriors) {
                jRootedWarriorArray.put(warrior.hashCode());
            }

            jo.put(ACTIVATED_WARRIORS, jRootedWarriorArray);
        }
        return jo;
    }
}
