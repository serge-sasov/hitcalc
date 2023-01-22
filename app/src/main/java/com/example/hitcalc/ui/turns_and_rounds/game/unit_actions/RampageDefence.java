package com.example.hitcalc.ui.turns_and_rounds.game.unit_actions;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public class RampageDefence extends UnitAbstractAction {

    public RampageDefence(LoadTable table, WarriorInCombat warrior) throws IOException, CsvException {
        mWarrior = warrior;
        mTitle = "Rampage Defence";
        mTitleId = R.string.rampage_defence;

        //get weakness impact for the given action
        getImpact(table);
    }
}
