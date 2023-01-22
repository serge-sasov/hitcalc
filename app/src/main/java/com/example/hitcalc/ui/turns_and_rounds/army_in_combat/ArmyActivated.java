package com.example.hitcalc.ui.turns_and_rounds.army_in_combat;

import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//The class will be used by turn step resolution to handle the processed warriors, i.e. remove a warrior item as it is used completely.
public class ArmyActivated extends ArmyInCombat {
    private ArmyInCombat mSourceArmy; //reference to the source army

    // ------------------------- JSON -----------------------
    private static final String SOURCE_ARMY = "source_army";
    //----------------------------------------------

    /**************** JSON Constructor *************************/
    public ArmyActivated(JSONObject jo, ArmyInCombat army, UnitWeaknessTable table) throws JSONException {
        super();

        mSourceArmy = army;
        mCivilizationTitle = jo.getString(CIVILIZATION_TITLE);

        //cast formations to FormationActivated
        JSONArray jArmyArray = jo.getJSONArray(ARMY);
        mArmy = new ArrayList<Formation>();

        for (int i = 0; i < jArmyArray.length(); i++) {
            mArmy.add(new FormationActivated(jArmyArray.getJSONObject(i), army, table));
        }
    }
    /**************** JSON Constructor *************************/

    //create a copy of activated army using the base army instance
    public ArmyActivated(ArmyInCombat army) {
        super();

        mSourceArmy = army;

        mArmy = new ArrayList<Formation>();
        //Create a copy of each formation
        for(Formation formation: army.getFormations()){
            mArmy.add(new FormationActivated((FormationInCombat) formation));
        }
    }

    //
    public ArmyActivated(String civil, ArmyInCombat army){
        super(civil);
        mSourceArmy = army;
    }

    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        return super.convertToJSON();

    }
}
