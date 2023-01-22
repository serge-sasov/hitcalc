package com.example.hitcalc.ui.combat_scenes.combat_pager.tables;

import com.example.hitcalc.utility.ErrorMessageTracker;
import com.example.hitcalc.utility.LoadTable;
import com.example.hitcalc.utility.ParseTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public class WeaponSystems extends ParseTable {
    protected String mAttackResult;
    protected String mAttacker, mDefender;
    private ErrorMessageTracker mErrorTracker = new ErrorMessageTracker();

    public WeaponSystems(LoadTable table, String attacker, String defender) throws IOException, CsvException {
        super(table);
        mAttacker = attacker;
        mDefender = defender;

        try {
            //Calculate the attack result
            getAttackResult(attacker, defender);
        }
        catch (Exception | Error e){
            mErrorTracker.appendLog(e.toString());
            e.printStackTrace();
        }
    }

    public WeaponSystems(LoadTable table) throws IOException, CsvException {
        super(table);
    }

    //return attack result for given attacker and defender types
    public void getAttackResult(String attacker, String defender){
        try {
            //get an attack result first
            mAttackResult = replaceNullThroughZero(getTableValueByGivenNames(attacker, defender));
        }
        catch (Exception | Error e){
            mErrorTracker.appendLog(e.toString());
            e.printStackTrace();
        }
    }

    //return in value of attack result
    public int getAttackResultInteger(){
        //Call the function to replace null value first and afterwards convert it into integer
        return Integer.parseInt(mAttackResult);
    }
}
