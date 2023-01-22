package com.example.hitcalc.ui.combat_scenes.combat_pager.tables;

import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public class FrontAttackTable extends WeaponSystems {

    public FrontAttackTable(LoadTable table, String attacker, String defender) throws CsvException, IOException {
        super(table, attacker, defender);
    }

    public FrontAttackTable(LoadTable table) throws CsvException, IOException {
        super(table);
    }

    //Check for unit (is currently applicable for Elephants only) whether the attack is allowed
    public boolean isAttackAllowed(){
        //evaluate the attack result;
        if(mAttackResult.equals("N/A")){
            return false;
        }
        return true;
    }

}
