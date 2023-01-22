package com.example.hitcalc.utility;

import android.os.Bundle;

import com.example.hitcalc.R;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class ParseTable {
    protected List<String[]> mTable;
    protected HashMap<String, Integer> mHeader;

    //Work with table data
    public ParseTable(LoadTable table) throws CsvException, IOException {
        mTable = table.getTable();
    }

    //Create header out of the first row
    public void parseHeader(){
        mHeader = new HashMap<String, Integer>();
        String [] headerItems = mTable.get(0);
        for(int i=0; i < headerItems.length; i++){
            mHeader.put(headerItems[i], i);
        }
    }

    /*
        Used for search in units table by title and type
     */
    public String [] getUnitItemByNameAndType(String title, String type){
        for(int i = 1; i < mTable.size(); i++){
            /*
                 1. compare to
                    - id [index = 0]
                    - title [index = 1]
                    - type [index = 2]
                 2. Starting from index 1 because we are not interesting to get a header of the table
             */
            if((mTable.get(i)[0].equals(title) || mTable.get(i)[1].equals(title) ) && mTable.get(i)[2].equals(type)){
                return mTable.get(i);
            }
        }

        return null;
    }

    /*
        Used for search in units table by matching unit's title or id
     */
    public String [] getUnitItemByIdOrName(String id){
        for(int i = 1; i < mTable.size(); i++){
            /*
                 1. compare to
                    - id [index = 0]
                    - title [index = 1]
                 2. Starting from index 1 because we are not interesting to get a header of the table
             */
            if(mTable.get(i)[0].equals(id) || mTable.get(i)[1].equals(id)){
                return mTable.get(i);
            }
        }

        return null;
    }

    //Return an Index on the given Row Title Name - e.g. "PH" -> 1 or -1 if not found
    protected int getRowIndexByName(String toMatch){
        for(int i = 1; i < mTable.size(); i++){
            /*
                 1. compare with the first value only
                 2. Starting from index 1 because we are not interesting to get a header of the table
             */
            if(mTable.get(i)[0].equals(toMatch)){
                return i;
            }
        }
        return -1;
    }

    //Return Row by given Row title (first element) or null value
    public String [] getRowByRowTitle(String title){
        for(int i = 1; i < mTable.size(); i++){
            /*
                 1. compare with the first value only
                 2. Starting from index 1 because we are not interesting to get a header of the table
             */
            if(mTable.get(i)[0].equals(title)){
                return mTable.get(i);
            }
        }
        return null;
    }

    //Return an Index on the given Row Title Name - e.g. "PH" -> 1 or -1 if not found
    protected int getColumnIndexByName(String toMatch){
        String [] header = mTable.get(0);
        int headerSize = header.length;
        for(int i = 1; i < headerSize; i++){
            /*
                 1. Starting from index 1 because we are not interesting to get a header of the table
             */
            if(header[i].equals(toMatch)){
                return i;
            }
        }
        return -1;
    }

    //Return a value on given column and row names
    public String getTableValueByGivenNames(String columnName, String rowName){
        int columnIndex = getColumnIndexByName(columnName);
        //Check for data result
        if(columnIndex == -1){
            return "column data not found";
        }


        int rowIndex = getRowIndexByName(rowName);
        //Check for data result
        if(rowIndex == -1){
            return "row data not found";
        }

        return mTable.get(rowIndex)[columnIndex];
    }

    //replace empty entry through "0"
    public String replaceNullThroughZero(String toReplaceValue){
        //Check for result
        if(toReplaceValue.isEmpty()){
            toReplaceValue = "0";
        }
        return toReplaceValue;
    }

    //replace empty entry through "0"
    public String replaceNotAllowedByMinusOne(String toReplaceValue){
        //Check for result
        if(toReplaceValue.equals("N/A")){
            toReplaceValue = "-1";
        }else if(toReplaceValue.equals("Free")){
            toReplaceValue = "0";
        }
        return toReplaceValue;
    }

    //replace empty entry through "0"
    public boolean convertToBoolean(String toReplaceValue){
        boolean result = true;
        //Check for result
        if(toReplaceValue.isEmpty()){
            result = false;
        }
        return result;
    }
}
