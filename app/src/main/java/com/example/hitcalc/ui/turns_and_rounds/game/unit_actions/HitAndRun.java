package com.example.hitcalc.ui.turns_and_rounds.game.unit_actions;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

//Hit & Run, Shock Combat, Distance Attack Defence, Forced Retreat, Retreat before Shock, Rampage Defence, Rampage
public class HitAndRun extends UnitAbstractAction {

    public HitAndRun(LoadTable table, WarriorInCombat warrior) throws IOException, CsvException {
        mWarrior = warrior;
        mTitle = "Hit & Run";
        mTitleId = R.string.hit_and_run;

        if(mWarrior.twoHexSize()){
            isDoubleSize = true;
        }

        //get weakness impact for the given action
        getImpact(table);
    }
}
