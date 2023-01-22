package com.example.hitcalc.ui.turns_and_rounds;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import com.example.hitcalc.MainActivity;
import com.example.hitcalc.R;
import com.example.hitcalc.ui.combat_scenes.army.Formation;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.sub_fragments.TurnNestedFragmentAdapter;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;

import java.io.IOException;
import java.util.Set;

public class TurnFragment extends BasicFragment {
    private TurnNestedFragmentAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_turn, container, false);

        //Persist action chosen at the each turn end, instantiate a new turn and switch user if necessary
        Button endTurnButton = (Button) root.findViewById(R.id.btnEndTurn);
        endTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set activation flag of the chosen formation to true to consider this by further threshold calculation
                FormationInCombat formation = mGame.currentRound().currentTurn().getFormation();
                //Activate the formation
                formation.activate();

                //Activate the given sub-formations
                Set<String> sub_formations = mGame.currentRound().currentTurn().getSubFormationTitles();
                if(sub_formations != null) {
                    for (String title : sub_formations) {
                        Formation item = formation.subFormations().get(title);
                        ((FormationInCombat) item).activate();
                    }
                }

                //terminate current turn & instantiate a new one
                mGame.finishTurn();

                try {
                    //Save changes made
                    mGameStorage.save(getContext());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        //------------------------------------------- Move to TurnSubFragment ------------------------------------------------
        //Get game storage from main activity and parse it out
        try {
            MainActivity activity = (MainActivity) getActivity();
            mGameStorage = activity.gameStorageData();
            mGame = mGameStorage.game();

        }catch(Error|Exception e){
            e.printStackTrace();
        }

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
}
