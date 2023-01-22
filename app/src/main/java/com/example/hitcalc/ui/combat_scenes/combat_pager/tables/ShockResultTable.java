package com.example.hitcalc.ui.combat_scenes.combat_pager.tables;

import com.example.hitcalc.utility.LoadTable;
import com.example.hitcalc.utility.ParseTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

public class ShockResultTable extends ParseTable {
    //DR,HIT,Retreat,DR Rout,Extra DR,Rout
    private boolean mRetreat = false; // No retreat by default
    private boolean mIsDRForRout = false; // No need to make a check against of root
    private int mExtraPointsForRout = 0; //by default there is no extra points if rout happens
    private boolean mIsRout = false; //No Rout by default
    private int mHit = 0; //number of Hits a unit receives as result of a combat
    private int mInputDiceRoll; //Input data to calculate the result of a combat

    public ShockResultTable(LoadTable table, int inputDRValue) throws CsvException, IOException {
        super(table);
        //Convert the input value to the given boundaries -3 .. 12
        if(inputDRValue < -3){
            inputDRValue = -3;
        }else if(inputDRValue > 12){
            inputDRValue = 12;
        }

        String stringInputDRValue = "" + inputDRValue;
        mRetreat = convertToBoolean(getTableValueByGivenNames("Retreat",stringInputDRValue));
        mIsDRForRout = convertToBoolean(getTableValueByGivenNames("DR Rout",stringInputDRValue));


        if(mIsDRForRout){
            String extraPoints =  replaceNullThroughZero(getTableValueByGivenNames("Extra DR",stringInputDRValue));
            mExtraPointsForRout = Integer.parseInt(extraPoints);
        }

        mInputDiceRoll = inputDRValue;
        String hit = replaceNullThroughZero(getTableValueByGivenNames("HIT",stringInputDRValue));

        mHit = Integer.parseInt(hit);
        mIsRout = convertToBoolean(getTableValueByGivenNames("Rout",stringInputDRValue));
    }

    //Unit gets a hit
    public int hits() {
        return mHit;
    }

    //Unit retreats
    public boolean retreat(){
        return mRetreat;
    }

    //Unit is rooted
    public boolean rout(){
        return mIsRout;
    }


    //Check for root
    public int extraPoints(){
        return mExtraPointsForRout;
    }

    public boolean routCheck(){
        return mIsDRForRout;
    }
}
