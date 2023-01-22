package com.example.hitcalc.ui.turns_and_rounds.game.tables;

import com.example.hitcalc.ui.turns_and_rounds.game.unit_actions.UnitAbstractAction;
import com.example.hitcalc.utility.LoadTable;
import com.example.hitcalc.utility.ParseTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public class UnitActionTable extends ParseTable {
    private String mWarriorType;
    private UnitAbstractAction mAction;

    public UnitActionTable(LoadTable table, String type, UnitAbstractAction action) throws IOException, CsvException {
        super(table);
        mWarriorType = type;
        mAction = action;
    }

    public UnitActionTable(LoadTable table) throws IOException, CsvException {
        super(table);
    }

    //return attack result for given attacker and defender types
    public Integer getActionImpact(){
        String weaknessImpact;

        try {
            //get an attack result first
            weaknessImpact = replaceNotAllowedByMinusOne(getTableValueByGivenNames(mAction.getTitle(),mWarriorType));
            return Integer.parseInt(weaknessImpact);
        }
        catch (Exception | Error e){
            e.printStackTrace();
        }

        return -1;
    }
}
