package com.example.hitcalc.ui.combat_scenes.army.parser;


import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.utility.LoadTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class ArmyParser {
    private final LoadTable mScenarioTable;
    private final LoadTable mUnitsTable;
    private HashMap<String, ArmyEntryParser> mRowFormationList; //a mapping between input entries and formation pre-parse
    private HashMap<String, String> mFormationToCivilMap; // a mapping between formation and civil
    private HashMap<String, Formation> mFormationMap;
    private Collection<ArmyEntryParser> mArmyEntries;
    private ArrayList<String> mCivilList; //A list of civilizations participating in the battle scenario
    private HashMap<String, ArrayList<Formation>> mCivilToFormationMap; //mapping of civil to its formations


    public ArmyParser(LoadTable scenarioTable, LoadTable unitTable) throws IOException, CsvException{
        mScenarioTable = scenarioTable;
        mUnitsTable = unitTable;
        List<String []> table = mScenarioTable.getTable();
        mRowFormationList = new HashMap<String, ArmyEntryParser>();
        String [] entry; // raw table entry
        String key; // raw table entry key
        mFormationMap = new HashMap<String, Formation>();
        mFormationToCivilMap = new HashMap<String, String>();

        //Step 1: Create an list of row formations
        for(int i=0; i < table.size(); i++) {
            ArmyEntryParser armyEntryParser;
            entry = table.get(i);
            key = table.get(i)[0];

            //Update or populate the list
            if(mRowFormationList.get(key) == null){
                armyEntryParser = new ArmyEntryParser(unitTable);

            }else{
                armyEntryParser = mRowFormationList.get(key);
            }
            armyEntryParser.addEntry(entry);
            mRowFormationList.put(key, armyEntryParser);
        }

        mCivilList = new ArrayList<String>();

        //Step 2: pre-parse initial row formation data
        mArmyEntries = mRowFormationList.values();

        mCivilToFormationMap = new HashMap<String, ArrayList<Formation>>();

        for(ArmyEntryParser entryItem : mArmyEntries) {
            ArrayList<Formation> formationList;

            entryItem.parseRowEntryData();
            Formation formation = entryItem.getFormation();
            String civil = entryItem.getArmyTitle();

            //get a formation list of given civil
            formationList = mCivilToFormationMap.get(civil);
            if(formationList == null){
                formationList = new ArrayList<Formation>();
            }
            formationList.add(formation);
            mCivilToFormationMap.put(civil, formationList);


            //Create an array of civils participating in the battle scenario
            if(!mCivilList.contains(entryItem.getArmyTitle())){
                mCivilList.add(entryItem.getArmyTitle());
            }
        }
        //Step 3. Populate sub-formation in each formation
        for(ArmyEntryParser armyEntry : mArmyEntries) {
            //in case there is any number of sub-formation, populate it with prepared formation data
            if(armyEntry.getAssociationWithSubFormations() != null && armyEntry.isStandalone()){
                ArrayList<Formation> formationList = getFormationTitlesOfGivenCivil(armyEntry.getArmyTitle());

                armyEntry.populateSubFormations(formationList); //<- Send here list of formations
            }
        }
    }

    //return all Civil parties of the scenario
    public ArrayList<String> getCivilList(){
        return mCivilList;
    }

    //return a formation list for given civil
    public ArrayList<Formation> getFormationTitlesOfGivenCivil(String civil){
        return mCivilToFormationMap.get(civil);
    }

    //Return all prepared and parsed army entries
    public HashMap<String, Formation> getScenarioFormations(){
        return mFormationMap;
    }
}



