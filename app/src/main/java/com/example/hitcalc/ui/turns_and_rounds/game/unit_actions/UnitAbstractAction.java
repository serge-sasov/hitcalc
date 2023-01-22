package com.example.hitcalc.ui.turns_and_rounds.game.unit_actions;

import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitActionTable;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public abstract class UnitAbstractAction {
    protected Boolean isDoubleSize = false;
    protected WarriorInCombat mWarrior;
    protected String mTitle;
    protected Integer mTitleId;
    protected Integer mWeaknessImpact; //current weakness value to be applied for given action depending on unit type
    protected Integer mAppliedWeaknessValue = 0;
    protected Boolean isApplicable = false; //Show the action applicability for the given unit type

    //apply a given action
    public void apply(){
        //change weakness
        mWarrior.addWeakness(mWeaknessImpact);
        mAppliedWeaknessValue += mWeaknessImpact;

        //set spent flag
        mWarrior.spend();
    }

    //rollback a given action
    public void rollback(){
        mWarrior.rollbackWeakness(mWeaknessImpact);
    }

    //Get action title
    public String getTitle() {
        if(mTitle != null) {
            return mTitle;
        }
        return null;
    }

    //Get action title
    public Integer getTitleId() {
        if(mTitleId != null) {
            return mTitleId;
        }
        return null;
    }

    //set the weakness value and applicability for given unit type
    protected void getImpact(LoadTable table) throws IOException, CsvException {
        UnitActionTable unitActionTable = new UnitActionTable(table, mWarrior.type(), this);
        Integer result = unitActionTable.getActionImpact();
        if(result > 0){
            mWeaknessImpact = result;
            isApplicable = true; //the given action may be used by given unit type
        }
        else {
            mWeaknessImpact = 0;
        }
    }

    //Show applicability of the action for the given unit type
    public Boolean isActionApplicable() {
        return isApplicable;
    }

    public Boolean isDoubleSize() {
        return isDoubleSize;
    }

    public WarriorInCombat getWarrior() {
        return mWarrior;
    }
}
