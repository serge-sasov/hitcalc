package com.example.hitcalc.ui.turns_and_rounds;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.example.hitcalc.MainActivity;
import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.game.Game;
import com.example.hitcalc.ui.turns_and_rounds.sub_fragments.RoundNestedFragmentAdapter;
import com.example.hitcalc.ui.turns_and_rounds.views.TrackerView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class RoundFragment extends BasicFragment {
    private RoundNestedFragmentAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_round, container, false);

        //Get game storage from main activity and parse it out
        try {
            MainActivity activity = (MainActivity) getActivity();
            mGameStorage = activity.gameStorageData();
            mGame = mGameStorage.game();

            //Instantiate a new game
            if (mGame == null) {
                //initialise the input attributes
                ArrayList<String> players = mGameStorage.scenario().getCivilizations();
                String firstPlayer = players.get(0);

                mGame = new Game(mGameStorage, firstPlayer);
                mGameStorage.setGame(mGame);
            }
        }catch(Error|Exception e){
            e.printStackTrace();
        }

        /*
        * Check if previous round is completed and new round is to be instantiated so that available action points
        * and removed formations & warriors after round completion activities may take effect.
        * */
        if(mGame.checkRoundCompleted() == true){
            mGame.newRound();
        }

        //Display game turn & round tracker
        TrackerView trackerView = (TrackerView) root.findViewById(R.id.trackerView);
        trackerView.config(mGame);

        setPagerAdapter(root);
        return root;
    }

    //Configure page adapter
    protected void setPagerAdapter(View view) {
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mTabLayout = (TabLayout) view.findViewById(R.id.combatTabs);

        //Pass to adapter a map list
        try {
            //Get used civilization and formation
            mAdapter = new RoundNestedFragmentAdapter(getChildFragmentManager(), mGameStorage, mFormationTitle);

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
}