package com.example.hitcalc.ui.turns_and_rounds.game.unit_actions;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public class RootAction extends UnitAbstractAction {
    private FormationInCombat mFormation;

    public RootAction(LoadTable table, WarriorInCombat warrior) throws IOException, CsvException {
        mTitle = "Root";
        mWarrior = warrior;
        mTitleId = R.string.root;
    }

    public RootAction(FormationInCombat formation, WarriorInCombat warrior) throws IOException, CsvException {
        mTitle = "Root";
        mWarrior = warrior;
        mFormation = formation;
        mTitleId = R.string.root;
    }

    //set the weakness value and applicability for given unit type
    protected void getImpact(LoadTable table) throws IOException, CsvException {
        mWeaknessImpact = mWarrior.getWeaknessLimit();
    }

    //apply a given action
    public void apply(){
        //change weakness
        mFormation.killWarrior(mWarrior);
    }

    //rollback a given action
    public void rollback(){
        //no rollback is applicable currently
        mFormation.recoverWarrior(mWarrior);
    }

    //Show applicability of the action for the given unit type
    @Override
    public Boolean isActionApplicable() {
        return true;
    }
}
