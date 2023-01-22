package com.example.hitcalc.ui.turns_and_rounds.calculator;

import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.combat_scenes.combat_pager.tables.FlankRearAttackTable;
import com.example.hitcalc.ui.combat_scenes.combat_pager.tables.FrontAttackTable;
import com.example.hitcalc.ui.combat_scenes.combat_pager.tables.WeaponSystems;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.combat_scenes.map.Map;
import com.example.hitcalc.ui.combat_scenes.map.MapView;
import com.example.hitcalc.ui.turns_and_rounds.game.Game;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.ArrayList;

public class ShockCombatCalculator {
    private GameStorage mGameStorage;
    private Map mMap;
    private MapView mMapView;

    private boolean mIsSimilarType = false; //Shows whether all units involved in the battle are similar type

    private WarriorInShock mDefender, mAddDefender;
    private ArrayList<WarriorInShock> mFrontAttackers, mRearFlangAttackers;

    private LoadTable mFrontAttackTable, mRearFlankAttackTable;
    private int mAggregatedSize = 0;
    private int mLeadTroopQuality = 0;
    private String mLeadTitle;
    private int mWeaponSystemBestResult = -10; //set to unreal low value
    private int mShockCombatCalculationResult; //complete aggregate outcome of all modifiers implying on the combat
    private WarriorInShock mBestAttackerUnit; //Best lead unit
    private WarriorInShock mPreferableAttackingUnit;
    private int mAttackerLeaderCount = 0;

    public ShockCombatCalculator(GameStorage gameStorage, Game game, WarriorInShock preferedAttacker) throws IOException, CsvException {
        mMap = game.mapView().map();

        WarriorInShock defender = mMap.retrieveWarriorFromHex(1, 1); //Hardcoded - Defender hex
        WarriorInShock addDefender = mMap.retrieveWarriorFromHex(2, 1); //Hardcoded - additional defender hex - just to use for Rome

        ArrayList<WarriorInShock> frontAttackers = new ArrayList<WarriorInShock>();
        ArrayList<WarriorInShock> rearFlangAttackers = new ArrayList<WarriorInShock>();
        int shockCalcResult = 0;


        for (int y = 0; y < Map.getHexViewIds().length; y++) {
            for (int x = 0; x < Map.getHexViewIds()[y].length; x++) {
                if (mMap.retrieveWarriorFromHex(x, y) != null) {
                    if (Map.getHexTypes()[y][x].equals("Front")) {
                        frontAttackers.add(mMap.retrieveWarriorFromHex(x, y));
                    }

                    if (Map.getHexTypes()[y][x].equals("Rear") || Map.getHexTypes()[y][x].equals("Flank")) {
                        rearFlangAttackers.add(mMap.retrieveWarriorFromHex(x, y));
                    }
                }
            }
        }
        Integer terrainModifier = mMap.terrainModifier();

        //Tables
        mFrontAttackTable = gameStorage.getFrontAttackTable();
        mRearFlankAttackTable = gameStorage.getFlankRearAttackTable();

        //Defenders
        mDefender = defender;
        mAddDefender = addDefender;

        //Preferable attacker if any
        mPreferableAttackingUnit = preferedAttacker;

        //Front Attackers
        mFrontAttackers = frontAttackers;

        //Rear and Flank Attackers
        mRearFlangAttackers = rearFlangAttackers;

        /*
        Step 0. Check for unit types involved in the strike and exclude EL, SK and CH size
        from calculation unless all of the units are the same type
        * */
        mIsSimilarType = isAllUnitsSimilarType();

        /*
         Step 1. Determine best result from weapon system tables - mWeaponSystemBestResult
         */
        determineWeaponSystem();

        /*
        Step 2. Estimate Troop Quality - attacker TQ - defender TQ
         */
        int deltaTroopQuality = mLeadTroopQuality - mDefender.troopQuality();
        if(deltaTroopQuality < -3){
            deltaTroopQuality = -3;
        }
        else if(deltaTroopQuality >3){
            deltaTroopQuality = 3;
        }

        /*
        * Step 3: Estimate Troop Size
         */

        int defenderSize = 0;
        if(mIsSimilarType == false){
            if(!mDefender.type().equals("CH")
                    &&!mDefender.type().equals("SK")
                    &&!mDefender.type().equals("EL")){
                defenderSize = getDefenderSize();
            }
        }else{
            defenderSize = getDefenderSize();
        }


        //Check if there is any charriots, elephants or skirmishes in the battle, if so, ignore the size rating
        int unitSizeComparePoints = 0;
        if(isSizeRatingApplicable()){
            unitSizeComparePoints = getSizePoints(mAggregatedSize, defenderSize);
        }

        /*
        *  Step 4: Check for commander presence
         */
        if(mDefender.leader() != null || mDefender.harizma() != null){
            mAttackerLeaderCount = mAttackerLeaderCount - 1;
        }

        if(mAddDefender != null) {
            if (mAddDefender.leader() != null || mAddDefender.harizma() != null) {
                mAttackerLeaderCount = mAttackerLeaderCount - 1;
            }
        }

        /*
        * Step 5: Terrain check
         */
        //tbd

        //Final calculation of the whole set of modifiers
        mShockCombatCalculationResult = mWeaponSystemBestResult + mAttackerLeaderCount + deltaTroopQuality + unitSizeComparePoints + terrainModifier;
    }

    //return a list of defending warriors
    public ArrayList<WarriorInShock> defenders(){
        ArrayList<WarriorInShock> defenders = new ArrayList<WarriorInShock>();
        defenders.add(mDefender);

        //consider stoked warriors
        if(mDefender.stokedWarrior() != null){
            defenders.add((WarriorInShock) mDefender.stokedWarrior());
        }

        if(mAddDefender != null) {
            //consider stoked warriors
            defenders.add(mAddDefender);

            if(mAddDefender.stokedWarrior() != null){
                defenders.add((WarriorInShock) mAddDefender.stokedWarrior());
            }
        }

        return defenders;
    }

    //return a list of attacking warriors
    public ArrayList<WarriorInShock> attackers(){
        ArrayList<WarriorInShock> attackers = new ArrayList<WarriorInShock>();
        if(mFrontAttackers != null && mFrontAttackers.size() > 0) {
            attackers.addAll(mFrontAttackers);

            //need to consider also attached warriors
            for(WarriorInShock warrior: mFrontAttackers){
                if(warrior.stokedWarrior() != null){
                    attackers.add((WarriorInShock) warrior.stokedWarrior());
                }
            }
        }

        if(mRearFlangAttackers != null && mRearFlangAttackers.size() > 0) {
            attackers.addAll(mRearFlangAttackers);
            //need to consider also attached warriors
            for(WarriorInShock warrior: mRearFlangAttackers){
                if(warrior.stokedWarrior() != null){
                    attackers.add((WarriorInShock) warrior.stokedWarrior());
                }
            }
        }

        return attackers;
    }

    //return a whole warrior list participating in the battle scene
    public ArrayList<WarriorInShock> allWarriors(){
        ArrayList<WarriorInShock> allWarriors = new ArrayList<WarriorInShock>();
        allWarriors.addAll(attackers());
        allWarriors.addAll(defenders());

        return allWarriors;
    }

    /*
    * Calculate full defender size
    * */
    private Integer getDefenderSize(){
        if(mAddDefender != null){
            return mDefender.size() + mAddDefender.size();
        }
        return mDefender.size();
    }

    //If all are similar type -> return true, otherwise return false
    public boolean isAllUnitsSimilarType() {
        String type = mDefender.type();

        //Check first for Front Attackers
        for (WarriorItem warrior : mFrontAttackers) {
            if (!type.equals(warrior.type())) {
                return false;
            }
        }

        //Check afterwards for Rear and Flank Attackers
        for (WarriorItem warrior : mRearFlangAttackers) {
            if (!type.equals(warrior.type())) {
                return false;
            }
        }

        return true;
    }

    //If all are similar type -> return true, otherwise return false
    private boolean isSizeRatingApplicable() {

        if(checkSizeRatingApplicability(mDefender.type()) == false){
            return false;
        }

        //Check first for Front Attackers
        for (WarriorItem warrior : mFrontAttackers) {
            if (checkSizeRatingApplicability(warrior.type()) == false) {
                return false;
            }
        }

        //Check afterwards for Rear and Flank Attackers
        for (WarriorItem warrior : mRearFlangAttackers) {
            if (checkSizeRatingApplicability(warrior.type()) == false) {
                return false;
            }
        }

        return true;
    }

    private boolean checkSizeRatingApplicability(String type) {
        if (type.equals("CH") || type.equals("SK") || type.equals("EL")) {
            return false;
        }

        return true;
    }


    //Evaluate Weapon System - get the highest value for all possible combinations
    /*
    * To do this we need to
    * 1. check against all possible combinations the best possible result
    * 2. if any is equal, then to check theirs troop quality to determine the best solution
    * */
    private void determineWeaponSystem() throws IOException, CsvException {
        //Evaluate the front attack results
        checkForBetterWeaponSystem(mFrontAttackers, new FrontAttackTable(mFrontAttackTable));
        checkForBetterWeaponSystem(mRearFlangAttackers, new FlankRearAttackTable(mRearFlankAttackTable));
    }

    //Estimate best choice for multiple imput
    private void checkForBetterWeaponSystem(ArrayList<WarriorInShock> attackers, WeaponSystems table){
        //Calculate first for flank attacks
        if(attackers.size() > 0){
            for (WarriorInShock attacker : attackers){
                table.getAttackResult(attacker.shockCombatType(), mDefender.shockCombatType());
                //First check for highest value
                if (table.getAttackResultInteger() > mWeaponSystemBestResult ||
                        //In peer case check the  Troop Quality and select the best one
                        (table.getAttackResultInteger() == mWeaponSystemBestResult &&
                                mLeadTroopQuality < attacker.troopQuality())){
                    //Set new best results values
                    if(mPreferableAttackingUnit != null){
                        //If preferable unit is defined, calculate best result only for that unit
                        if(mPreferableAttackingUnit.title().equals(attacker.title())){
                            mWeaponSystemBestResult = table.getAttackResultInteger();
                        }
                    }
                    else{
                        //if no preferable unit is defined find out the best result across all attacking units
                        mWeaponSystemBestResult = table.getAttackResultInteger();
                    }
                    mLeadTroopQuality = attacker.troopQuality();
                    mLeadTitle = attacker.title();
                    mBestAttackerUnit = attacker;
                }
                //calculate the leader impact
                if(attacker.leader() != null || attacker.harizma() != null){
                    mAttackerLeaderCount++;
                }

                //Aggregate the attacker size if they are
                if(mIsSimilarType == false){
                    if(!attacker.type().equals("CH")
                        &&!attacker.type().equals("SK")
                        &&!attacker.type().equals("EL")) {
                        //Calculate the size for all units to the exception of CK, EL and CH
                        mAggregatedSize += attacker.size();
                    }
                }
                else{
                    mAggregatedSize += attacker.size();
                }
            }
        }

        //if preferable attacking unit is provided then reassign all values according to that unit
        if(mPreferableAttackingUnit != null){
            mBestAttackerUnit = mPreferableAttackingUnit;
            mLeadTroopQuality = mPreferableAttackingUnit.troopQuality();
            mLeadTitle = mPreferableAttackingUnit.title();
        }
    }

    /*
    * Calculate Size delta points based on the input data
    * */

    private int getSizeDelta(int largetSize, int smallerSize){
        int sizePoints = 0;

        //Concider the case, when unit has got no considerable size at all
        if(smallerSize == 0){
            return sizePoints = 2;
        }

        if(largetSize/smallerSize >= 2){
            sizePoints = 2;
        }
        else if(largetSize - smallerSize >= 2){
            sizePoints = 1;
        }

        return sizePoints;
    }

    private int getSizePoints(int attackerSize, int defenderSize){
        int sizePoints = 0;
        if(attackerSize > defenderSize){
            sizePoints = getSizeDelta(attackerSize, defenderSize);
        }
        else if(attackerSize < defenderSize){
            //Inverse the calculation for the case when attecker size is smaller as defender one
            sizePoints = -1 * getSizeDelta(defenderSize, attackerSize);
        }

        return sizePoints;
    }

    //get a result of shock combat
    public int getShockCombatCalculationResult() {
        return mShockCombatCalculationResult;
    }

    public WarriorItem getBestAttackerUnit(){
        return mBestAttackerUnit;
    }

    public WarriorItem getDefenderUnit(){
        return mDefender;
    }
}
