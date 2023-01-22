package com.example.hitcalc.ui.turns_and_rounds;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.example.hitcalc.MainActivity;
import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationActivated;
import com.example.hitcalc.ui.turns_and_rounds.army_in_combat.FormationInCombat;
import com.example.hitcalc.ui.turns_and_rounds.sub_fragments.RoundCompletionNestedAdapter;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;

import java.io.IOException;

public class RoundCompletionFragment extends BasicFragment {
    private RoundCompletionNestedAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_turn, container, false);

        //get the End Turn Button to terminate current turn, spend applied actions, instantiate a new turn and switch user if necessary
        Button endTurnButton = (Button) root.findViewById(R.id.btnEndTurn);
        endTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //Save changes made
                    mGameStorage.save(getContext());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                //Check all actions are made and if so instantiate a new round
                checkRoundEnd(view);
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

    //Check all actions are made and if so instantiate a new round
    private void checkRoundEnd(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Confirm Round End");
        builder.setMessage("Are all actions made?");

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //Navigate to the round fragment configuration page
                Navigation.findNavController(view).navigate(R.id.action_roundCompletionFragment_to_fragmentRound);

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
            mAdapter = new RoundCompletionNestedAdapter(getChildFragmentManager(), mGameStorage);

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
