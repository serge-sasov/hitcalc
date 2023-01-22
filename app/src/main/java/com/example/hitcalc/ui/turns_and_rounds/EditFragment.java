package com.example.hitcalc.ui.turns_and_rounds;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;

import com.example.hitcalc.MainActivity;
import com.example.hitcalc.R;
import com.example.hitcalc.ui.turns_and_rounds.sub_fragments.EditNestedFragmentAdapter;
import com.google.android.material.tabs.TabLayout;

//If revision of any warrior weakness is needed this fragment shall be used.
public class EditFragment extends BasicFragment {

    private EditNestedFragmentAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_turn, container, false);

        //get the End Turn Button to terminate current turn, spend applied actions, instantiate a new turn and switch user if necessary
        Button endTurnButton = (Button) root.findViewById(R.id.btnEndTurn);
        //set title -> "Complete Edit"
        endTurnButton.setText(getResources().getString(R.string.complete_edit));
        endTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Navigate to the turn fragment configuration page
                Navigation.findNavController(view).navigate(R.id.action_editFragment_to_fragmentRound);

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
            mAdapter = new EditNestedFragmentAdapter(getChildFragmentManager(), mGameStorage, mFormationTitle);

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
