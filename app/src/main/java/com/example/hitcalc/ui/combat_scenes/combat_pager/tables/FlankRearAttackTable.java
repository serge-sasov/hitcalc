package com.example.hitcalc.ui.combat_scenes.combat_pager.tables;

import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public class FlankRearAttackTable extends WeaponSystems {
    private String  mDefenderCategory;

    public FlankRearAttackTable(LoadTable table) throws CsvException, IOException {
        super(table);
    }

    public FlankRearAttackTable(LoadTable table, String attacker, String defender) throws CsvException, IOException {
        super(table, attacker, defender);
        mAttacker = attacker;
        mDefender = defender;

        getAttackResult(attacker, defender);
    }

    //return attack result for given attacker and defender types
    public void getAttackResult(String attacker, String defender){
        mDefenderCategory = getDefenderType(defender);
        //get an attack result first
        mAttackResult = replaceNullThroughZero(getTableValueByGivenNames(attacker, mDefenderCategory));
    }

    //Convert the defender to the row category Infantry or LN/LC if needed
    private String getDefenderType(String def){
        //  PH,HI,MI,LP,LI, LG
        if(def.equals("PH") ||
                def.equals("HI") ||
                def.equals("MI") ||
                def.equals("LP") ||
                def.equals("LG") ||
                def.equals("LI")){
            def = "Infantry";
        }
        //  Convert a given unit to united category title -> LN/LC
        if(def.equals("LN") || def.equals("LC")){
            def = "LN/LC";
        }

        // Convert a given unit to united category title -> HC/BC/RC
        if(def.equals("HC") || def.equals("BC") || def.equals("RC")){
            def = "HC/BC/RC";
        }

        return def;
    }
}
