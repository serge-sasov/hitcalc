package com.example.hitcalc.utility;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class LoadTable {
    protected List<String[]> mTable;

    public LoadTable(InputStream csvStream) throws CsvException, IOException {
        loadCSVTable(csvStream);
    }

    //Allow to reload the method in the child classes
    protected void loadCSVTable(InputStream csvStream){
        /* Load CSV setting file */
        InputStreamReader csvStreamReader = new InputStreamReader(csvStream);

        CSVReader csvReader = new CSVReader(csvStreamReader);

        try {
            mTable = csvReader.readAll();
        } catch (
                IOException e) {
            e.printStackTrace();
        } catch (
                CsvValidationException e) {
            e.printStackTrace();
        } catch (
                CsvException e) {
            e.printStackTrace();
        }
    }

    public List<String[]> getTable(){
        return mTable;
    }
}
