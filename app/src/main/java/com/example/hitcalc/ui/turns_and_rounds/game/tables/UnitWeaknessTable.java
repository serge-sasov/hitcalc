package com.example.hitcalc.ui.turns_and_rounds.game.tables;

import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect.AbstractWeaknessEffect;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect.CheckForRetreat;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect.CheckForRoot;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect.ExtraActionPoints;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect.Retreat;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.weakness_effect.Root;
import com.example.hitcalc.utility.LoadTable;
import com.example.hitcalc.utility.ParseTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class UnitWeaknessTable extends ParseTable {
    private HashMap<String, Integer> mRootValuePerUnitTypeList;

    private HashMap<String, HashMap<Integer, ArrayList<AbstractWeaknessEffect>>> mWeaknessEffectsPerUnitType;

    public UnitWeaknessTable(LoadTable table) throws CsvException, IOException {
        super(table);

        //Calculate for each given unit type its root weakness limit value
        mRootValuePerUnitTypeList = getRootValuePerUnitType();

        //Prepare WeaknessEffect
        mWeaknessEffectsPerUnitType = parseWeaknessEffects();
    }

    //Derive the root weakness limit value
    protected HashMap<String, Integer> getRootValuePerUnitType(){
        HashMap<String, Integer> rootValuePerUnitTypeList = new HashMap<String, Integer>();
        if(mTable != null) {
            //Get each row and calculate the unconditional root value
            for (int i = 1; i < mTable.size(); i++) {
                String type = mTable.get(i)[0];
                int weaknessLimit = 1;
                for (int j = 1; !mTable.get(i)[j].equals("Root"); j++) {
                    //take a correct value of the weakness limit out of the table header
                    String string = mTable.get(0)[j];
                    weaknessLimit = Integer.parseInt(mTable.get(0)[j]);
                }
                rootValuePerUnitTypeList.put(type, weaknessLimit);
            }

            return rootValuePerUnitTypeList;
        }
        return null;
    }

    public Integer getWeaknessLimitValue(String unitType){
        if(mRootValuePerUnitTypeList != null){
            return mRootValuePerUnitTypeList.get(unitType);
        }

        return null;
    }

    //Derive the root weakness limit value
    protected HashMap<String, HashMap<Integer, ArrayList<AbstractWeaknessEffect>>> parseWeaknessEffects(){
        HashMap<String, HashMap<Integer, ArrayList<AbstractWeaknessEffect>>> weaknessEffects =
                new HashMap<String, HashMap<Integer, ArrayList<AbstractWeaknessEffect>>>();
        if(mTable != null) {
            //create a mapping of the weakness effect available patterns
            HashMap<String, ArrayList<AbstractWeaknessEffect>> weaknessEffectPatterns = new HashMap<String, ArrayList<AbstractWeaknessEffect>>();

            //Get each row and calculate the unconditional root value
            for (int y = 1; y < mTable.size(); y++) {
                //get unit type
                String unitType = mTable.get(y)[0];

                HashMap<Integer, ArrayList<AbstractWeaknessEffect>> unitTypeWeaknessEffects = new  HashMap<Integer, ArrayList<AbstractWeaknessEffect>>();
                for (int x = 1; x < mTable.get(y).length; x++) {
                    String effectString = mTable.get(y)[x];

                    if(weaknessEffectPatterns.containsKey(effectString) == false){
                        weaknessEffectPatterns.put(effectString, calculateWeaknessEffect(effectString));
                    }
                    String stringValue = mTable.get(0)[x];
                    Integer weaknessThreshold = Integer.parseInt(stringValue);
                    //take a correct value of the weakness limit out of the table header
                    unitTypeWeaknessEffects.put(weaknessThreshold, weaknessEffectPatterns.get(effectString));
                    weaknessEffects.put(unitType, unitTypeWeaknessEffects);

                }
            }
            return weaknessEffects;
        }
        return null;
    }

    /*
    * determine weakness effect for given input combination
    * */
    private   ArrayList<AbstractWeaknessEffect> calculateWeaknessEffect(String rawEffect){
        String[] effectArray = rawEffect.split("/");
        ArrayList<AbstractWeaknessEffect> effects = new ArrayList<AbstractWeaknessEffect>();

        for(String effectString : effectArray){
            switch (effectString){
                case "No Effect": effects = null;
                    break;

                case "1": effects.add(new ExtraActionPoints(1));
                    break;

                case "2": effects.add(new ExtraActionPoints(2));
                    break;

                case "3": effects.add(new ExtraActionPoints(3));
                    break;

                case "4": effects.add(new ExtraActionPoints(4));
                    break;

                case "[Retreat]": effects.add(new CheckForRetreat());
                    break;

                case "[Retreat+2]": effects.add(new CheckForRetreat(2));
                    break;

                case "Retreat": effects.add(new Retreat());
                    break;

                case "[Root]": effects.add(new CheckForRoot());
                    break;

                case "[Root+2]": effects.add(new CheckForRoot(2));
                    break;

                case "Root": effects.add(new Root());
                    break;

                default:
                    break;
            }
        }
        return effects;
    }

    //Get weakness effect for given unit type and weakness value
    public ArrayList<AbstractWeaknessEffect> getWeaknessEffects(WarriorInCombat warrior) {
        String unitType = warrior.type();
        int weaknessValue = warrior.getWeakness();
        int weaknessThreshold = warrior.getWeaknessLimit();

        //recalculate weakness value for the oversize case, when the value exceeds the threshold too much (i.e. 10 instead of 7).
        if(weaknessThreshold + 1 < weaknessValue){
            weaknessValue = weaknessThreshold + 1;
        }

        if(warrior.twoHexSize()){
            //in case this is a a two hex unit its common weakness value is to be reduced twice and rounded down
            weaknessValue = (int) weaknessValue/2;
        }

        return mWeaknessEffectsPerUnitType.get(unitType).get(weaknessValue);
    }
}
