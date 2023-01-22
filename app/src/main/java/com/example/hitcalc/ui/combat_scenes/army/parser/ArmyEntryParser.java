package com.example.hitcalc.ui.combat_scenes.army.parser;

import android.util.Log;

import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.utility.LoadTable;
import com.example.hitcalc.utility.ParseTable;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//holds the bundle of attr and value of a formation belonging to the scenario
public class ArmyEntryParser {
    private final LoadTable mUnitsTable;
    //row data set
    private ArrayList<String> mAttributes;
    private ArrayList<String> mValues;

    private Integer mSeizurePoints = null;

    //formation data set
    // a list of warriors title to type map belonning to the given formation
    private ArrayList<WarriorItem> mWarriors;
    private String mLeader;
    private String mArmyTitle;
    private String mFormationTitle;
    private Formation mFormation; //pre-Formation object

    private boolean mStandalone = true;
    // a map of formation/association type representing the possibility to be activated together with that formation
    private HashMap<String, String> mAssociationWithSubFormations;

    
    public ArmyEntryParser(LoadTable unitTable) {
        mUnitsTable = unitTable;
    }

    //cut the first 2 elements, move the strings either to attr or to values
    public void addEntry(String [] strings) {
        if (strings[1].equals("value")) {
            mValues = cutHeader(strings);
        } else {
            mAttributes = cutHeader(strings);
        }
    }
    //Cut 2 first elements from the header and transform the output to ArrayList<String>
    private ArrayList<String> cutHeader(String [] strings) {
        ArrayList<String> outString = new ArrayList<String>();
        for(int i = 2; i<strings.length ; i++) {
            outString.add(strings[i]);
        }
        return outString;
    }

    //Parse row data into internal attributes structure
    public void parseRowEntryData() throws IOException, CsvException {
        String attribute, value;
        ParseTable configUnitsTable = new ParseTable(mUnitsTable);
        mWarriors = new ArrayList<WarriorItem>();

        for( int i = 0; i< mAttributes.size(); i++){
            attribute = mAttributes.get(i);
            value = mValues.get(i);
            //Header part: {Army,	Title,	Leader,	Standalone, OR || AND}
            if(attribute.equals("Army")){
                mArmyTitle = value;
            }

            if(attribute.equals("Title")){
                mFormationTitle = value;
            }

            if(attribute.equals("Leader")){
                String [] unit = configUnitsTable.getUnitItemByIdOrName(value);
                if(unit != null) {
                    if (mFormationTitle == null) {
                        mFormationTitle = unit[1];
                    }
                    mLeader = unit[1];
                }
            }

            //if set to no the formation can not be used independently (applied for sub-formation mostly)
            if(attribute.equals("Standalone") && value.equals("No")){
                mStandalone = false;
            }

            //if there is any available seizure points
            if(attribute.equals("SeizurePoints")){
                 mSeizurePoints = getInegerValue(value);
            }

            if(attribute.equals("OR") || attribute.equals("AND")){
                if(mAssociationWithSubFormations == null) {
                    mAssociationWithSubFormations = new HashMap<String, String>();
                }
                mAssociationWithSubFormations.put(value, attribute);
            }

            //List of warriors
            if(attribute.equals("PH") ||
                    attribute.equals("HI") ||
                    attribute.equals("MI") ||
                    attribute.equals("LI") ||
                    attribute.equals("LG") ||
                    attribute.equals("LP") ||
                    attribute.equals("SK") ||
                    attribute.equals("HC") ||
                    attribute.equals("LC") ||
                    attribute.equals("LN") ||
                    attribute.equals("RC") ||
                    attribute.equals("GC") ||
                    attribute.equals("BC") ||
                    attribute.equals("EL") ||
                    attribute.equals("CH") ||
                    attribute.equals("Leader") ||
                    attribute.equals("Dummy")||
                    attribute.equals("Harizma")){
                //Create Warrior Item belonging to the formation
                //Title is used as a key for search for Type,Quality,Size,Movement,Property,Army,TwoHex
                String [] unit = configUnitsTable.getUnitItemByNameAndType(value, attribute);

                //Create object if units actually exists
                if(unit != null) {
                    String title, type;
                    Integer tq, size, mov, legioId;
                    String unitId, property, addProperty, civilization, color;
                    boolean isTwoHex;

                    unitId = unit[0];
                    title = unit[1];
                    type = unit[2];
                    tq = getInegerValue(unit[3]);
                    size = getInegerValue(unit[4]);
                    mov = getInegerValue(unit[5]);
                    property = unit[6];
                    addProperty = unit[7];
                    civilization = unit[8];
                    isTwoHex = configUnitsTable.convertToBoolean(unit[9]);
                    color = unit[10];

                    //legion Id to derive a proper cohort class (veteran, recruit or conscript)
                    legioId = getInegerValue(unit[11]);

                    mWarriors.add(new WarriorItem(title, type, tq, size, mov,
                            property, addProperty, civilization, isTwoHex, color, legioId));
                }
                else{
                    //notify user that a unit was not found
                    Log.w("Class.Formation", "Unit not found: " + value + " " + attribute);
                }
            }
        }

        //Create formation object
        mFormation = new Formation(mWarriors, mLeader, mFormationTitle, mStandalone, mSeizurePoints);
    }

    //Populate sub-formation with prepared formation and association type
    public void populateSubFormations(ArrayList<Formation> formationList){
        //Get each Formation title -> determine its association and populate sub-formation
        for(Formation formation: formationList){
            boolean association = true;
            //Assign the association flag to the formation if there is any
            // sub-formation to treat it further by building the vision
            if(mAssociationWithSubFormations.get(formation.getTitle()) != null){
                if(mAssociationWithSubFormations.get(formation.getTitle()).equals("OR")){
                association = false;
                }
                //set sub-formation
                mFormation.addSubFormation(formation, association);
            }
        }
    }


    //Use function to transform null string into null or real velue if any provided
    protected Integer getInegerValue(String value){
        Integer result = null;

        if(value.length() > 0) {
            result = Integer.parseInt(value);
        }
        return result;
    }

    public Formation getFormation() {
        return mFormation;
    }

    //Check whether there is any sub-formation
    public HashMap<String, String> getAssociationWithSubFormations() {
        return mAssociationWithSubFormations;
    }

    public String getArmyTitle(){
        return mArmyTitle;
    }

    //Check whether the formation can be used alone
    public boolean isStandalone(){
        return mStandalone;
    }
}
