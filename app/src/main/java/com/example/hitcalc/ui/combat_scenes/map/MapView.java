package com.example.hitcalc.ui.combat_scenes.map;

import android.content.ClipData;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.combat_scenes.army.WarriorItem;
import com.example.hitcalc.ui.combat_scenes.army.views.UnitView;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;
import com.example.hitcalc.utility.items_selector.ItemSelectorSimple;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MapView extends LinearLayout {
    private Map mMap;
    private Integer mTerrainModifier = 0;

    //the variables are used to remove the unit data from the mMap object once the move is completed
    private Integer mShiftedUnitCoordinateX;
    private Integer mShiftedUnitCoordinateY;
    private boolean mIsShifted = false;

    private Integer mFocusHexX, mFocusHexY; //Focus on the hex currently being configured

    public MapView(Context context) {
        super(context);
        initializeViews(context);
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public MapView(Context context,
                   AttributeSet attrs,
                   int defStyle) {
        super(context, attrs, defStyle);
        initializeViews(context);
    }

    /**
     * Inflates the views in the layout.
     *
     * @param context
     *           the current context for the view.
     */
    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.map_view, this);

        //Set user specific data if data were provided
        setHexConfiguration();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    protected void setHexConfiguration(){
        //Initalize Map configuration
        mMap = new Map();

        for(int y = 0; y < Map.getHexViewIds().length ;y++ ){
            for(int x = 0; x < Map.getHexViewIds()[y].length ;x++ ){
                //Set hex title
                HexView hexView =(HexView) findViewById(Map.getHexViewIds()[y][x]);
                hexView.setHex(Map.getHexTypes()[y][x]);
                hexView.setCoordinateX(x);
                hexView.setCoordinateY(y);

                //Set listener for on Drag event to catch the unit moved to the cell
                hexView.setOnDragListener(new MyDragListener());
                UnitView warriorView = hexView.findViewById(R.id.smallUnit);

                warriorView.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        hexView.toBeMoved(true); //store the flag that the unit is going to be moved away

                        WarriorInShock warrior = warriorView.warrior();
                        FormationActivated formationActivated = null;

                        //search for formation activated
                        for(FormationActivated formation : mMap.formations().keySet()){
                            if(mMap.formations().get(formation).contains(warrior)){
                                formationActivated = formation;
                            }
                        }

                        mShiftedUnitCoordinateX = hexView.getCoordinateX();
                        mShiftedUnitCoordinateY = hexView.getCoordinateY();
                        mIsShifted = true; //set this attribute to true to notice drop event that previous hex and map data set need to be cleaned up

                        //put a warrior into tmp table
                        mMap.placeWarrior(formationActivated, warrior);

                        //remove warrior along with any stocked units from the map
                        mMap.erase(warrior);

                        JSONObject jo = new JSONObject();
                        // --------- build up json data --------------
                        try {
                            jo.put("formation_hashcode", formationActivated.hashCode());
                            jo.put("warrior_hashcode", warrior.hashCode());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // --------- build up json data --------------
                        ClipData data = ClipData.newPlainText("hashcodes", jo.toString());

                        DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

                        view.startDrag( data, //data to be dragged
                                shadowBuilder, //drag shadow
                                view, //local data about the drag and drop operation
                                0   //no needed flags
                        );

                        view.setVisibility(View.GONE);
                        return true;
                    }
                });
            }
        }
    }

    class MyDragListener implements OnDragListener {

        @Override
        public boolean onDrag(View view, DragEvent event) {
            WarriorInShock warrior = null; //warrior dragged by user to place on the hex
            WarriorInShock warriorInHex; //Warrior in hex a new warrior shall be staked with
            int action = event.getAction();
            HexView hexView = (HexView) view;
            Integer formationHashCode = null;
            Integer warriorHashCode = null;

            Log.d("Drag Event = ", "" + action);
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    warrior = hexView.warrior();

                    //clear up all stoked attached units and effects from the warrior
                    if(warrior != null && hexView.toBeMoved() == true) {
                        warrior.stokedWarrior(null);
                        warrior.leader(null);
                        warrior.harizma(null);
                    }

                    // Check first whether the unit was moved from a hex around
                    clearUnitFromHexAndMap();

                    break;
                case DragEvent.ACTION_DROP:
                    // Check first whether the unit was moved from a hex around
                    clearUnitFromHexAndMap();

                    ClipData.Item clip = event.getClipData().getItemAt(0);
                    String hashCodes = clip.getText().toString();

                    try {
                        JSONObject jo = new JSONObject(hashCodes);
                        if(jo.has("formation_hashcode")){
                            formationHashCode = jo.getInt("formation_hashcode");
                        }
                        if(jo.has("warrior_hashcode")){
                            warriorHashCode = jo.getInt("warrior_hashcode");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //search warrior objects using the hash codes
                    HashMap<FormationActivated, WarriorInShock> movingWarrior = mMap.placeWarrior();

                    if(movingWarrior != null){
                        for(Formation formation : movingWarrior.keySet()){
                            if(formation.hashCode() == formationHashCode){
                                if(movingWarrior.get(formation) != null){
                                    //derive placed warrior
                                    warrior = movingWarrior.get(formation);

                                    //store only warrior & leaders
                                    if(warrior.type().equals("Harizma") != true) {
                                        //place a warrior into activated warrior list
                                        mMap.warrior((FormationActivated) formation, warrior);

                                        //place attached leader into the list
                                        if (warrior.leader() != null && warrior.leader() instanceof WarriorInShock) {
                                            mMap.warrior((FormationActivated) formation, (WarriorInShock) warrior.leader());
                                        }

                                        //place stocked leader into list
                                        if (warrior.stokedWarrior() != null) {
                                            mMap.warrior((FormationActivated) formation, (WarriorInShock) warrior.stokedWarrior());
                                        }
                                    }
                                }
                            }
                        }
                    }

                    try {

                        if(warrior != null){

                            Integer hexX = hexView.getCoordinateX();
                            Integer hexY = hexView.getCoordinateY();

                            warriorInHex = mMap.retrieveWarriorFromHex(hexX,hexY);

                            if (warrior.type().equals("Leader") || warrior.type().equals("Harizma")  ) {
                                if (warriorInHex != null) {
                                    //if there is a warrior on hex than add the leader to him
                                    warriorInHex.leader(warrior);
                                }
                            } else {
                                //Check if any warrior is in hex than stoke a new with him
                                if (warriorInHex != null) {
                                    //Stork a new warrior with a unit placed in hex
                                    warriorInHex.stokedWarrior((WarriorItem) warrior);
                                } else {
                                    //there is still no unit in hex, so place a new warrior there
                                    warriorInHex = warrior;
                                }
                            }

                            //Store the data and update view outlook
                            mMap.placeWarriorOnHex(warriorInHex, hexX, hexY);
                            putUnitOnHex(warriorInHex, hexX, hexY); //update unit view
                            Log.d("UnitDraggedInHex ", " Unit" + warriorInHex.title() + "X: " + hexView.getCoordinateX() + " Y: " + hexView.getCoordinateY());
                        }

                    } catch (Error e) {
                        e.printStackTrace();
                    }

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    //to define in future
                    break;
                default:
                    break;
            }
            Log.d("Drag Action Id ", "" + action);
            return true;
        }
    }

    private Integer deriveMovingFormationHashcode(View view, DragEvent event){
        WarriorInShock warriorInHex; //Warrior in hex a new warrior shall be staked with
        int action = event.getAction();
        HexView hexView = (HexView) view;

        ClipData.Item clip = event.getClipData().getItemAt(0);
        String hashCodes = clip.getText().toString();

        Integer formationHashCode = null;
        //Integer warriorHashCode = null;

        try {
            JSONObject jo = new JSONObject(hashCodes);
            if(jo.has("formation_hashcode")){
                formationHashCode = jo.getInt("formation_hashcode");
            }
            /*
            if(jo.has("warrior_hashcode")){
                warriorHashCode = jo.getInt("warrior_hashcode");
            }
             */
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return formationHashCode;
    }

    private WarriorInShock recognizeMovingWarrior(Integer formationHashCode){
        WarriorInShock warrior = null; //warrior dragged by user to place on the hex
        //search warrior objects using the hash codes
        HashMap<FormationActivated, WarriorInShock> movingWarrior = mMap.placeWarrior();

        if(movingWarrior != null){
            for(Formation formation : movingWarrior.keySet()){
                if(formation.hashCode() == formationHashCode){
                    if(movingWarrior.get(formation) != null){
                        //derive placed warrior
                        warrior = movingWarrior.get(formation);
                    }
                }
            }
        }

        return warrior;
    }


    /*
    * Remove unit from hex and map to clear out any retired data
    * */
    private void clearUnitFromHexAndMap(){
        if(mIsShifted == true){
            //remove the unit from the past location
            clearChosenHex(mShiftedUnitCoordinateX, mShiftedUnitCoordinateY);
            mMap.removeWarriorFromHex(mShiftedUnitCoordinateX, mShiftedUnitCoordinateY);
            mIsShifted = false;
        }
    }

    /*
    * use this function to highlight the chosen hex
    * */
    public boolean setFocusOnHex(Integer hexX, Integer hexY){
        HexView hexView = (HexView) findViewById(Map.getHexViewIds()[hexY][hexX]);
        mFocusHexX = hexX;
        mFocusHexY = hexY;

        return hexView.selectHex();
    }

    public void putUnitOnHex(WarriorInShock warrior, Integer hexX, Integer hexY) {
        HexView hexView = (HexView) findViewById(Map.getHexViewIds()[hexY][hexX]);
        hexView.setHex(warrior);
    }

    //Remove selected unit out of the hex if it is clicked
    public void clearChosenHexIfClicked(Integer hexX, Integer hexY){
        HexView hexView = (HexView) findViewById(Map.getHexViewIds()[hexY][hexX]);
        hexView.clearHex();

        if(mMap != null) {
            mMap.removeWarriorFromHex(hexX, hexY);
        }
    }

    //Remove selected unit out of the hex if it is clicked
    public void clearChosenHex(Integer hexX, Integer hexY){
        //If unit is just selected then store his object and update the state
        HexView hexView = (HexView) findViewById(Map.getHexViewIds()[hexY][hexX]);
        mMap.removeWarriorFromHex(hexX, hexY);

        hexView.clearHex();
    }

    //Remove selected unit out of the hex if it is clicked
    public void clearChosenHex(){
        //If unit is just selected then store his object and update the state
        clearChosenHex(mFocusHexX, mFocusHexY);
    }

    //clean up all used warriors in the scene
    public void cleanUp() {
        mMap.erase();

        String [][] hexTypes = Map.getHexTypes();

        for (int y = 0; y < hexTypes.length; y++) {
            for (int x = 0; x < hexTypes[y].length; x++) {
                clearChosenHex(x, y);
            }
        }
    }


    /*
    * Allows to retrieve a map configuration done directly via drag and drop functionality
    */
    public Map map(){
        return mMap;
    }

    /*
     * Restore the previous configuration
     */
    public void setMap(Map map){
        mMap = map;
        //restore terrain configuration
        setTerrainModifier(map.terrainModifier());
        ItemSelectorSimple terrainModifier = (ItemSelectorSimple) findViewById(R.id.terrainModifierSelector);
        terrainModifier.setSelectedValue(mMap.terrainModifier().toString());


        //restore hex's configuration and place units on it
        int[][] hexViewIds = Map.getHexViewIds();
        for (int y = 0; y < hexViewIds.length; y++) {
            for (int x = 0; x < hexViewIds[y].length; x++) {
                //If there is a warrior put it on hex
                if (mMap.retrieveWarriorFromHex(x, y) != null) {
                    putUnitOnHex(mMap.retrieveWarriorFromHex(x, y), x, y);
                }
            }
        }
    }

    //Provide to the map view a value selected by user out of the main activity
    public void setTerrainModifier(Integer value){
        /*
        * keep value locally and in map object for further use by transferring it
        *  to another activity and calculation of battle outcome
        */
        mTerrainModifier = value;
        mMap.terrainModifier(value);
    }

    public Integer getTerrainModifier(){
        return mTerrainModifier;
    }
}
