package com.example.hitcalc.ui.combat_scenes.army;

import com.example.hitcalc.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class WarriorItem extends Warrior{
    //Title,Type,Quality,Size,Movement,Property,Civilization,TwoHex
    protected Integer mSize; //Unit size
    protected Integer mMovement; // Unit movement allowance
    protected Integer mTroopQuality; // Unit troop quality
    protected String mProperty = "";
    protected String mAddProperty = ""; //additional property being used by legion units
    protected String mCivilization = "";
    protected String mColor = "default";
    protected Integer mLegioId; //unique legio identity
    public static HashMap<String, Integer> mUnitColors = createMap(); //Available units color schemes

    //Default constructor to copy object
    public WarriorItem() {

    }

    //Populate a set of colour schemes for units
    private static HashMap<String, Integer> createMap() {
        HashMap<String, Integer> myMap = new HashMap<String, Integer>();
        myMap.put("white", R.drawable.rounded_border_white);
        myMap.put("red", R.drawable.rounded_border_red);
        myMap.put("blue", R.drawable.rounded_border_blue);
        myMap.put("blue_light", R.drawable.rounded_border_blue_light);
        myMap.put("green", R.drawable.rounded_border_green);
        myMap.put("brown", R.drawable.rounded_border_brown);
        myMap.put("brown_light", R.drawable.rounded_border_brown_light);
        myMap.put("yellow", R.drawable.rounded_border_yellow);
        myMap.put("silver", R.drawable.rounded_border_silver);
        myMap.put("turquoise", R.drawable.rounded_border_turquoise);
        myMap.put("orange", R.drawable.rounded_border_orange);
        myMap.put("default", R.drawable.rounded_border_default);
        return myMap;
    }

    // JSON constants
    private static final String SIZE = "size";
    private static final String MOVEMENT = "movement";
    private static final String TROOP_QUALITY = "troop_quality";
    private static final String PROPERTY = "property";
    private static final String ADD_PROPERTY = "add_property";
    private static final String CIVILIZATION = "civilization";
    private static final String COLOR = "color";
    private static final String LEGIO_ID = "legio_id";

    //Title,Type,Quality,Size,Movement,Property,Army,TwoHex
    public WarriorItem(String title, String type, Integer tq, Integer size, Integer mov,
                       String prop, String addProp, String civil, boolean isTwoHex, String color, Integer legioId){
        super(title, type, isTwoHex);
        mSize = size;
        mMovement = mov;
        mTroopQuality = tq;
        mProperty = prop;
        mAddProperty = addProp;
        mCivilization = civil;
        mColor = color;
        mLegioId = legioId;
    }

    //JSON Constructor
    public WarriorItem(JSONObject jo) throws JSONException {
        super(jo);

        //Leader units do not have Size, Mov and TQ characteristics
        if(jo.has(SIZE)) {
            mSize = jo.getInt(SIZE);
        }

        if(jo.has(MOVEMENT)) {
            mMovement = jo.getInt(MOVEMENT);
        }
        if(jo.has(TROOP_QUALITY)) {
            mTroopQuality = jo.getInt(TROOP_QUALITY);
        }

        if(jo.has(LEGIO_ID)) {
            mLegioId = jo.getInt(LEGIO_ID);
        }

        mProperty = jo.getString(PROPERTY);
        mAddProperty = jo.getString(ADD_PROPERTY);
        mCivilization = jo.getString(CIVILIZATION);
        mColor = jo.getString(COLOR);
    }

    //Clone WarriorItem
    public WarriorItem(WarriorItem warrior){
        super();
        mSize = warrior.size();
        mMovement = warrior.movement();
        mTroopQuality = warrior.troopQuality();
        mProperty = warrior.property();
        mAddProperty = warrior.addProperty();
        mCivilization = warrior.civilization();
        mColor = warrior.color();
        mLegioId =legioId();
    }

    public Integer size(){
            return mSize;
        }

    public Integer movement(){
        return mMovement;
    }

    public Integer troopQuality(){
            return mTroopQuality;
        }

    public String property(){
        return mProperty;
    }

    public String addProperty(){
        return mAddProperty;
    }

    public String color(){
        return mColor;
    }

    public String civilization(){
        return mCivilization;
    }

    public Integer legioId() {
        return mLegioId;
    }

    // ------------------------- JSON Conversion ---------------------------
    /*
     * Converter to JSON object
     */
    public JSONObject convertToJSON() throws JSONException {
        //Need to call all previous methods in consequentially
        JSONObject jo = new JSONObject();
        jo = super.convertToJSON();

        jo.put(SIZE, mSize);
        jo.put(MOVEMENT, mMovement);
        jo.put(TROOP_QUALITY, mTroopQuality);
        jo.put(PROPERTY, mProperty);
        jo.put(ADD_PROPERTY, mAddProperty);
        jo.put(CIVILIZATION, mCivilization);
        jo.put(COLOR, mColor);
        jo.put(LEGIO_ID, mLegioId);
        return jo;
    }

    //provide the unit nature, i.e. simple, or any other type like commander or harizma
    public Boolean simple(){
        if(mType.equals("Leader") || mType.equals("Harizma") || mType.equals("Dummy")){
            return false;
        }
      return true;
    }

    /*
        Allows to determine a determine the correct warrior type for further shock combat calculation
        in case any legio cohort capable warrior is used
     */
    public String shockCombatType(){
        Integer legioId = mLegioId;
        String type = mType;

        if(legioId != null && legioId > 0){
            if(mAddProperty.equals("CO")){
                if(legioId == 1 || legioId == 10){
                    //veteran case, nothing is needed
                    type = "LG";
                }else if(legioId == 3 || legioId == 5){
                    // recruit case
                    type = "HI";
                }else if(legioId == 7 || legioId == 14 || legioId == 15 || legioId == 19){
                    //conscript case
                    type = "MI";
                }
            }
        }

        return type;
    }
}
