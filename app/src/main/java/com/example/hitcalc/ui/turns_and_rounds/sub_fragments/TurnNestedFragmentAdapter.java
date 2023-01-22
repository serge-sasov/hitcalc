package com.example.hitcalc.ui.turns_and_rounds.sub_fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.hitcalc.storage.GameStorage;

public class TurnNestedFragmentAdapter extends FragmentStatePagerAdapter {
    //keep the backup of the configuration in the file
    private GameStorage mGameStorage;

    //Selected formation to be activated in the turn
    private String mFormationTitle;

    public TurnNestedFragmentAdapter(@NonNull FragmentManager fm, GameStorage gameStorage, String formationTitle) {
        super(fm);
        mGameStorage = gameStorage;
        mFormationTitle = formationTitle;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return new TurnNestedFragment(mGameStorage, position);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
