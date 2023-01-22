package com.example.hitcalc.ui.turns_and_rounds;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.hitcalc.MainActivity;
import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.ArmyInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.WarriorInCombat;
import com.example.hitcalc.ui.turns_and_rounds.game.Game;
import com.example.hitcalc.storage.GameStorage;
import com.example.hitcalc.ui.turns_and_rounds.sub_fragments.RoundNestedFragmentAdapter;
import com.example.hitcalc.ui.turns_and_rounds.views.TrackerView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class BasicFragment extends Fragment implements FormationInCombat.OnRootListener {
    //keep the backup of the configuration in the file
    protected GameStorage mGameStorage;

    //instance of the current game
    protected Game mGame;

    //sub-fragments
    protected ViewPager mPager;
    protected TabLayout mTabLayout;

    //fragment related data
    protected Integer mPage = 0; //current page number a user navigated to
    private HashMap<Integer, Integer> mTabToRootScore = new HashMap<Integer, Integer>(); //mapping between tab id and army root score
    private HashMap<Integer, String>  mTabToTitle = new HashMap<Integer, String>(); //mapping between tab id and army title
    //Selected formation to be activated in the turn
    protected String mFormationTitle;

    protected void setTabs(View view){
        ArmyInCombat army;
        String title;
        String message;

        mTabLayout = (TabLayout) view.findViewById(R.id.combatTabs);

        mTabLayout.setupWithViewPager(mPager);
        //Use as tabs title the names of straggling nations
        //mTabLayout.getTabAt(i).setText(title);

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

        //Setup title content for tabs layout
        for (int id = 0; id < mTabLayout.getTabCount(); id++) {
            if (id == 0) {
                army = mGame.getActivePlayersArmy();
            } else {
                army = mGame.getPassivePlayersArmy();

            }
            title = army.getCivilizationTitle();
            mTabToRootScore.put(id, army.score());
            mTabToTitle.put(id, title);

            message = title + " (" + army.score() + ")";
            if(id == 0){
                message = title + " (" + army.score() + ")" + "\n" + getResources().getString(R.string.active_player);
            }

            //Set army title & army root value
            mTabLayout.getTabAt(id).setText(message);

            //set listener for warrior root event to recalculate the army root value as a warrior roots
            for(Formation formation: army.getFormations()){
                ((FormationInCombat) formation).setOnRootListener((BasicFragment) this, id);
            }
        }
    }

    /*
        Recalculate the army resulting root value as a new warrior roots
        caused by manual move to the rooted warriors range.
     */
    @Override
    public void onRoot(WarriorInCombat warrior, Integer tabId) {
        String title;
        String message;
        //recalculate score value for the given tab
        int score = mTabToRootScore.get(tabId) + warrior.score();

        //update the recalculated value
        mTabToRootScore.put(tabId, score);

        title = mTabToTitle.get(tabId);

        message = title + " (" + score + ")";
        if(tabId == 0){
            message = title + " (" + score + ")" + "\n" + getResources().getString(R.string.active_player);
        }

        mTabLayout.getTabAt(tabId).setText(message);
    }

    /*
        Recalculate the army resulting root value as a new warrior roots
        caused by manual move to the rooted warriors range.
     */
    @Override
    public void onRecover(WarriorInCombat warrior, Integer tabId) {
        String title;
        String message;
        //recalculate score value for the given tab
        int score = mTabToRootScore.get(tabId) - warrior.score();

        //update the recalculated value
        mTabToRootScore.put(tabId, score);

        title = mTabToTitle.get(tabId);

        message = title + " (" + score + ")";
        if(tabId == 0){
            message = title + " (" + score + ")" + "\n" + getResources().getString(R.string.active_player);
        }

        mTabLayout.getTabAt(tabId).setText(message);
    }
}