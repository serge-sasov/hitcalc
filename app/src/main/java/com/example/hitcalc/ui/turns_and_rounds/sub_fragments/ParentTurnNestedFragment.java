package com.example.hitcalc.ui.turns_and_rounds.sub_fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.example.hitcalc.MainActivity;
import com.example.hitcalc.R;
import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.ui.combat_scenes.army.Army;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.combat_scenes.army.Scenario;
import com.example.hitcalc.ui.combat_scenes.army.WarriorInShock;
import com.example.hitcalc.ui.combat_scenes.army.views.GroupFormationView;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyInCombat;
import com.example.hitcalc.ui.turns_and_rounds.calculator.CombatDialogue;
import com.example.hitcalc.ui.turns_and_rounds.calculator.ShockCombatCalculator;
import com.example.hitcalc.ui.combat_scenes.map.HexView;
import com.example.hitcalc.ui.combat_scenes.map.Map;
import com.example.hitcalc.ui.combat_scenes.map.MapView;
import com.example.hitcalc.ui.turns_and_rounds.BasicFragment;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;
import com.example.hitcalc.utility.items_selector.ItemSelectorSimple;
import com.example.hitcalc.utility.items_selector.ItemSelectorSimpleHorizontal;
import com.google.android.material.tabs.TabLayout;
import com.opencsv.exceptions.CsvException;
import com.rollbar.android.Rollbar;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ParentTurnNestedFragment extends BasicFragment implements HexView.OnClickHexListener{

    private TurnNestedFragmentAdapter mAdapter;

    private boolean mIsAttackerHex = false; //shows default selected hex for configuration
    private GroupFormationView mGroupFormationView;
    private GameStorage mGameStorage;

    private Integer mHexX, mHexY; //Coordinates of chosen hex
    private Scenario mScenario;
    private String mAttackerCiv, mDefenderCiv; //Selected by user civilizations for attacker and defender units
    private Integer mDiceRollValue;
    private Integer mTerrainModifier;

    private Boolean mVisibleElements = false;

    private Formation mChosenFormation; //Currently used formation
    private WarriorInShock mSelectedWarrior;
    private MapView mMapView;
    private Map mMap; //Output of the configuration process is stored here

    //Data set for fragment pager
    private String mSelectedFormationTitle;

    public ParentTurnNestedFragment(GameStorage gameStorage, int position){

        //Get game storage from main activity and parse it out
        try {
            MainActivity activity = (MainActivity) getActivity();
            mGameStorage = gameStorage;
            mGame = mGameStorage.game();

        }catch(Error|Exception e){
            e.printStackTrace();
        }

        //hold current position index
        mPage = position;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = null;

        if(mPage == 0){
            //For the first tab show up only the weakness state of the given army entries
            root = inflater.inflate(R.layout.fragment_turn, container, false);

            //Persist action chosen at the each turn end, instantiate a new turn and switch user if necessary
            Button endTurnButton = (Button) root.findViewById(R.id.btnEndTurn);
            endTurnButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkTurnEnd(view);
                }
            });

            //------------------------------------------- Move to TurnSubFragment ------------------------------------------------
            //Get game storage from main activity and parse it out
            try {
                MainActivity activity = (MainActivity) getActivity();
                mGameStorage = activity.gameStorageData();
                mGame = mGameStorage.game();

            } catch (Error | Exception e) {
                e.printStackTrace();
            }

            setPagerAdapter(root);
        }else if(mPage == 1){
            //show the combat calculator layout
            root = prepareCombatCalculatorLayout(inflater, container, savedInstanceState);
        }

        return root;
    }

    private void checkTurnEnd(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Confirm");
        builder.setMessage("Are you sure?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //set activation flag of the chosen formation to true to consider this by further threshold calculation
                FormationInCombat formation = mGame.currentRound().currentTurn().getFormation();

                //Activate source root- & sub-formation to consider its state while activating the warriors
                ((FormationActivated) formation).activate();

                //terminate current turn & instantiate a new one
                mGame.finishTurn();

                try {
                    //Save changes made
                    mGameStorage.save(getContext());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

                // if round is finished, need to move to the round completion page, otherwise to the main round page
                if (mGame.checkRoundCompleted() == true) {
                    //Navigate to round completion fragment if any actions need to be done
                    Navigation.findNavController(view).navigate(R.id.action_rootTurnFragment_to_roundCompletionFragment);
                } else {
                    //Navigate to the turn fragment configuration page
                    Navigation.findNavController(view).navigate(R.id.action_rootTurnFragment_to_fragmentRound);
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    //Configure page adapter
    protected void setPagerAdapter(View view) {
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mTabLayout = (TabLayout) view.findViewById(R.id.combatTabs);

        //Pass to adapter a map list
        try {
            //Get used civilization and formation
            mAdapter = new TurnNestedFragmentAdapter(getChildFragmentManager(), mGameStorage, mFormationTitle);

            //Set adapter with preconfigured page number
            mPager.setAdapter(mAdapter);

            //Set current page number
            mPager.setCurrentItem(mPage);

        } catch (Error e) {
            e.printStackTrace();
        }

        //Allocate tabs
        setTabs(view);
    }

    //Show up the combat calculator for given formation
    private View prepareCombatCalculatorLayout(@NonNull LayoutInflater inflater,
                                               ViewGroup container, Bundle savedInstanceState){
        View root = inflater.inflate(R.layout.fragment_combat_scene_setup, container, false);
        //Get input data from previous activity
        try {
            //instantiate the map configuration
            setMapConfiguration(root);

            //load the input data
            prepareInputData();

            //configure combat calculator
            prepareCombatCalculator(root);

            //Terrain & Dice Modifiers
            setupTerrainModifierView(root);
            setupDiceRollView(root);


        } catch (JSONException | Error e ) {
            e.printStackTrace();
        }

        //Configure and display menu in case of defending formations & show all units out the currently chosen formation
        mGroupFormationView = (GroupFormationView) root.findViewById(R.id.groupFormationView);
        mGroupFormationView.configForFormationNavigation(mMap, mScenario, mAttackerCiv, mChosenFormation.getTitle(),mDefenderCiv);
        mGroupFormationView.setNavigationVisibility(false);

        //hide not necessary view elements by initial setup
        handleElementVisibility(root);
        //Handle expandable menu with configurable elements (terrain modifier, dice value and combat button)

        ImageButton expandBtn = (ImageButton) root.findViewById(R.id.expandBtn);
        expandBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //change visibility of the config elements
                mVisibleElements = !mVisibleElements;
                handleElementVisibility(root);
            }
        });

        return root;
    }

    //Show or Hide control elements being used for combat calculation.
    private void handleElementVisibility(View view){
        ItemSelectorSimpleHorizontal diceRoll = (ItemSelectorSimpleHorizontal) view.findViewById(R.id.diceRollSelector);
        ItemSelectorSimpleHorizontal terrainModifier = (ItemSelectorSimpleHorizontal) view.findViewById(R.id.terrainModifierSelector);
        Button calculateHitsBtn = (Button) view.findViewById(R.id.calculateHitsBtn);
        ImageButton expandBtn = (ImageButton) view.findViewById(R.id.expandBtn);

        if(mVisibleElements == true){
            expandBtn.setImageResource(R.drawable.button_up);
            diceRoll.setVisibility(View.VISIBLE);
            terrainModifier.setVisibility(View.VISIBLE);
            calculateHitsBtn.setVisibility(View.VISIBLE);
        }else{
            expandBtn.setImageResource(R.drawable.button_down);
            diceRoll.setVisibility(View.GONE);
            terrainModifier.setVisibility(View.GONE);
            calculateHitsBtn.setVisibility(View.GONE);
        }
    }

    @Override
    //Clear out the content of a hex the user clicked on if its coordinates coincides with the input provided.
    public void onHexClick(Integer x, Integer y) {
        //Check if any user is on the hex and populate it to the variable selected warrior.
        if(mMap != null) {
            mSelectedWarrior = mMap.retrieveWarriorFromHex(x, y);
        }
        //Determine the cell type and hide/show the navigation menu
        mIsAttackerHex = true;

        if(Map.getHexTypes()[y][x].equals("Defender")){
            //Hide attacker and show defender selection structure
            mIsAttackerHex = false;

            if(mSelectedWarrior != null) {
                //Retrieve formation the user belongs to
                String formationTitle = mScenario.getArmy(mDefenderCiv).getFormationByWarrior(mSelectedWarrior).getTitle();

                //Navigate the menu pointer to the selected formation
                mGroupFormationView.updateMenuItem(formationTitle);
            }
        }
        // hide/show the menu depending on the attacker / defender cell choosen
        mGroupFormationView.setNavigationVisibility(mIsAttackerHex);


        if(x == mHexX && y == mHexY) {
            mMapView.clearChosenHexIfClicked(x, y);
            if(mMap != null) {
                mMap.removeWarriorFromHex(x, y);
            }
            mSelectedWarrior = null;
        }
        else{
            //clear out the previous selection if any
            if(mHexX != null && mHexY != null){
                mMapView.setFocusOnHex(mHexX, mHexY);
            }

            //Activate new hex for configuration
            mHexX = x;
            mHexY = y;
            mMapView.setFocusOnHex(x, y);
        }
    }

    /************ BaseConfigActivity ************/
    protected void prepareMap(View view) throws JSONException {
        //Get a map
        mMapView = (MapView) view.findViewById(R.id.combat_field);

        //For new instance derive the map from the view
        mMap = mMapView.map();


        //Set a focus on selected hex
        if (mHexX != null && mHexY != null) {
            mMapView.setFocusOnHex(mHexX, mHexY);

            if(mSelectedWarrior != null){
                mMapView.putUnitOnHex(mSelectedWarrior, mHexX, mHexY);
            }
        }

        //save map instance into game
        mGameStorage.game().mapView(mMapView);

        //By default set focus on the first defending hex with coordinates: 1.1 and give this info to navigation
        mHexX = 1;
        mHexY = 1;
        mMapView.setFocusOnHex(mHexX, mHexY);
    }

    //prepare view configuration for terrain modifier
    private void setupTerrainModifierView(View view){
        ItemSelectorSimpleHorizontal terrainModifier = (ItemSelectorSimpleHorizontal) view.findViewById(R.id.terrainModifierSelector);
        terrainModifier.setOutlook(2, 25, 20, 10, 20, 10);

        ArrayList<String> values = new ArrayList<String>();
        values.add("-3");
        values.add("-2");
        values.add("-1");
        values.add("0");
        values.add("1");
        values.add("2");
        values.add("3");

        terrainModifier.setValues(values, "Terrain Modifier");
        terrainModifier.setOnClickItemSelectorListener(new ItemSelectorSimple.OnClickItemSelectorListener() {
            @Override
            public void onItemSelectorClick(String item) {
                //Get selected modifier value and provide it to the map view to store it in the data structure
                mTerrainModifier = Integer.parseInt(item);
                mMap.terrainModifier(mTerrainModifier);
            }
        });
    }

    //prepare view configuration for dice roll attempt
    private void setupDiceRollView(View view){
        //Setup dice roll input view
        ItemSelectorSimpleHorizontal diceRoll = (ItemSelectorSimpleHorizontal) view.findViewById(R.id.diceRollSelector);
        diceRoll.setOutlook(2, 25, 20, 10, 20, 10);

        ArrayList<String> values = new ArrayList<String>();
        for(Integer i = 0; i<=9; i++) {
            values.add(i.toString());
        }

        diceRoll.setValues(values, "Dice Roll Value");
        diceRoll.setOnClickItemSelectorListener(new ItemSelectorSimpleHorizontal.OnClickItemSelectorListener() {
            @Override
            public void onItemSelectorClick(String item) {
                //Get selected dice roll value provided by a user
                mDiceRollValue = Integer.parseInt(item);
            }
        });
    }

    //Add a click listener to the map to go to the hex configuration step
    protected void setMapConfiguration(View view) throws JSONException {
        prepareMap(view);

        String[][] hexTitles = Map.getHexTypes();
        int[][] hexViewIds = Map.getHexViewIds();

        for(int i = 0; i < hexTitles.length ;i++ ){
            for(int j = 0; j < hexTitles[i].length ;j++ ){
                //Set hex title
                HexView hexView =(HexView) view.findViewById(hexViewIds[i][j]);
                hexView.setCoordinateX(j);
                hexView.setCoordinateY(i);

                //For each cell assign its own listener and wait until it is fired in HexView
                hexView.setOnClickHexListener((HexView.OnClickHexListener) this); //<--- activity of fragment???
            }
        }
    }

    protected void prepareInputData(){
        mAttackerCiv = mGame.activePlayer().title();
        mScenario = mGameStorage.scenario();

        for(String civil : mScenario.getCivilizations()){
            if(!civil.equals(mAttackerCiv)){
                mDefenderCiv = civil;
            }
        }

        mSelectedFormationTitle = mGame.currentRound().currentTurn().formationTitle();
        mChosenFormation = mScenario.getArmy(mAttackerCiv).getFormationByTitle(mSelectedFormationTitle);
    }

    //setup config for combat calculator
    private void prepareCombatCalculator(View view){
        //Calculate shock combat results
        Button shockCombat = (Button) view.findViewById(R.id.calculateHitsBtn);
        shockCombat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer diceRollValue;

                try {
                    ShockCombatCalculator shockCombatCalculator =
                            new ShockCombatCalculator(mGameStorage, mGameStorage.game(), null);

                    //define a dice roll value source
                    if(mDiceRollValue == null){
                        diceRollValue = mGameStorage.diceRoll().nextInt(10);
                    }
                    else{
                        diceRollValue = mDiceRollValue;
                    }

                    //Call a dialogue
                    CombatDialogue dialog =
                            new CombatDialogue(shockCombatCalculator, mGameStorage, diceRollValue);

                    dialog.setTargetFragment(ParentTurnNestedFragment.this, 1);
                    dialog.show(getFragmentManager(), dialog.getClass().getName());
                } catch (IOException | CsvException | Error e) {
                    e.printStackTrace();

                    Rollbar.instance().error(e,"CombatFragment onCreateView ShockCombatCalculator");
                }
            }

        });
    }
}
