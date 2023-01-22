package com.example.hitcalc.ui.turns_and_rounds;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.example.hitcalc.MainActivity;
import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Army;
import com.example.hitcalc.ui.combat_scenes.army.Scenario;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.tables.UnitWeaknessTable;
import com.example.hitcalc.ui.turns_and_rounds.sub_fragments.ParentTurnNestedFragmentAdapter;
import com.google.android.material.tabs.TabLayout;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.HashMap;

public class ParentTurnFragment extends BasicFragment {

    private ParentTurnNestedFragmentAdapter mAdapter;

    /*
    * the class creates a list of sub-fragments holding:
    * fragment 0: weakness fragments for both player armies
    * fragment 1: shock battle field to calculate close combat outcome
    * */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_root_turn, container, false);

        //------------------------------------------- Move to TurnSubFragment ------------------------------------------------
        //Get game storage from main activity and parse it out
        try {
            MainActivity activity = (MainActivity) getActivity();
            mGameStorage = activity.gameStorageData();
            mGame = mGameStorage.game();

            //rebuild the scenario army array into ArmyActivated
            mGame.scenario(mGameStorage);
            rebuildScenario();

            //create a page adapter
            setPagerAdapter(root);

        }catch(Error|Exception e){
            e.printStackTrace();
        }

        return root;
    }

    //rebuild scenario array to show only the activated warriors used in the formation
    private void rebuildScenario(){
        FormationInCombat activatedFormation = mGame.currentRound().currentTurn().getFormation();
        String activePlayer = mGame.currentRound().currentTurn().activePlayer();

        Scenario scenario = mGameStorage.scenario();

        HashMap<String, Army> armies = scenario.getArmies();
        ArmyActivated sourceArmy = (ArmyActivated) armies.get(activePlayer);

        ArmyActivated army = new ArmyActivated(activePlayer, sourceArmy);
        army.addFormation(activatedFormation);
        armies.put(activePlayer, army);

        scenario.update(armies);
    }

    //Configure page adapter
    protected void setPagerAdapter(View view) {
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mTabLayout = (TabLayout) view.findViewById(R.id.combatTabs);

        //Pass to adapter a map list
        try {
            //Get used civilization and formation
            mAdapter = new ParentTurnNestedFragmentAdapter(getChildFragmentManager(), mGameStorage, mFormationTitle);

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

    protected void setTabs(View view){
       mTabLayout = (TabLayout) view.findViewById(R.id.combatTabs);

        mTabLayout.setupWithViewPager(mPager);
        //Use as tabs title the names of straggling nations

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab){
                mPage = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //mFragmentPage = tab.getPosition();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //mFragmentPage = tab.getPosition();
            }
        });

        //Setup titleS for given tabs layout
        for (int id = 0; id < mTabLayout.getTabCount(); id++) {
            String title = "Weakness Setup";
            if(id == 1){
                title = "Combat Scenes";
            }

            mTabLayout.getTabAt(id).setText(title);

        }
    }
}
